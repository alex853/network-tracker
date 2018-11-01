package net.simforge.networkview.flights2.flight;

import net.simforge.networkview.flights.model.Flightplan;
import net.simforge.networkview.flights2.Position;

public class FlightDto implements Flight {
    private FlightStatus status;

    private Position firstSeen;
    private Position origin;
    private Position destination;
    private Position lastSeen;

    private Flightplan flightplan;

    private boolean isDirty;

    @Override
    public FlightStatus getStatus() {
        return status;
    }

    public void setStatus(FlightStatus status) {
        setDirty();
        this.status = status;
    }

    @Override
    public Position getFirstSeen() {
        return firstSeen;
    }

    public void setFirstSeen(Position firstSeen) {
        setDirty();
        this.firstSeen = firstSeen;
    }

    @Override
    public Position getOrigin() {
        return origin;
    }

    public void setOrigin(Position origin) {
        setDirty();
        this.origin = origin;
    }

    @Override
    public Position getDestination() {
        return destination;
    }

    public void setDestination(Position destination) {
        setDirty();
        this.destination = destination;
    }

    @Override
    public Position getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Position lastSeen) {
        setDirty();
        this.lastSeen = lastSeen;
    }

    @Override
    public Flightplan getFlightplan() {
        return flightplan;
    }

    public void setFlightplan(Flightplan flightplan) {
        setDirty();
        this.flightplan = flightplan;
    }

    public boolean isDirty() {
        return isDirty;
    }

    private void setDirty() {
        isDirty = true;
    }

    public FlightDto makeCopy() {
        FlightDto copy = new FlightDto();
        copy.status = status;
        copy.firstSeen = firstSeen;
        copy.origin = origin;
        copy.destination = destination;
        copy.lastSeen = lastSeen;
        copy.flightplan = flightplan;
        copy.isDirty = false;
        return copy;
    }
}
