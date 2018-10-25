package net.simforge.tracker.flights.model.criteria;

import net.simforge.commons.misc.Geo;
import net.simforge.commons.misc.Misc;
import net.simforge.tracker.flights.model.Flight;
import net.simforge.tracker.world.Position;

public class OnGroundJumpCriterion implements Criterion {
    private Flight flight;

    private OnGroundJumpCriterion(Flight flight) {
        this.flight = flight;
    }

    @Override
    public void process(Position position) {
        // no/op
    }

    @Override
    public boolean meets(Position position) {
        Position lastSeen = flight.getLastSeen();

        if (!lastSeen.isPositionKnown() || !position.isPositionKnown()) {
            return false;
        }

        if (!lastSeen.isOnGround() || !position.isOnGround()) {
            return false;
        }

        if (!Misc.equal(lastSeen.getAirportIcao(), position.getAirportIcao())) {
            return true;
        }

        double distance = Geo.distance(lastSeen.getCoords(), position.getCoords());

        return distance > 5.0;
    }

    public static Criterion get(Flight flight) {
        return new OnGroundJumpCriterion(flight);
    }
}
