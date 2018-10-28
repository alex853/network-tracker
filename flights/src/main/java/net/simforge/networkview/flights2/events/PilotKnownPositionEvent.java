package net.simforge.networkview.flights2.events;

import net.simforge.networkview.flights2.PilotContext;
import net.simforge.networkview.flights2.Position;

public class PilotKnownPositionEvent extends PilotEvent {

    public PilotKnownPositionEvent(PilotContext pilotContext) {
        super(pilotContext.getPilotNumber(), pilotContext.getLastProcessedReport(), "pilot/known");
    }

    static {
        TrackingEventHandler.registry.put(PilotKnownPositionEvent.class, new EventHandler());
    }

    private static class EventHandler implements TrackingEventHandler<PilotKnownPositionEvent> {
        @Override
        public void process(PilotContext.ModificationsDelegate delegate, PilotKnownPositionEvent event) {
            PilotContext pilotContext = delegate.getPilotContext();
            Position prevPosition = pilotContext.getPrevPosition();
            Position nextPosition = pilotContext.getCurrPosition();

            boolean hasEvents = false;
            if (!prevPosition.isPositionKnown()) {
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

            /*todo if (!hasEvents) {
                Flight flight = pilotContext.getCurrFlight();
                if (flight != null) {
                    if (OnGroundJumpCriterion.get(flight).meets(nextPosition)) {
                        // stop current flight
                        // start new flight

                        if (flight.getStatus().is(FlightStatus.Arrival)) {
                            FlightOps.finish(pilotContext, flight);
                        } else {
                            FlightOps.terminate(pilotContext, flight);
                        }

                        FlightOps.create(pilotContext);
                    } else {
                        // if flight is already for some time in Arrival status then finish the flight
                        if (flight.getStatus().is(FlightStatus.Arrival)) {
                            double timeBetween = pilotContext.getMainContext().getTimeBetween(flight.getDestination().getReportId(), nextPosition.getReportId());
                            if (timeBetween >= TrackerUtil.duration(10, TrackerUtil.Minute)) {
                                FlightOps.finish(pilotContext, flight);

                                flight = FlightOps.create(pilotContext);
                            }
                        }

                        FlightOps.continueFlight(pilotContext, flight);
                    }
                }
            }*/
        }
    }
}
