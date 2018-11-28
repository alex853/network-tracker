package net.simforge.networkview.flights2.events;

import net.simforge.networkview.flights.FlightStatus;
import net.simforge.networkview.flights.Position;
import net.simforge.networkview.flights2.PilotContext;
import net.simforge.networkview.flights2.criteria.Criterion;
import net.simforge.networkview.flights2.criteria.EllipseCriterion;
import net.simforge.networkview.flights2.flight.Flight;

@Deprecated
public class PilotOnlineEvent extends PilotEvent {
    public PilotOnlineEvent(int pilotNumber, String report) {
        super(pilotNumber, report, "pilot/online");
    }

    static {
        TrackingEventHandler.registry.put(PilotOnlineEvent.class, (TrackingEventHandler<PilotOnlineEvent>) (delegate, event) -> {
            PilotContext pilotContext = delegate.getPilotContext();
            Position position = pilotContext.getCurrPosition();

            // check if previous flight can be continued
            boolean createNew = true;
            Flight flight = pilotContext.getCurrFlight();
            if (flight != null) {
                if (flight.getStatus() == FlightStatus.Lost) {
                    Criterion trackTrailCriterion = pilotContext.getCurrFlightTrackTrailCriterion();
                    boolean trackTrailCriterionOk = trackTrailCriterion.meets(position);
                    boolean ellipseCriterionOk = EllipseCriterion.get(flight).meets(position);

                    if (flight.getLastSeen().isOnGround() && !position.isOnGround()) {
                        // check for takeoff
                        throw new UnsupportedOperationException("PilotOnlineEvent#check for takeoff");
                    } else if (!flight.getLastSeen().isOnGround() && position.isOnGround()) {
                        if (trackTrailCriterionOk || ellipseCriterionOk) {
                            // todo add event 'flight resumed because of ellipse or track trail criterion'
                            delegate.landing(flight);

                            createNew = false;
                        }
                    } else {

                        if (!position.isOnGround() && (trackTrailCriterionOk || ellipseCriterionOk)) {
                            delegate.resumeLostFlight(flight);
                            // todo add event 'flight resumed because of ellipse or track trail criterion'

                            createNew = false;
                        }
                    }
                }
            }

            if (createNew) {
                if (flight != null) {
                    delegate.terminateFlight(flight);
                }

                delegate.startFlight(position);
            }
        });
    }
}
