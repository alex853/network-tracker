package net.simforge.networkview.flights2.events;

import net.simforge.networkview.flights2.flight.Flight;
import net.simforge.networkview.flights2.PilotContext;

public class PilotTakeoffEvent extends PilotEvent {
    public PilotTakeoffEvent(int pilotNumber, String report) {
        super(pilotNumber, report, "pilot/takeoff");
    }

    static {
        TrackingEventHandler.registry.put(PilotTakeoffEvent.class, new EventHandler());
    }

    private static class EventHandler implements TrackingEventHandler<PilotTakeoffEvent> {
        @Override
        public void process(PilotContext.ModificationsDelegate delegate, PilotTakeoffEvent event) {
            PilotContext pilotContext = delegate.getPilotContext();
            Flight flight = pilotContext.getCurrFlight();

            /* todo if (flight.getStatus().is(FlightStatus.Arrival)) {
                FlightOps.finish(pilotContext, flight);
                flight = FlightOps.create(pilotContext, flight.getLastSeen());
            }

            if (!flight.getStatus().is(FlightStatus.Departure)) {
                throw new IllegalStateException("Takeoff is not suitable for flight in '" + flight.getStatus() + "' status");
            }

            FlightOps.takeoff(pilotContext, flight);*/
        }
    }
}
