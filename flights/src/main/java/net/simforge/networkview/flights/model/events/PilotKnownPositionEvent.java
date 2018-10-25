package flights.model.events;

import flights.model.Flight;
import flights.model.FlightOps;
import flights.model.criteria.OnGroundJumpCriterion;
import net.simforge.tracker.TrackerUtil;
import flights.model.FlightStatus;
import flights.model.PilotContext;
import net.simforge.tracker.world.Position;

public class PilotKnownPositionEvent extends PilotEvent {
    private Position prevPosition;

    public PilotKnownPositionEvent(PilotContext pilotContext, Position prevPosition) {
        super(pilotContext.getPilotNumber(), pilotContext.getPosition().getReportId(), "pilot/known");
        this.prevPosition = prevPosition;
    }

    static {
        TrackingEventHandler.registry.put(PilotKnownPositionEvent.class, new EventHandler());
    }

    private static class EventHandler implements TrackingEventHandler {
        @Override
        public void process(PilotContext pilotContext, TrackingEvent event) {
            PilotKnownPositionEvent _event = (PilotKnownPositionEvent) event;
            Position prevPosition = _event.prevPosition;
            Position nextPosition = pilotContext.getPosition();

            boolean hasEvents = false;
            if (!prevPosition.isPositionKnown()) {
                pilotContext.putEvent(new PilotOnlineEvent(pilotContext.getPilotNumber(), nextPosition.getReportId()));
                hasEvents = true;
            } else {
                if (prevPosition.isOnGround() && !nextPosition.isOnGround()) {
                    pilotContext.putEvent(new PilotTakeoffEvent(pilotContext.getPilotNumber(), nextPosition.getReportId()));
                    hasEvents = true;
                } else if (!prevPosition.isOnGround() && nextPosition.isOnGround()) {
                    pilotContext.putEvent(new PilotLandingEvent(pilotContext.getPilotNumber(), nextPosition.getReportId()));
                    hasEvents = true;
                }
            }

            if (!hasEvents) {
                Flight flight = pilotContext.getCurrentFlight();
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
            }
        }
    }
}
