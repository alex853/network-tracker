package net.simforge.tracker.flights.model;

import net.simforge.tracker.flights.model.events.FlightStatusEvent;
import net.simforge.tracker.flights.model.events.FlightplanEvent;
import net.simforge.tracker.world.Position;

public class FlightOps {
    public static Flight create(PilotContext pilotContext) {
        return create(pilotContext, pilotContext.getPosition());
    }

    public static Flight create(PilotContext pilotContext, Position firstSeenPosition) {
        Flight flight = new Flight(pilotContext);

        flight.setStatus(firstSeenPosition.isOnGround() ? FlightStatus.Departure : FlightStatus.Flying);
        flight.setFirstSeen(firstSeenPosition);
        flight.setOrigin(firstSeenPosition);
        flight.setLastSeen(firstSeenPosition);

        processCriteria(flight, firstSeenPosition);

        collectFlightplan(pilotContext, flight);

        putMovementStatusEvent(pilotContext, flight);

        pilotContext.addFlight(flight);

        return flight;
    }

    public static void takeoff(PilotContext pilotContext, Flight flight) {
        Position position = pilotContext.getPosition();

        flight.setStatus(FlightStatus.Flying);
        flight.setLastSeen(position);

        processCriteria(flight, position);

        collectFlightplan(pilotContext, flight);

        putMovementStatusEvent(pilotContext, flight);
    }

    public static void landing(PilotContext pilotContext, Flight flight) {
        Position position = pilotContext.getPosition();

        flight.setStatus(FlightStatus.Arrival);
        flight.setDestination(position);
        flight.setLastSeen(position);

        processCriteria(flight, position);

        collectFlightplan(pilotContext, flight);

        putMovementStatusEvent(pilotContext, flight);
    }

    public static void continueFlight(PilotContext pilotContext, Flight flight) {
        Position position = pilotContext.getPosition();

        flight.setLastSeen(position);

        if (flight.getStatus().is(FlightStatus.Departure)
                && position.isPositionKnown()
                && position.isOnGround()) {
            flight.setOrigin(position);
        }

        processCriteria(flight, position);

        collectFlightplan(pilotContext, flight);
    }

    public static void lostFlight(PilotContext pilotContext, Flight flight) {
        flight.setStatus(FlightStatus.Lost);

        putMovementStatusEvent(pilotContext, flight);
    }

    public static void resumeLostFlight(PilotContext pilotContext, Flight flight) {
        Position position = pilotContext.getPosition();

        flight.setStatus(FlightStatus.Flying);
        flight.setLastSeen(position);

        processCriteria(flight, position);

        collectFlightplan(pilotContext, flight);

        putMovementStatusEvent(pilotContext, flight);
    }

    public static void finish(PilotContext pilotContext, Flight flight) {
        flight.setStatus(FlightStatus.Finished);

        putMovementStatusEvent(pilotContext, flight);
    }

    public static void terminate(PilotContext pilotContext, Flight flight) {
        flight.setStatus(FlightStatus.Terminated);

        putMovementStatusEvent(pilotContext, flight);
    }

    private static void processCriteria(Flight flight, Position position) {
        flight.getLostFlightEnduranceCriterion().process(position);
        flight.getTrackTrailCriterion().process(position);
    }

    private static void collectFlightplan(PilotContext pilotContext, Flight flight) {
        Position position = pilotContext.getPosition();
        Flightplan flightplan = Flightplan.fromPosition(position);
        if (flightplan != null) {
            if (!flightplan.equals(flight.getFlightplan())) {
                flight.setFlightplan(flightplan);

                pilotContext.putEvent(new FlightplanEvent(pilotContext, flight));
            }
        }
    }

    private static void putMovementStatusEvent(PilotContext pilotContext, Flight flight) {
        pilotContext.putEvent(new FlightStatusEvent(pilotContext, flight));
    }
}
