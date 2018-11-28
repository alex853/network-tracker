package net.simforge.networkview.flights2.flight;

import net.simforge.networkview.flights2.Position;

public class Flightplan {
    private String callsign;
    private String aircraftType;
    private String regNo;
    private String departure;
    private String destination;

    public Flightplan(String callsign, String aircraftType, String regNo, String departure, String destination) {
        this.callsign = callsign;
        this.aircraftType = aircraftType;
        this.regNo = regNo;
        this.departure = departure;
        this.destination = destination;
    }

    @Deprecated
    public String getCallsign() {
        return callsign;
    }

    public String getAircraftType() {
        return aircraftType;
    }

    public String getRegNo() {
        return regNo;
    }

    public String getDeparture() {
        return departure;
    }

    public String getDestination() {
        return destination;
    }

    public static Flightplan fromPosition(Position position) {
        if (position.hasFlightplan()) {
            return new Flightplan(position.getCallsign(), position.getFpAircraftType(), position.getRegNo(), position.getFpDeparture(), position.getFpDestination());
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Flightplan that = (Flightplan) o;

        if (callsign != null ? !callsign.equals(that.callsign) : that.callsign != null) return false;
        if (aircraftType != null ? !aircraftType.equals(that.aircraftType) : that.aircraftType != null) return false;
        if (regNo != null ? !regNo.equals(that.regNo) : that.regNo != null) return false;
        if (departure != null ? !departure.equals(that.departure) : that.departure != null) return false;
        if (destination != null ? !destination.equals(that.destination) : that.destination != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = callsign != null ? callsign.hashCode() : 0;
        result = 31 * result + (aircraftType != null ? aircraftType.hashCode() : 0);
        result = 31 * result + (regNo != null ? regNo.hashCode() : 0);
        result = 31 * result + (departure != null ? departure.hashCode() : 0);
        result = 31 * result + (destination != null ? destination.hashCode() : 0);
        return result;
    }
}
