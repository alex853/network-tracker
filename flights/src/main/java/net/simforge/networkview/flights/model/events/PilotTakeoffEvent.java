package flights.model.events;

import flights.model.Flight;
import flights.model.FlightOps;
import flights.model.FlightStatus;
import flights.model.PilotContext;

public class PilotTakeoffEvent extends PilotEvent {
    public PilotTakeoffEvent(int pilotNumber, long reportId) {
        super(pilotNumber, reportId, "pilot/takeoff");
    }

    static {
        TrackingEventHandler.registry.put(PilotTakeoffEvent.class, new EventHandler());
    }

    private static class EventHandler implements TrackingEventHandler {
        @Override
        public void process(PilotContext pilotContext, TrackingEvent event) {
            Flight flight = pilotContext.getCurrentFlight();

            if (flight.getStatus().is(FlightStatus.Arrival)) {
                FlightOps.finish(pilotContext, flight);
                flight = FlightOps.create(pilotContext, flight.getLastSeen());
            }

            if (!flight.getStatus().is(FlightStatus.Departure)) {
                throw new IllegalStateException("Takeoff is not suitable for flight in '" + flight.getStatus() + "' status");
            }

            FlightOps.takeoff(pilotContext, flight);
        }
    }
}
