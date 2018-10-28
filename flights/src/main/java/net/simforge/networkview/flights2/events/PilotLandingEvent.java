package net.simforge.networkview.flights2.events;

import net.simforge.networkview.flights2.flight.Flight;
import net.simforge.networkview.flights2.PilotContext;

public class PilotLandingEvent extends PilotEvent {
    public PilotLandingEvent(int pilotNumber, String report) {
        super(pilotNumber, report, "pilot/landing");
    }

    static {
        TrackingEventHandler.registry.put(PilotLandingEvent.class, new EventHandler());
    }

    private static class EventHandler implements TrackingEventHandler<PilotLandingEvent> {
        @Override
        public void process(PilotContext.ModificationsDelegate delegate, PilotLandingEvent event) {
            PilotContext pilotContext = delegate.getPilotContext();
            Flight flight = pilotContext.getCurrFlight();

            /* todo if (!flight.getStatus().is(FlightStatus.Flying)) {
                throw new IllegalStateException("Landing is not suitable for flight in '" + flight.getStatus() + "' status");
            }

            FlightOps.landing(pilotContext, flight);*/
        }
    }
}
