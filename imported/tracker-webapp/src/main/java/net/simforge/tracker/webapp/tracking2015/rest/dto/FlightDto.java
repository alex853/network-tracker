package net.simforge.tracker.webapp.tracking2015.rest.dto;

import net.simforge.tracker.flights.model.Flight;
import net.simforge.tracker.world.Position;

public class FlightDto {
    private String status;

    private String firstSeen;
    private String origin;
    private String destination;
    private String lastSeen;

    private String fpAircraft;
    private String fpOrigin;
    private String fpDestination;

    public static FlightDto create(Flight flight) {
        FlightDto result = new FlightDto();

        result.status = flight.getStatus().toString();

        result.firstSeen = positionToString(flight.getFirstSeen());
        result.origin = positionToString(flight.getOrigin());
        result.destination = positionToString(flight.getDestination());
        result.lastSeen = positionToString(flight.getLastSeen());

        result.fpAircraft = flight.getFlightplan() != null ? flight.getFlightplan().getAircraft() : null;
        result.fpOrigin = flight.getFlightplan() != null ? flight.getFlightplan().getOrigin() : null;
        result.fpDestination = flight.getFlightplan() != null ? flight.getFlightplan().getDestination() : null;

        return result;
    }

    private static String positionToString(Position position) {
        return position != null ? position.getStatus() + "     (" + position.getReportId() + ")" : null;
    }

    public String getStatus() {
        return status;
    }

    public String getFirstSeen() {
        return firstSeen;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public String getFpAircraft() {
        return fpAircraft;
    }

    public String getFpOrigin() {
        return fpOrigin;
    }

    public String getFpDestination() {
        return fpDestination;
    }
}
