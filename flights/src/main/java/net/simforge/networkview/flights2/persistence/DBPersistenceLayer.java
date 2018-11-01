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
import net.simforge.networkview.flights2.flight.Flight;
import net.simforge.networkview.flights2.flight.FlightDto;
import net.simforge.networkview.flights2.flight.FlightStatus;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class DBPersistenceLayer implements PersistenceLayer {
    private SessionFactory sessionFactory; // todo
    private ReportDatasource reportDatasource; // todo

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

            List<DBFlight> dbFlights = loadRecentPilotFlights(session, pilotNumber);
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
            DBPilotStatus dbPilotStatus = loadPilotStatus(session, pilotContext.getPilotNumber());

            //noinspection unchecked
            List<FlightDto> recentFlights = (List) pilotContext.getRecentFlights();
            Map<FlightDto, DBFlight> dirtyFlights = recentFlights.stream().filter(FlightDto::isDirty).collect(Collectors.toMap(flight -> flight, this::toDbFlight));

            FlightDto currFlight = (FlightDto) pilotContext.getCurrFlight();
            if (currFlight != null && currFlight.isDirty()) {
                dirtyFlights.put(currFlight, toDbFlight(currFlight));
            }

            Position lastSeenPosition = pilotContext.getLastSeenPosition();
            dbPilotStatus.setLastSeenReportId(lastSeenPosition.getReportId());
            dbPilotStatus.setLastSeenDt(ReportUtils.fromTimestampJava(lastSeenPosition.getReport()));

            return null;
        } finally {
            BM.stop();
        }
    }

    private List<DBFlight> loadRecentPilotFlights(Session session, int pilotNumber) {
        BM.start("DBPersistenceLayer.loadPilotStatus");
        try {
            //noinspection unchecked,JpaQlInspection
            return session
                    .createQuery("select f from Flight f where pilotNumber = :pilotNumber and firstSeenDt >= :threshold order by firstSeenDt")
                    .setInteger("pilotNumber", pilotNumber)
                    .setParameter("threshold", JavaTime.nowUtc().minusHours(PilotContext.RECENT_FLIGHTS_TIME_LIMIT_HOURS))
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
        BM.start("DBPersistenceLayer.loadPilotStatus");
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

    private DBFlight toDbFlight(FlightDto flight) {
        throw new UnsupportedOperationException("DBPersistenceLayer.fromDbFlight");
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
