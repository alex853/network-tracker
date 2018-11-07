package net.simforge.networkview.flights3.persistence;

import net.simforge.commons.hibernate.HibernateUtils;
import net.simforge.commons.legacy.BM;
import net.simforge.networkview.datafeeder.ReportUtils;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import net.simforge.networkview.flights.datasource.ReportDatasource;
import net.simforge.networkview.flights2.Position;
import net.simforge.networkview.flights2.flight.FlightStatus;
import net.simforge.networkview.flights2.flight.Flightplan;
import net.simforge.networkview.flights3.Flight;
import net.simforge.networkview.flights3.PersistenceLayer;
import net.simforge.networkview.flights3.PilotContext;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class DBPersistenceLayer implements PersistenceLayer {

    private static Logger logger = LoggerFactory.getLogger(DBPersistenceLayer.class.getName());

    private SessionFactory sessionFactory;
    private ReportDatasource reportDatasource;

    public DBPersistenceLayer(SessionFactory sessionFactory, ReportDatasource reportDatasource) {
        this.sessionFactory = sessionFactory;
        this.reportDatasource = reportDatasource;
    }

    @Override
    public List<PilotContext> loadActivePilotContexts(LocalDateTime lastProcessedReportDt) throws IOException {
        BM.start("DBPersistenceLayer.loadActivePilotContexts");
        try (Session session = sessionFactory.openSession()) {
            //noinspection unchecked,JpaQlInspection
            List<DBPilotStatus> dbPilotStatuses = session
                    .createQuery("select ps from PilotStatus ps where currFlight is not null")
                    .list();

            logger.info("Loaded {} DB pilot statuses. Converting into pilot contexts...", dbPilotStatuses.size());

            long lastDt = System.currentTimeMillis();
            int done = 0;
            List<PilotContext> pilotContexts = new ArrayList<>();
            for (DBPilotStatus dbPilotStatus : dbPilotStatuses) {
                pilotContexts.add(toPilotContext(session, dbPilotStatus.getPilotNumber(), dbPilotStatus));
                done++;

                long now = System.currentTimeMillis();
                if (now - lastDt > 10000) {
                    logger.info("    {} % done", Math.round((100.0 * done) / dbPilotStatuses.size()));
                    lastDt = now;
                }
            }

            logger.info("All done");

            return pilotContexts;
        } finally {
            BM.stop();
        }
    }

    @Override
    public PilotContext createContext(int pilotNumber, Report seenReport) {
        BM.start("DBPersistenceLayer.createContext");
        try (Session session = sessionFactory.openSession()) {
            DBPilotStatus dbPilotStatus = loadPilotStatus(session, pilotNumber);

            if (dbPilotStatus != null) {
                throw new IllegalArgumentException("Pilot status for pilot " + pilotNumber + " already exists");
            }

            dbPilotStatus = new DBPilotStatus();
            dbPilotStatus.setPilotNumber(pilotNumber);
            dbPilotStatus.setLastProcessedReportId(seenReport.getId());
            dbPilotStatus.setLastProcessedDt(ReportUtils.fromTimestampJava(seenReport.getReport()));

            HibernateUtils.saveAndCommit(session, dbPilotStatus);

            return new PilotContext(pilotNumber);
        } finally {
            BM.stop();
        }
    }

    @Override
    public PilotContext loadContext(int pilotNumber) throws IOException {
        BM.start("DBPersistenceLayer.loadContext");
        try (Session session = sessionFactory.openSession()) {

            DBPilotStatus dbPilotStatus = loadPilotStatus(session, pilotNumber);

            if (dbPilotStatus == null) {
                return null;
            }

            return toPilotContext(session, pilotNumber, dbPilotStatus);

        } finally {
            BM.stop();
        }
    }

    @Override
    public PilotContext saveChanges(PilotContext pilotContext) {
        BM.start("DBPersistenceLayer.saveChanges");
        try (Session session = sessionFactory.openSession()) {
            HibernateUtils.transaction(session, () -> {

                DBPilotStatus dbPilotStatus = loadPilotStatus(session, pilotContext.getPilotNumber());

                //noinspection unchecked
                List<Flight> recentFlights = (List) pilotContext.getRecentFlights();

                List<Flight> dirtyFlightsList = recentFlights.stream().filter(Flight::isDirty).collect(toList());
                Flight currFlight = pilotContext.getCurrFlight();
                if (currFlight != null) {
                    dirtyFlightsList.add(currFlight);
                }

                List<DBFlight> loadedDbFlights = !dirtyFlightsList.isEmpty()
                        ? loadDbFlightsByFirstSeenReportId(session, pilotContext.getPilotNumber(), dirtyFlightsList)
                        : Collections.EMPTY_LIST;
                Map<Long, DBFlight> loadedDbFlightsMap = loadedDbFlights.stream().collect(Collectors.toMap(DBFlight::getFirstSeenReportId, Function.identity()));

                Iterator<Flight> it = dirtyFlightsList.iterator();
                while (it.hasNext()) {
                    Flight flight = it.next();

                    if (!flight.isDirty()) {
                        // we put currFlight to dirty list but if the currFlight is not changed there is no need to update it
                        it.remove();
                        continue;
                    }

                    DBFlight dbFlight = loadedDbFlightsMap.get(flight.getFirstSeen().getReportId());
                    if (dbFlight == null) {
                        continue; // it seems we have new flight
                    }
                    it.remove();

                    toDbFlight(dbFlight, pilotContext.getPilotNumber(), flight);

                    session.update(dbFlight);
                }

                // persist new flights to DB
                for (Flight flight : dirtyFlightsList) {
                    DBFlight dbFlight = new DBFlight();

                    toDbFlight(dbFlight, pilotContext.getPilotNumber(), flight);

                    session.save(dbFlight);

                    loadedDbFlightsMap.put(dbFlight.getFirstSeenReportId(), dbFlight);
                }

                Position lastProcessedPosition = pilotContext.getLastProcessedPosition();
                dbPilotStatus.setLastProcessedReportId(lastProcessedPosition.getReportId());
                dbPilotStatus.setLastProcessedDt(lastProcessedPosition.getDt());

                if (currFlight != null) {
                    DBFlight currDbFlight = loadedDbFlightsMap.get(currFlight.getFirstSeen().getReportId());
                    if (currDbFlight == null) {
                        throw new IllegalArgumentException("Unable to find currFlight in loaded DB flights");
                    }
                    dbPilotStatus.setCurrFlight(currDbFlight);
                } else {
                    dbPilotStatus.setCurrFlight(null);
                }

                session.update(dbPilotStatus);

            });

            return pilotContext.makeCopy(); // it resets dirty flags

        } finally {
            BM.stop();
        }
    }

    private DBPilotStatus loadPilotStatus(Session session, int pilotNumber) {
        BM.start("DBPersistenceLayer.loadPilotStatus");
        try {
            //noinspection JpaQlInspection
            return (DBPilotStatus) session
                    .createQuery("select ps from PilotStatus ps where pilotNumber = :pilotNumber")
                    .setInteger("pilotNumber", pilotNumber)
                    .uniqueResult();
        } finally {
            BM.stop();
        }
    }

    private DBLoadedPilotContext toPilotContext(Session session, int pilotNumber, DBPilotStatus dbPilotStatus) throws IOException {
        BM.start("DBPersistenceLayer.toPilotContext");
        try {

            DBLoadedPilotContext pilotContext = new DBLoadedPilotContext(pilotNumber);

            Long lastProcessedReportId = dbPilotStatus.getLastProcessedReportId();
            Report lastProcessedReport = null;
            if (lastProcessedReportId != null) {
                ReportPilotPosition lastProcessedReportPilotPosition = reportDatasource.loadPilotPosition(lastProcessedReportId, pilotNumber);
                lastProcessedReport = reportDatasource.loadReport(lastProcessedReportId);
                Position lastProcessedPosition = lastProcessedReportPilotPosition != null
                        ? Position.create(lastProcessedReportPilotPosition)
                        : Position.createOfflinePosition(lastProcessedReport);
                pilotContext.setLastProcessedPosition(lastProcessedPosition);
            }

            DBFlight dbCurrFlight = dbPilotStatus.getCurrFlight();
            if (dbCurrFlight != null) {
                Flight currFlight = fromDbFlight(dbCurrFlight, lastProcessedReport);
                pilotContext.setCurrFlight(currFlight);
            }

            return pilotContext;

        } finally {
            BM.stop();
        }
    }

    private Flight fromDbFlight(DBFlight dbFlight, Report lastProcessedReport) {
        BM.start("DBPersistenceLayer.fromDbFlight");
        try {
            int pilotNumber = dbFlight.getPilotNumber();

            List<Position> track = new LinkedList<>();
            Report firstSeenReport = reportDatasource.loadReport(dbFlight.getFirstSeenReportId());
            Report lastSeenReport = reportDatasource.loadReport(dbFlight.getLastSeenReportId());
            Report loadTillReport = lastProcessedReport.getReport().compareTo(lastSeenReport.getReport()) > 0
                    ? lastProcessedReport
                    : lastSeenReport;
            Report currReport = firstSeenReport;

            while (true) {  // todo loadPilotPositions(pilotNumber, fromReportId, toReportId)
                ReportPilotPosition reportPilotPosition = reportDatasource.loadPilotPosition(currReport.getId(), pilotNumber);
                Position position = reportPilotPosition != null
                        ? Position.create(reportPilotPosition)
                        : Position.createOfflinePosition(currReport);
                track.add(position);

                if (currReport.getReport().equals(loadTillReport.getReport())) {
                    break;
                }

                currReport = reportDatasource.loadNextReport(currReport.getReport());
                if (currReport == null) {
                    throw new IllegalStateException(); // todo message
                }

            }

            Flight flight = Flight.load(
                    pilotNumber,
                    FlightStatus.byCode(dbFlight.getStatus()),
                    dbFlight.getCallsign(),
                    Position.create(reportDatasource.loadPilotPosition(dbFlight.getFirstSeenReportId(), pilotNumber)),
                    Position.create(reportDatasource.loadPilotPosition(dbFlight.getLastSeenReportId(), pilotNumber)),
                    dbFlight.getTakeoffReportId() != null ? Position.create(reportDatasource.loadPilotPosition(dbFlight.getTakeoffReportId(), pilotNumber)) : null,
                    dbFlight.getLandingReportId() != null ? Position.create(reportDatasource.loadPilotPosition(dbFlight.getLandingReportId(), pilotNumber)) : null,
                    new Flightplan(dbFlight.getCallsign(), dbFlight.getAircraftType(), dbFlight.getRegNo(), dbFlight.getPlannedDeparture(), dbFlight.getPlannedDestination()),
                    track
            );

            return flight;
        } catch (IOException e) {
            logger.error("Error on converting DB flight", e);
            throw new RuntimeException("Error on converting DB flight", e);
        } finally {
            BM.stop();
        }
    }

    private void toDbFlight(DBFlight dbFlight, int pilotNumber, Flight flight) {
        BM.start("DBPersistenceLayer.toDbFlight");
        try {
            //dbFlight.setNetwork(null);
            dbFlight.setPilotNumber(pilotNumber);

            dbFlight.setCallsign(flight.getCallsign());

            Flightplan flightplan = flight.getFlightplan();
            if (flightplan != null) {
                dbFlight.setAircraftType(flightplan.getAircraftType());
                dbFlight.setRegNo(flightplan.getRegNo());
                dbFlight.setPlannedDeparture(flightplan.getDeparture());
                dbFlight.setPlannedDestination(flightplan.getDestination());
            } else {
                dbFlight.setAircraftType(null);
                dbFlight.setRegNo(null);
                dbFlight.setPlannedDeparture(null);
                dbFlight.setPlannedDestination(null);
            }

            dbFlight.setStatus(flight.getStatus().getCode());

            dbFlight.setFirstSeenReportId(flight.getFirstSeen().getReportId());
            dbFlight.setFirstSeenDt(flight.getFirstSeen().getDt());

            dbFlight.setLastSeenReportId(flight.getLastSeen().getReportId());
            dbFlight.setLastSeenDt(flight.getLastSeen().getDt());

            if (flight.getTakeoff() != null) {
                dbFlight.setTakeoffReportId(flight.getTakeoff().getReportId());
                dbFlight.setTakeoffDt(flight.getTakeoff().getDt());
                dbFlight.setTakeoffLatitude(flight.getTakeoff().getCoords().getLat());
                dbFlight.setTakeoffLongitude(flight.getTakeoff().getCoords().getLon());
                //dbFlight.setTakeoffType(null);
                dbFlight.setTakeoffIcao(flight.getTakeoff().getAirportIcao());
            } else {
                dbFlight.setTakeoffReportId(null);
                dbFlight.setTakeoffDt(null);
                dbFlight.setTakeoffLatitude(null);
                dbFlight.setTakeoffLongitude(null);
                dbFlight.setTakeoffType(null);
                dbFlight.setTakeoffIcao(null);
            }

            if (flight.getLanding() != null) {
                dbFlight.setLandingReportId(flight.getLanding().getReportId());
                dbFlight.setLandingDt(flight.getLanding().getDt());
                dbFlight.setLandingLatitude(flight.getLanding().getCoords().getLat());
                dbFlight.setLandingLongitude(flight.getLanding().getCoords().getLon());
                //dbFlight.setLandingType(null);
                dbFlight.setLandingIcao(flight.getLanding().getAirportIcao());
            } else {
                dbFlight.setLandingReportId(null);
                dbFlight.setLandingDt(null);
                dbFlight.setLandingLatitude(null);
                dbFlight.setLandingLongitude(null);
                dbFlight.setLandingType(null);
                dbFlight.setLandingIcao(null);
            }

            // todo dbFlight.setDistanceFlown(null);
            // todo dbFlight.setFlightTime(flight.getFlightTime().toMillis() / 3600000.0);
        } finally {
            BM.stop();
        }
    }

    private DBFlight loadCurrFlight(Session session, int pilotNumber) {
        BM.start("DBPersistenceLayer.loadCurrFlight");
        try {
            //noinspection unchecked,JpaQlInspection
            return (DBFlight) session
                    .createQuery("select f from Flight f where pilotNumber = :pilotNumber and status < :finishedCode")
                    .setInteger("pilotNumber", pilotNumber)
                    .setInteger("finishedCode", FlightStatus.Finished.getCode())
                    .uniqueResult();
        } finally {
            BM.stop();
        }
    }

    private List<DBFlight> loadDbFlightsByFirstSeenReportId(Session session, int pilotNumber, List<Flight> flights) {
        BM.start("DBPersistenceLayer.loadDbFlightsByFirstSeenReportId");
        try {
            //noinspection unchecked,JpaQlInspection
            return session
                    .createQuery("select f from Flight f where pilotNumber = :pilotNumber and firstSeenReportId in (:firstSeenReportIdList)")
                    .setInteger("pilotNumber", pilotNumber)
                    .setParameterList("firstSeenReportIdList", flights.stream().map(f -> f.getFirstSeen().getReportId()).collect(Collectors.toList()))
                    .list();
        } finally {
            BM.stop();
        }
    }

    /*private List<DBFlight> loadRecentPilotFlights(Session session, int pilotNumber, LocalDateTime lastSeenDt) {
        BM.start("DBPersistenceLayer.loadPilotStatus");
        try {
            //noinspection unchecked,JpaQlInspection
            return session
                    .createQuery("select f from Flight f where pilotNumber = :pilotNumber and firstSeenDt >= :threshold order by firstSeenDt")
                    .setInteger("pilotNumber", pilotNumber)
                    .setParameter("threshold", lastSeenDt.minusHours(PilotContext.RECENT_FLIGHTS_TIME_LIMIT_HOURS))
                    .list();
        } finally {
            BM.stop();
        }
    }*/


    private class DBLoadedPilotContext extends PilotContext {
        public DBLoadedPilotContext(int pilotNumber) {
            super(pilotNumber);
        }

        public void setLastProcessedPosition(Position lastProcessedPosition) {
            this.lastProcessedPosition = lastProcessedPosition;
        }

        public void setCurrFlight(Flight currFlight) {
            this.currFlight = currFlight;
        }
    }
}
