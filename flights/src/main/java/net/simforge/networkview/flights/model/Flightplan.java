package flights.model;

import net.simforge.tracker.world.Position;

public class Flightplan {
    private String aircraft;
    private String origin;
    private String destination;

    public Flightplan(String aircraft, String origin, String destination) {
        this.aircraft = aircraft;
        this.origin = origin;
        this.destination = destination;
    }

    public String getAircraft() {
        return aircraft;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public static Flightplan fromPosition(Position position) {
        if (position.hasFlightplan()) {
            return new Flightplan(position.getFpAircraftType(), position.getFpOrigin(), position.getFpDestination());
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Flightplan that = (Flightplan) o;

        if (aircraft != null ? !aircraft.equals(that.aircraft) : that.aircraft != null) return false;
        if (origin != null ? !origin.equals(that.origin) : that.origin != null) return false;
        if (destination != null ? !destination.equals(that.destination) : that.destination != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = aircraft != null ? aircraft.hashCode() : 0;
        result = 31 * result + (origin != null ? origin.hashCode() : 0);
        result = 31 * result + (destination != null ? destination.hashCode() : 0);
        return result;
    }
}
