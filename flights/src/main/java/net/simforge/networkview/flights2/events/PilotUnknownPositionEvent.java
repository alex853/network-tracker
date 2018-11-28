package net.simforge.networkview.flights2.events;

import net.simforge.networkview.flights2.PilotContext;
import net.simforge.networkview.flights2.Position;

@Deprecated
public class PilotUnknownPositionEvent extends PilotEvent {

    private Position prevPosition;

    public PilotUnknownPositionEvent(PilotContext pilotContext, Position prevPosition) {
        super(pilotContext.getPilotNumber(), pilotContext.getCurrPosition().getReport(), "pilot/unknown");
        this.prevPosition = prevPosition;
    }

    static {
        TrackingEventHandler.registry.put(PilotUnknownPositionEvent.class, (TrackingEventHandler<PilotUnknownPositionEvent>) (delegate, event) -> {
            PilotContext pilotContext = delegate.getPilotContext();
            Position prevPosition = event.prevPosition;
            Position nextPosition = pilotContext.getCurrPosition();

            boolean hasEvents = false;
            if (prevPosition.isPositionKnown()) {
                delegate.enqueueEvent(new PilotOfflineEvent(pilotContext.getPilotNumber(), nextPosition.getReport()));
                hasEvents = true;
            }

            /*todo if (!hasEvents) {
                Flight flight = pilotContext.getCurrFlight();
                if (flight != null) {
                    if (flight.getStatus() == FlightStatus.Lost) {
                        Criterion lostFlightEnduranceCriterion = flight.getLostFlightEnduranceCriterion();

                        if (!lostFlightEnduranceCriterion.meets(nextPosition)) {
                            FlightOps.terminate(pilotContext, flight);
                        }
                    }
                }
            }*/
        });
    }
}
