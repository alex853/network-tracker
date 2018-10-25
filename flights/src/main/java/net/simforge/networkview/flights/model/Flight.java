package flights.model;

import flights.model.criteria.TrackTrailCriterion;
import flights.model.criteria.LostFlightEnduranceCriterion;
import net.simforge.tracker.world.Position;

import java.time.Duration;

public class Flight {
    private PilotContext pilotContext;

    private FlightStatus status;

    private Position firstSeen;
    private Position origin;
    private Position destination;
    private Position lastSeen;

    private Flightplan flightplan;

    private LostFlightEnduranceCriterion lostFlightEnduranceCriterion = new LostFlightEnduranceCriterion(this);
    private TrackTrailCriterion trackTrailCriterion = new TrackTrailCriterion(this);

    public Flight(PilotContext pilotContext) {
        this.pilotContext = pilotContext;
    }

    public PilotContext getPilotContext() {
        return pilotContext;
    }

    public FlightStatus getStatus() {
        return status;
    }

    public void setStatus(FlightStatus status) {
        this.status = status;
    }

    public Position getFirstSeen() {
        return firstSeen;
    }

    public void setFirstSeen(Position firstSeen) {
        this.firstSeen = firstSeen;
    }

    public Position getOrigin() {
        return origin;
    }

    public String getOriginIcao() {
        return origin != null ? origin.getAirportIcao() : null;
    }

    public void setOrigin(Position origin) {
        this.origin = origin;
    }

    public Position getDestination() {
        return destination;
    }

    public String getDestinationIcao() {
        return destination != null ? destination.getAirportIcao() : null;
    }

    public void setDestination(Position destination) {
        this.destination = destination;
    }

    public Position getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Position lastSeen) {
        this.lastSeen = lastSeen;
    }

    public Flightplan getFlightplan() {
        return flightplan;
    }

    public void setFlightplan(Flightplan flightplan) {
        this.flightplan = flightplan;
    }

    public LostFlightEnduranceCriterion getLostFlightEnduranceCriterion() {
        return lostFlightEnduranceCriterion;
    }

    public TrackTrailCriterion getTrackTrailCriterion() {
        return trackTrailCriterion;
    }

    public Duration getFlightTime() {
        if (destination != null) {
            return Duration.between(origin.getDt(), destination.getDt());
        } else {
            return Duration.between(origin.getDt(), lastSeen.getDt());
        }
    }
}
