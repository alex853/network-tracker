package net.simforge.networkview.flights2.events;

import net.simforge.networkview.flights2.flight.Flight;
import net.simforge.networkview.flights2.PilotContext;

public class PilotOfflineEvent extends PilotEvent {
    public PilotOfflineEvent(int pilotNumber, String report) {
        super(pilotNumber, report, "pilot/offline");
    }

    static {
        TrackingEventHandler.registry.put(PilotOfflineEvent.class, new EventHandler());
    }

    private static class EventHandler implements TrackingEventHandler<PilotOfflineEvent> {
        @Override
        public void process(PilotContext.ModificationsDelegate delegate, PilotOfflineEvent event) {
            PilotContext pilotContext = delegate.getPilotContext();
            Flight flight = pilotContext.getCurrFlight();
            /* todo if (flight != null) {
                if (flight.getStatus() == FlightStatus.Arrival) {
                    FlightOps.finish(pilotContext, flight);
                } else {
                    if (flight.getStatus() == FlightStatus.Flying) {
                        FlightOps.lostFlight(pilotContext, flight);
                    } else {
                        FlightOps.terminate(pilotContext, flight);
                    }
                }
            }*/
        }
    }
}
