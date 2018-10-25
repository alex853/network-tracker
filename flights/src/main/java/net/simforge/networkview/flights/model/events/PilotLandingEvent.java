package net.simforge.networkview.flights.model.events;

import net.simforge.networkview.flights.model.Flight;
import net.simforge.networkview.flights.model.FlightOps;
import net.simforge.networkview.flights.model.FlightStatus;
import net.simforge.networkview.flights.model.PilotContext;

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
