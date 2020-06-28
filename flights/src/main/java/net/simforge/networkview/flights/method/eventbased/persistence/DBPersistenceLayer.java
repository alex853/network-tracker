package net.simforge.networkview.flights.method.eventbased.persistence;

import net.simforge.commons.hibernate.HibernateUtils;
import net.simforge.commons.legacy.BM;
import net.simforge.networkview.core.Position;
import net.simforge.networkview.core.report.persistence.Report;
import net.simforge.networkview.core.report.persistence.ReportPilotPosition;
import net.simforge.networkview.flights.method.eventbased.*;
import net.simforge.networkview.flights.method.eventbased.datasource.ReportDatasource;
import net.simforge.networkview.flights.method.eventbased.events.*;
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
import static java.util.stream.Collectors.toMap;

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
            //noinspection unchecked
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
/*        BM.start("DBPersistenceLayer.createContext");
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
        }*/
        throw new UnsupportedOperationException();
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
                if (dbPilotStatus == null) {
                    dbPilotStatus = new DBPilotStatus();
                    dbPilotStatus.setPilotNumber(pilotContext.getPilotNumber());
                }

                Position lastProcessedPosition = pilotContext.getLastProcessedPosition();
                dbPilotStatus.setLastProcessedReportId(lastProcessedPosition.getReportInfo().getId());
                dbPilotStatus.setLastProcessedDt(lastProcessedPosition.getReportInfo().getDt());

                if (dbPilotStatus.getId() == null) {
                    session.save(dbPilotStatus);
                }

                pilotContext.getRecentEvents().stream().map(e -> toDbEvent(e, null)).forEach(session::save);

                List<Flight> recentFlights = pilotContext.getRecentFlights();

                List<Flight> dirtyFlightsList = recentFlights.stream().filter(Flight::isDirty).collect(toList());
                Flight currFlight = pilotContext.getCurrFlight();
                if (currFlight != null) {
                    dirtyFlightsList.add(currFlight);
                }

                List<DBFlight> loadedDbFlights = !dirtyFlightsList.isEmpty()
                        ? loadDbFlightsByFirstSeenReportId(session, pilotContext.getPilotNumber(), dirtyFlightsList)
                        : Collections.emptyList();
                Map<Long, DBFlight> loadedDbFlightsMap = loadedDbFlights.stream().collect(Collectors.toMap(DBFlight::getFirstSeenReportId, Function.identity()));

                Iterator<Flight> it = dirtyFlightsList.iterator();
                while (it.hasNext()) {
                    Flight flight = it.next();

                    if (!flight.isDirty()) {
                        // we put currFlight to dirty list but if the currFlight is not changed there is no need to update it
                        it.remove();
                        continue;
                    }

                    DBFlight dbFlight = loadedDbFlightsMap.get(flight.getFirstSeen().getReportInfo().getId());
                    if (dbFlight == null) {
                        continue; // it seems we have new flight
                    }
                    it.remove();

                    toDbFlight(dbFlight, pilotContext.getPilotNumber(), flight);

                    session.update(dbFlight);

                    flight.getRecentEvents().stream().map(e -> toDbEvent(e, dbFlight)).forEach(session::save);
                }

                // persist new flights to DB
                for (Flight flight : dirtyFlightsList) {
                    DBFlight dbFlight = new DBFlight();

                    toDbFlight(dbFlight, pilotContext.getPilotNumber(), flight);

                    session.save(dbFlight);

                    flight.getRecentEvents().stream().map(e -> toDbEvent(e, dbFlight)).forEach(session::save);

                    loadedDbFlightsMap.put(dbFlight.getFirstSeenReportId(), dbFlight);
                }

                if (currFlight != null) {
                    DBFlight currDbFlight = loadedDbFlightsMap.get(currFlight.getFirstSeen().getReportInfo().getId());
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

            long fromReportId = firstSeenReport.getId();
            long toReportId = loadTillReport.getId();

            List<Report> reports = reportDatasource.loadReports(fromReportId, toReportId);
            List<ReportPilotPosition> reportPilotPositions = reportDatasource.loadPilotPositions(pilotNumber, fromReportId, toReportId);
            Map<Long, ReportPilotPosition> reportPilotPositionMap = reportPilotPositions.stream().collect(toMap(p -> p.getReport().getId(), Function.identity()));

            for (Report report : reports) {
                ReportPilotPosition reportPilotPosition = reportPilotPositionMap.get(report.getId());
                Position position = reportPilotPosition != null
                        ? Position.create(reportPilotPosition)
                        : Position.createOfflinePosition(report);
                track.add(position);
            }

            //noinspection UnnecessaryLocalVariable
            Flight flight = Flight.load(
                    pilotNumber,
                    FlightStatus.byCode(dbFlight.getStatus()),
                    dbFlight.getCallsign(),
                    Position.create(reportDatasource.loadPilotPosition(dbFlight.getFirstSeenReportId(), pilotNumber)),
                    Position.create(reportDatasource.loadPilotPosition(dbFlight.getLastSeenReportId(), pilotNumber)),
                    dbFlight.getTakeoffReportId() != null ? Position.create(reportDatasource.loadPilotPosition(dbFlight.getTakeoffReportId(), pilotNumber)) : null,
                    dbFlight.getLandingReportId() != null ? Position.create(reportDatasource.loadPilotPosition(dbFlight.getLandingReportId(), pilotNumber)) : null,
                    new Flightplan(dbFlight.getAircraftType(), dbFlight.getRegNo(), dbFlight.getPlannedDeparture(), dbFlight.getPlannedDestination()),
                    dbFlight.getDistanceFlown(),
                    dbFlight.getFlightTime(),
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

            dbFlight.setFirstSeenReportId(flight.getFirstSeen().getReportInfo().getId());
            dbFlight.setFirstSeenDt(flight.getFirstSeen().getReportInfo().getDt());
            dbFlight.setFirstSeenLatitude(flight.getFirstSeen().getCoords().getLat());
            dbFlight.setFirstSeenLongitude(flight.getFirstSeen().getCoords().getLon());
            dbFlight.setFirstSeenIcao(flight.getFirstSeen().getAirportIcao());

            dbFlight.setLastSeenReportId(flight.getLastSeen().getReportInfo().getId());
            dbFlight.setLastSeenDt(flight.getLastSeen().getReportInfo().getDt());
            dbFlight.setLastSeenLatitude(flight.getLastSeen().getCoords().getLat());
            dbFlight.setLastSeenLongitude(flight.getLastSeen().getCoords().getLon());
            dbFlight.setLastSeenIcao(flight.getLastSeen().getAirportIcao());

            if (flight.getTakeoff() != null) {
                dbFlight.setTakeoffReportId(flight.getTakeoff().getReportInfo().getId());
                dbFlight.setTakeoffDt(flight.getTakeoff().getReportInfo().getDt());
                dbFlight.setTakeoffLatitude(flight.getTakeoff().getCoords().getLat());
                dbFlight.setTakeoffLongitude(flight.getTakeoff().getCoords().getLon());
                dbFlight.setTakeoffIcao(flight.getTakeoff().getAirportIcao());
            } else {
                dbFlight.setTakeoffReportId(null);
                dbFlight.setTakeoffDt(null);
                dbFlight.setTakeoffLatitude(null);
                dbFlight.setTakeoffLongitude(null);
                dbFlight.setTakeoffIcao(null);
            }

            if (flight.getLanding() != null) {
                dbFlight.setLandingReportId(flight.getLanding().getReportInfo().getId());
                dbFlight.setLandingDt(flight.getLanding().getReportInfo().getDt());
                dbFlight.setLandingLatitude(flight.getLanding().getCoords().getLat());
                dbFlight.setLandingLongitude(flight.getLanding().getCoords().getLon());
                dbFlight.setLandingIcao(flight.getLanding().getAirportIcao());
            } else {
                dbFlight.setLandingReportId(null);
                dbFlight.setLandingDt(null);
                dbFlight.setLandingLatitude(null);
                dbFlight.setLandingLongitude(null);
                dbFlight.setLandingIcao(null);
            }

            dbFlight.setDistanceFlown(flight.getDistanceFlown());
            dbFlight.setFlightTime(flight.getFlightTime());
        } finally {
            BM.stop();
        }
    }

    private DBEvent toDbEvent(TrackingEvent event, DBFlight dbFlight) {
        DBEvent dbEvent = new DBEvent();
        dbEvent.setPilotNumber(event.getPilotNumber());
        dbEvent.setReportId(event.getReportInfo().getId());
        dbEvent.setDt(event.getReportInfo().getDt());
        dbEvent.setType(mapEventType(event));
        dbEvent.setFlight(dbFlight);
        return dbEvent;
    }

    private int mapEventType(TrackingEvent event) {
        if (event instanceof PilotOnlineEvent) {
            return 1;
        } else if (event instanceof PilotOfflineEvent) {
            return 2;
        } else if (event instanceof PilotTakeoffEvent) {
            return 11;
        } else if (event instanceof PilotLandingEvent) {
            return 19;
        } else if (event instanceof FlightStatusEvent) {
            return ((FlightStatusEvent) event).getStatus().getCode();
        } else if (event instanceof FlightplanEvent) {
            return 99;
        } else {
            throw new IllegalArgumentException("Unknown type of event " + event);
        }
    }

    private DBFlight loadCurrFlight(Session session, int pilotNumber) {
        BM.start("DBPersistenceLayer.loadCurrFlight");
        try {
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
            //noinspection unchecked
            return session
                    .createQuery("select f from Flight f where pilotNumber = :pilotNumber and firstSeenReportId in (:firstSeenReportIdList)")
                    .setInteger("pilotNumber", pilotNumber)
                    .setParameterList("firstSeenReportIdList", flights.stream().map(f -> f.getFirstSeen().getReportInfo().getId()).collect(Collectors.toList()))
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
        DBLoadedPilotContext(int pilotNumber) {
            super(pilotNumber);
        }

        void setLastProcessedPosition(Position lastProcessedPosition) {
            this.lastProcessedPosition = lastProcessedPosition;
        }

        void setCurrFlight(Flight currFlight) {
            this.currFlight = currFlight;
        }
    }
}
