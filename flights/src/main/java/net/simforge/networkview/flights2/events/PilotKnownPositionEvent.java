package net.simforge.networkview.flights2.events;

import net.simforge.networkview.flights2.PilotContext;
import net.simforge.networkview.flights2.Position;
import net.simforge.networkview.flights2.flight.Flight;
import net.simforge.networkview.flights2.flight.FlightStatus;

public class PilotKnownPositionEvent extends PilotEvent {

    public PilotKnownPositionEvent(PilotContext pilotContext) {
        super(pilotContext.getPilotNumber(), pilotContext.getLastProcessedReport(), "pilot/known");
    }

    static {
        TrackingEventHandler.registry.put(PilotKnownPositionEvent.class, (TrackingEventHandler<PilotKnownPositionEvent>) (delegate, event) -> {
            PilotContext pilotContext = delegate.getPilotContext();
            Position prevPosition = pilotContext.getPrevPosition();
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
                            /* todo double timeBetween = pilotContext.getMainContext().getTimeBetween(flight.getDestination().getReportId(), nextPosition.getReportId());
                            // todo increase time?
                            if (timeBetween >= TrackerUtil.duration(10, TrackerUtil.Minute)) {
                                FlightOps.finish(pilotContext, flight);

                                flight = FlightOps.create(pilotContext);
                            }*/
                        }

                        delegate.continueFlight(flight);
                    }
                }
            }
        });
    }
}
