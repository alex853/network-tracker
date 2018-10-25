package flights.model.events;

import flights.model.Flight;
import flights.model.FlightOps;
import flights.model.FlightStatus;
import flights.model.PilotContext;
import flights.model.criteria.Criterion;
import net.simforge.tracker.world.Position;

public class PilotUnknownPositionEvent extends PilotEvent {
    private Position prevPosition;

    public PilotUnknownPositionEvent(PilotContext pilotContext, Position prevPosition) {
        super(pilotContext.getPilotNumber(), pilotContext.getPosition().getReportId(), "pilot/unknown");
        this.prevPosition = prevPosition;
    }

    static {
        TrackingEventHandler.registry.put(PilotUnknownPositionEvent.class, new EventHandler());
    }

    private static class EventHandler implements TrackingEventHandler {
        @Override
        public void process(PilotContext pilotContext, TrackingEvent event) {
            PilotUnknownPositionEvent _event = (PilotUnknownPositionEvent) event;
            Position prevPosition = _event.prevPosition;
            Position nextPosition = pilotContext.getPosition();

            boolean hasEvents = false;
            if (prevPosition.isPositionKnown()) {
                pilotContext.putEvent(new PilotOfflineEvent(pilotContext.getPilotNumber(), nextPosition.getReportId()));
                hasEvents = true;
            }

            if (!hasEvents) {
                Flight movement = pilotContext.getCurrentFlight();
                if (movement != null) {
                    if (movement.getStatus() == FlightStatus.Lost) {
                        Criterion lostFlightEnduranceCriterion = movement.getLostFlightEnduranceCriterion();

                        if (!lostFlightEnduranceCriterion.meets(nextPosition)) {
                            FlightOps.terminate(pilotContext, movement);
                        }
                    }
                }
            }
        }
    }
}
