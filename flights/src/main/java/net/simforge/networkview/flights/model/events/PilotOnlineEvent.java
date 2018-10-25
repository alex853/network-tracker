package net.simforge.networkview.flights.model.events;

import net.simforge.networkview.datafeeder.Position;
import net.simforge.networkview.flights.model.Flight;
import net.simforge.networkview.flights.model.FlightOps;
import net.simforge.networkview.flights.model.FlightStatus;
import net.simforge.networkview.flights.model.PilotContext;
import net.simforge.networkview.flights.model.criteria.Criterion;
import net.simforge.networkview.flights.model.criteria.EllipseCriterion;

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
