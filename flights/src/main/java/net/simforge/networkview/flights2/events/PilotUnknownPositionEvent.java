package net.simforge.networkview.flights2.events;

import net.simforge.networkview.flights2.PilotContext;
import net.simforge.networkview.flights2.Position;

public class PilotUnknownPositionEvent extends PilotEvent {

    public PilotUnknownPositionEvent(PilotContext pilotContext) {
        super(pilotContext.getPilotNumber(), pilotContext.getLastProcessedReport(), "pilot/unknown");
    }

    static {
        TrackingEventHandler.registry.put(PilotUnknownPositionEvent.class, (TrackingEventHandler<PilotUnknownPositionEvent>) (delegate, event) -> {
            PilotContext pilotContext = delegate.getPilotContext();
            Position prevPosition = pilotContext.getPrevPosition();
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
