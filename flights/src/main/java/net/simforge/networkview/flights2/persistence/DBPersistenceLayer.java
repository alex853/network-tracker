package net.simforge.networkview.flights2.persistence;

import net.simforge.commons.hibernate.HibernateUtils;
import net.simforge.commons.legacy.BM;
import net.simforge.commons.misc.JavaTime;
import net.simforge.networkview.datafeeder.ReportUtils;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import net.simforge.networkview.flights.datasource.ReportDatasource;
import net.simforge.networkview.flights.model.Flightplan;
import net.simforge.networkview.flights2.PersistenceLayer;
import net.simforge.networkview.flights2.PilotContext;
import net.simforge.networkview.flights2.Position;
import net.simforge.networkview.flights2.flight.FlightDto;
import net.simforge.networkview.flights2.flight.FlightStatus;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class DBPersistenceLayer implements PersistenceLayer {
    private SessionFactory sessionFactory;
    private ReportDatasource reportDatasource;

    public DBPersistenceLayer(SessionFactory sessionFactory, ReportDatasource reportDatasource) {
        this.sessionFactory = sessionFactory;
        this.reportDatasource = reportDatasource;
    }

    @Override
    public PilotContext createContext(int pilotNumber) {
        BM.start("DBPersistenceLayer.createContext");
        try (Session session = sessionFactory.openSession()) {
            DBPilotStatus dbPilotStatus = loadPilotStatus(session, pilotNumber);

            if (dbPilotStatus != null) {
                throw new IllegalArgumentException("Pilot status for pilot " + pilotNumber + " already exists");
            }

            dbPilotStatus = new DBPilotStatus();
            dbPilotStatus.setPilotNumber(pilotNumber);

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

            Long lastSeenReportId = dbPilotStatus.getLastSeenReportId();
            ReportPilotPosition lastSeenReportPilotPosition = reportDatasource.loadPilotPosition(lastSeenReportId, pilotNumber);
            Position lastSeenPosition = Position.create(lastSeenReportPilotPosition);

            DBLoadedPilotContext pilotContext = new DBLoadedPilotContext(pilotNumber);
            pilotContext.setLastSeenPosition(lastSeenPosition);

            List<DBFlight> dbFlights = loadRecentPilotFlights(session, pilotNumber, lastSeenPosition.getDt());
            List<FlightDto> flights = dbFlights.stream().map(this::fromDbFlight).collect(toList());

            DBFlight dbCurrFlight = dbPilotStatus.getCurrFlight();
            if (dbCurrFlight != null) {
                FlightDto lastFlight = flights.get(flights.size() - 1);
                // todo Preconditions.checkArgument(lastFlight.getId() == dbCurrFlight.getId());
                flights.remove(flights.size() - 1);
                pilotContext.setCurrFlight(lastFlight);
            }
            pilotContext.setRecentFlights(flights);

            return pilotContext;
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
                List<FlightDto> recentFlights = (List) pilotContext.getRecentFlights();

                List<FlightDto> dirtyFlightsList = recentFlights.stream().filter(FlightDto::isDirty).collect(toList());
                FlightDto currFlight = (FlightDto) pilotContext.getCurrFlight();
                if (currFlight != null && currFlight.isDirty()) {
                    dirtyFlightsList.add(currFlight);
                }

                List<DBFlight> loadedDbFlights = loadDbFlightsByFirstSeenReportId(session, pilotContext.getPilotNumber(), dirtyFlightsList);
                Map<Long, DBFlight> loadedDbFlightsMap = loadedDbFlights.stream().collect(Collectors.toMap(DBFlight::getFirstSeenReportId, Function.identity()));

                Iterator<FlightDto> it = dirtyFlightsList.iterator();
                while (it.hasNext()) {
                    FlightDto flightDto = it.next();
                    DBFlight dbFlight = loadedDbFlightsMap.get(flightDto.getFirstSeen().getReportId());
                    if (dbFlight == null) {
                        continue; // it seems we have new flight
                    }
                    it.remove();

                    toDbFlight(dbFlight, pilotContext.getPilotNumber(), flightDto);

                    session.update(dbFlight);
                }

                // persist new flights to DB
                for (FlightDto flightDto : dirtyFlightsList) {
                    DBFlight dbFlight = new DBFlight();

                    toDbFlight(dbFlight, pilotContext.getPilotNumber(), flightDto);

                    session.save(dbFlight);

                    loadedDbFlightsMap.put(dbFlight.getFirstSeenReportId(), dbFlight);
                }

                Position lastSeenPosition = pilotContext.getLastSeenPosition();
                dbPilotStatus.setLastSeenReportId(lastSeenPosition.getReportId());
                dbPilotStatus.setLastSeenDt(lastSeenPosition.getDt());

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

    private List<DBFlight> loadDbFlightsByFirstSeenReportId(Session session, int pilotNumber, List<FlightDto> dirtyFlightDtos) {
        BM.start("DBPersistenceLayer.loadDbFlightsByFirstSeenReportId");
        try {
            //noinspection unchecked,JpaQlInspection
            return session
                    .createQuery("select f from Flight f where pilotNumber = :pilotNumber and firstSeenReportId in (:firstSeenReportIdList)")
                    .setInteger("pilotNumber", pilotNumber)
                    .setParameterList("firstSeenReportIdList", dirtyFlightDtos.stream().map(f -> f.getFirstSeen().getReportId()).collect(Collectors.toList()))
                    .list();
        } finally {
            BM.stop();
        }
    }

    private List<DBFlight> loadRecentPilotFlights(Session session, int pilotNumber, LocalDateTime lastSeenDt) {
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

    private FlightDto fromDbFlight(DBFlight dbFlight) {
        BM.start("DBPersistenceLayer.fromDbFlight");
        try {
            int pilotNumber = dbFlight.getPilotNumber();
            FlightDto flight = new FlightDto();
            flight.setStatus(FlightStatus.byCode(dbFlight.getStatus()));
            flight.setFirstSeen(Position.create(reportDatasource.loadPilotPosition(dbFlight.getFirstSeenReportId(), pilotNumber)));
            flight.setLastSeen(Position.create(reportDatasource.loadPilotPosition(dbFlight.getLastSeenReportId(), pilotNumber)));
            if (dbFlight.getDepartureReportId() != null) {
                flight.setOrigin(Position.create(reportDatasource.loadPilotPosition(dbFlight.getDepartureReportId(), pilotNumber)));
            }
            if (dbFlight.getArrivalReportId() != null) {
                flight.setDestination(Position.create(reportDatasource.loadPilotPosition(dbFlight.getArrivalReportId(), pilotNumber)));
            }
            Flightplan flightplan = new Flightplan(dbFlight.getAircraftType(), dbFlight.getPlannedOrigin(), dbFlight.getPlannedDestination());
            flight.setFlightplan(flightplan);
            return flight;
        } catch (IOException e) {
            // todo logger
            throw new RuntimeException("Error on converting DB flight into memory structure", e);
        } finally {
            BM.stop();
        }
    }

    private void toDbFlight(DBFlight dbFlight, int pilotNumber, FlightDto flight) {
        BM.start("DBPersistenceLayer.toDbFlight");
        try {
            dbFlight.setNetwork(null); // todo
            dbFlight.setPilotNumber(pilotNumber);

            dbFlight.setCallsign("TODO"); // todo AK
            dbFlight.setRegNo("TODO"); // todo AK
            Flightplan flightplan = flight.getFlightplan();
            if (flightplan != null) {
                dbFlight.setAircraftType(flightplan.getAircraft());
                dbFlight.setPlannedOrigin(flightplan.getOrigin());
                dbFlight.setPlannedDestination(flightplan.getDestination());
            } else {
                dbFlight.setAircraftType(null);
                dbFlight.setPlannedOrigin(null);
                dbFlight.setPlannedDestination(null);
            }

            dbFlight.setStatus(flight.getStatus().getCode());

            dbFlight.setFirstSeenReportId(flight.getFirstSeen().getReportId());
            dbFlight.setFirstSeenDt(flight.getFirstSeen().getDt());

            dbFlight.setLastSeenReportId(flight.getLastSeen().getReportId());
            dbFlight.setLastSeenDt(flight.getLastSeen().getDt());

            if (flight.getOrigin() != null) {
                dbFlight.setDepartureReportId(flight.getOrigin().getReportId());
                dbFlight.setDepartureDt(flight.getOrigin().getDt());
                dbFlight.setDepartureLatitude(flight.getOrigin().getCoords().getLat());
                dbFlight.setDepartureLongitude(flight.getOrigin().getCoords().getLon());
                // todo AK dbFlight.setOriginType(null);
                dbFlight.setDepartureIcao(flight.getOrigin().getAirportIcao());
            } else {
                dbFlight.setDepartureReportId(null);
                dbFlight.setDepartureDt(null);
                dbFlight.setDepartureLatitude(null);
                dbFlight.setDepartureLongitude(null);
                dbFlight.setDepartureType(null);
                dbFlight.setDepartureIcao(null);
            }

            if (flight.getDestination() != null) {
                dbFlight.setArrivalReportId(flight.getDestination().getReportId());
                dbFlight.setArrivalDt(flight.getDestination().getDt());
                dbFlight.setArrivalLatitude(flight.getDestination().getCoords().getLat());
                dbFlight.setArrivalLongitude(flight.getDestination().getCoords().getLon());
                // todo AK dbFlight.setDestinationType(null);
                dbFlight.setArrivalIcao(flight.getDestination().getAirportIcao());
            } else {
                dbFlight.setArrivalReportId(null);
                dbFlight.setArrivalDt(null);
                dbFlight.setArrivalLatitude(null);
                dbFlight.setArrivalLongitude(null);
                dbFlight.setArrivalType(null);
                dbFlight.setArrivalIcao(null);
            }

            // todo AK dbFlight.setDistanceFlown(null);
            // todo AK dbFlight.setFlightTime(flight.getFlightTime().toMillis() / 3600000.0);
        } finally {
            BM.stop();
        }
    }

    private class DBLoadedPilotContext extends PilotContext {
        public DBLoadedPilotContext(int pilotNumber) {
            super(pilotNumber);
        }

        public void setLastSeenPosition(Position lastSeenPosition) {
            this.lastSeenPosition = lastSeenPosition;
            this.currPosition = lastSeenPosition;
        }

        public void setCurrFlight(FlightDto currFlight) {
            this.currFlight = currFlight;
        }

        public void setRecentFlights(List<FlightDto> flights) {
            this.recentFlights = flights;
        }
    }
}
