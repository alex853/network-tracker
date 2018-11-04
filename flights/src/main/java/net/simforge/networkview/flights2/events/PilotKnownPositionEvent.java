package net.simforge.networkview.flights2.events;

import net.simforge.networkview.flights2.PilotContext;
import net.simforge.networkview.flights2.Position;
import net.simforge.networkview.flights2.flight.Flight;
import net.simforge.networkview.flights2.flight.FlightStatus;

public class PilotKnownPositionEvent extends PilotEvent {

    private Position prevPosition;

    public PilotKnownPositionEvent(PilotContext pilotContext, Position prevPosition) {
        super(pilotContext.getPilotNumber(), pilotContext.getCurrPosition().getReport(), "pilot/known");
        this.prevPosition = prevPosition;
    }

    static {
        TrackingEventHandler.registry.put(PilotKnownPositionEvent.class, (TrackingEventHandler<PilotKnownPositionEvent>) (delegate, event) -> {
            PilotContext pilotContext = delegate.getPilotContext();
            Position prevPosition = event.prevPosition;
            Position nextPosition = pilotContext.getCurrPosition();

            boolean hasEvents = false;
            if (prevPosition == null || !prevPosition.isPositionKnown()) {
                delegate.enqueueEvent(new PilotOnlineEvent(pilotContext.getPilotNumber(), nextPosition.getReport()));
                hasEvents = true;
            } else {
                if (prevPosition.isOnGround() && !nextPosition.isOnGround()) {
                    delegate.enqueueEvent(new PilotTakeoffEvent(pilotContext.getPilotNumber(), nextPosition.getReport()));
                    hasEvents = true;
                } else if (!prevPosition.isOnGround() && nextPosition.isOnGround()) {
                    delegate.enqueueEvent(new PilotLandingEvent(pilotContext.getPilotNumber(), nextPosition.getReport()));
                    hasEvents = true;
                }
            }

            if (!hasEvents) {
                Flight flight = pilotContext.getCurrFlight();
                if (flight != null) {
                    /* todo if (OnGroundJumpCriterion.get(flight).meets(nextPosition)) {
                        // stop current flight
                        // start new flight

                        if (flight.getStatus().is(FlightStatus.Arrival)) {
                            FlightOps.finish(pilotContext, flight);
                        } else {
                            FlightOps.terminate(pilotContext, flight);
                        }

                        FlightOps.create(pilotContext);
                    } else*/ {
                        // if flight is already for some time in Arrival status then finish the flight
                        if (flight.getStatus().is(FlightStatus.Arrival)) {
                            // todo callsign or flightplan change ? --> finish/start

                            // todo increase time?
                            if (flight.getDestination().getDt().isBefore(nextPosition.getDt().minusMinutes(10))) {
                                // todo add event 'finish/start due to time after arrival'
                                delegate.finishFlight(flight);

                                delegate.startFlight(nextPosition);
                                flight = pilotContext.getCurrFlight();
                            }
                        }

                        delegate.continueFlight(flight);
                    }
                }
            }
        });
    }
}
