package net.simforge.tracker.flights.model.events;

import net.simforge.tracker.flights.model.Flight;
import net.simforge.tracker.flights.model.FlightOps;
import net.simforge.tracker.flights.model.FlightStatus;
import net.simforge.tracker.flights.model.PilotContext;

public class PilotOfflineEvent extends PilotEvent {
    public PilotOfflineEvent(int pilotNumber, long reportId) {
        super(pilotNumber, reportId, "pilot/offline");
    }

    static {
        TrackingEventHandler.registry.put(PilotOfflineEvent.class, new EventHandler());
    }

    private static class EventHandler implements TrackingEventHandler {
        @Override
        public void process(PilotContext pilotContext, TrackingEvent event) {
            Flight flight = pilotContext.getCurrentFlight();
            if (flight != null) {
                if (flight.getStatus() == FlightStatus.Arrival) {
                    FlightOps.finish(pilotContext, flight);
                } else {
                    if (flight.getStatus() == FlightStatus.Flying) {
                        FlightOps.lostFlight(pilotContext, flight);
                    } else {
                        FlightOps.terminate(pilotContext, flight);
                    }
                }
            }
        }
    }
}
