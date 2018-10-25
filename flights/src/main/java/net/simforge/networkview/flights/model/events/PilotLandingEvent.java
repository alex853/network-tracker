package flights.model.events;

import flights.model.Flight;
import flights.model.FlightOps;
import flights.model.FlightStatus;
import flights.model.PilotContext;

public class PilotLandingEvent extends PilotEvent {
    public PilotLandingEvent(int pilotNumber, long reportId) {
        super(pilotNumber, reportId, "pilot/landing");
    }

    static {
        TrackingEventHandler.registry.put(PilotLandingEvent.class, new EventHandler());
    }

    private static class EventHandler implements TrackingEventHandler {
        @Override
        public void process(PilotContext pilotContext, TrackingEvent event) {
            Flight flight = pilotContext.getCurrentFlight();

            if (!flight.getStatus().is(FlightStatus.Flying)) {
                throw new IllegalStateException("Landing is not suitable for flight in '" + flight.getStatus() + "' status");
            }

            FlightOps.landing(pilotContext, flight);
        }
    }
}
