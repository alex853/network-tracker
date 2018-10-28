package net.simforge.networkview.flights2.events;

import net.simforge.networkview.flights2.PilotContext;
import net.simforge.networkview.flights2.Position;

public class PilotOnlineEvent extends PilotEvent {
    public PilotOnlineEvent(int pilotNumber, String report) {
        super(pilotNumber, report, "pilot/online");
    }

    static {
        TrackingEventHandler.registry.put(PilotOnlineEvent.class, new EventHandler());
    }

    private static class EventHandler implements TrackingEventHandler<PilotOnlineEvent> {
        @Override
        public void process(PilotContext.ModificationsDelegate delegate, PilotOnlineEvent event) {
            PilotContext pilotContext = delegate.getPilotContext();
            Position position = pilotContext.getCurrPosition();

            // check if previous movement can be continued
            /* todo boolean createNew = true;
            Flight flight = pilotContext.getCurrFlight();
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
            }*/
        }
    }
}
