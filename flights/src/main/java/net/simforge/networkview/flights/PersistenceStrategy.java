package net.simforge.networkview.flights;

import net.simforge.networkview.flights.model.Flight;
import net.simforge.networkview.flights.model.MainContext;
import net.simforge.networkview.flights.model.PilotContext;
import net.simforge.commons.hibernate.HibernateUtils;
import net.simforge.commons.legacy.BM;
import net.simforge.tracker.Network;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;

public class PersistenceStrategy implements MainContext.Strategy {
    private final Network network;
    private final SessionFactory sessionFactory;

    public PersistenceStrategy(Network network, SessionFactory sessionFactory) {
        this.network = network;
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void initPilotContext(PilotContext pilotContext, ReportPilotPosition pilotPosition) {

    }

    @Override
    public void onPilotContextProcessed(PilotContext pilotContext) {
        List<Flight> flights = pilotContext.getFlights();
        for (Flight flight : flights) {
            long firstSeenReportId = flight.getFirstSeen().getReportId();

            try (Session session = sessionFactory.openSession()) {
                HibernateUtils.transaction(session, () -> {
                    flights.persistence.Flight dbFlight = loadFlight(session, pilotContext.getPilotNumber(), firstSeenReportId);

                    if (dbFlight == null) {
                        saveFlight(session, flight);
                    } else {
                        updateFlight(session, flight, dbFlight);
                    }
                });
            }
        }
    }

    private flights.persistence.Flight loadFlight(Session session, int pilotNumber, long firstSeenReportId) {
        BM.start("PersistenceStrategy.loadFlight");
        try {

            //noinspection JpaQlInspection,UnnecessaryLocalVariable
            flights.persistence.Flight dbFlight = (flights.persistence.Flight) session
                    .createQuery("from Flight where network = :network and pilotNumber = :pilotNumber and firstSeenReportId = :firstSeenReportId")
                    .setInteger("network", network.getCode())
                    .setInteger("pilotNumber", pilotNumber)
                    .setInteger("firstSeenReportId", (int) firstSeenReportId)
                    .uniqueResult();

            return dbFlight;

        } finally {
            BM.stop();
        }
    }

    private void saveFlight(Session session, Flight flight) {
        BM.start("PersistenceStrategy.saveFlight");
        try {

            flights.persistence.Flight dbFlight = new flights.persistence.Flight();
            convert(flight, dbFlight);

            session.save(dbFlight);

        } finally {
            BM.stop();
        }
    }

    private void updateFlight(Session session, Flight flight, flights.persistence.Flight dbFlight) {
        BM.start("PersistenceStrategy.updateFlight");
        try {

            convert(flight, dbFlight);

            session.update(dbFlight);

        } finally {
            BM.stop();
        }
    }

    private void convert(Flight flight, flights.persistence.Flight dbFlight) {
        dbFlight.setNetwork(network);
        dbFlight.setPilotNumber(flight.getPilotContext().getPilotNumber());

        dbFlight.setCallsign("TODO"); // todo AK
        dbFlight.setAircraftType(flight.getFlightplan().getAircraft());
        dbFlight.setRegNo("TODO"); // todo AK
        dbFlight.setPlannedOrigin(flight.getFlightplan().getOrigin());
        dbFlight.setPlannedDestination(flight.getFlightplan().getDestination());

        dbFlight.setStatus(flight.getStatus().getCode());

        dbFlight.setFirstSeenReportId((int) flight.getFirstSeen().getReportId());
        dbFlight.setFirstSeenDt(flight.getFirstSeen().getDt());

        dbFlight.setLastSeenReportId((int) flight.getLastSeen().getReportId());
        dbFlight.setLastSeenDt(flight.getLastSeen().getDt());

        if (flight.getOrigin() != null) {
            dbFlight.setDepartureReportId((int) flight.getOrigin().getReportId());
            dbFlight.setDepartureDt(flight.getOrigin().getDt());
            dbFlight.setDepartureLatitude(flight.getOrigin().getCoords().getLat());
            dbFlight.setDepartureLongitude(flight.getOrigin().getCoords().getLon());
            // todo AK dbFlight.setOriginType(null);
            dbFlight.setOriginIcao(flight.getOrigin().getAirportIcao());
        } else {
            dbFlight.setDepartureReportId(null);
            dbFlight.setDepartureDt(null);
            dbFlight.setDepartureLatitude(null);
            dbFlight.setDepartureLongitude(null);
            dbFlight.setOriginType(null);
            dbFlight.setOriginIcao(null);
        }

        if (flight.getDestination() != null) {
            dbFlight.setArrivalReportId((int) flight.getDestination().getReportId());
            dbFlight.setArrivalDt(flight.getDestination().getDt());
            dbFlight.setArrivalLatitude(flight.getDestination().getCoords().getLat());
            dbFlight.setArrivalLongitude(flight.getDestination().getCoords().getLon());
            // todo AK dbFlight.setDestinationType(null);
            dbFlight.setDestinationIcao(flight.getDestination().getAirportIcao());
        } else {
            dbFlight.setArrivalReportId(null);
            dbFlight.setArrivalDt(null);
            dbFlight.setArrivalLatitude(null);
            dbFlight.setArrivalLongitude(null);
            dbFlight.setDestinationType(null);
            dbFlight.setDestinationIcao(null);
        }

        // todo AK dbFlight.setDistanceFlown(null);
        dbFlight.setFlightTime(flight.getFlightTime().toMillis() / 3600000.0);
    }
}
