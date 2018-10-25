package net.simforge.tracker.flights.model.events;

import net.simforge.tracker.flights.model.Flight;
import net.simforge.tracker.flights.model.FlightOps;
import net.simforge.tracker.flights.model.FlightStatus;
import net.simforge.tracker.flights.model.PilotContext;
import net.simforge.tracker.flights.model.criteria.Criterion;
import net.simforge.tracker.flights.model.criteria.EllipseCriterion;
import net.simforge.tracker.world.Position;

public class PilotOnlineEvent extends PilotEvent {
    public PilotOnlineEvent(int pilotNumber, long reportId) {
        super(pilotNumber, reportId, "pilot/online");
    }

    static {
        TrackingEventHandler.registry.put(PilotOnlineEvent.class, new EventHandler());
    }

    private static class EventHandler implements TrackingEventHandler {
        @Override
        public void process(PilotContext pilotContext, TrackingEvent event) {
            Position position = pilotContext.getPosition();

            // check if previous movement can be continued
            boolean createNew = true;
            Flight flight = pilotContext.getCurrentFlight();
            if (flight != null) {
                if (flight.getStatus() == FlightStatus.Lost) {
                    Criterion trackTrailCriterion = flight.getTrackTrailCriterion();

                    if (!position.isOnGround()
                            && (trackTrailCriterion.meets(position) || EllipseCriterion.get(flight).meets(position))) {
                        FlightOps.resumeLostFlight(pilotContext, flight);

                        createNew = false;
                    }
                }
            }

            if (createNew) {
                if (flight != null) {
                    FlightOps.terminate(pilotContext, flight);
                }

                FlightOps.create(pilotContext);
            }
        }
    }
}
