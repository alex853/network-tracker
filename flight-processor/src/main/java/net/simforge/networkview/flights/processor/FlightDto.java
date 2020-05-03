package net.simforge.networkview.flights.processor;

import net.simforge.networkview.flights.Flight;
import net.simforge.networkview.flights.FlightStatus;
import net.simforge.networkview.flights.Flightplan;
import net.simforge.networkview.flights.Position;

public class FlightDto {
    private int pilotNumber;
    private FlightStatus status;
    private String callsign;
    private Position firstSeen;
    private Position lastSeen;
    private Position takeoff;
    private Position landing;
    private Flightplan flightplan;
    private Double distanceFlown;
    private Double flightTime;

    public FlightDto(Flight flight) {
        this.pilotNumber = flight.getPilotNumber();
        this.status = flight.getStatus();
        this.callsign = flight.getCallsign();
        this.firstSeen = flight.getFirstSeen();
        this.lastSeen = flight.getLastSeen();
        this.takeoff = flight.getTakeoff();
        this.landing = flight.getLanding();
        this.flightplan = flight.getFlightplan();
        this.distanceFlown = flight.getDistanceFlown();
        this.flightTime = flight.getFlightTime();
    }

    public int getPilotNumber() {
        return pilotNumber;
    }

    public FlightStatus getStatus() {
        return status;
    }

    public String getCallsign() {
        return callsign;
    }

    public Position getFirstSeen() {
        return firstSeen;
    }

    public Position getLastSeen() {
        return lastSeen;
    }

    public Position getTakeoff() {
        return takeoff;
    }

    public Position getLanding() {
        return landing;
    }

    public Flightplan getFlightplan() {
        return flightplan;
    }

    public Double getDistanceFlown() {
        return distanceFlown;
    }

    public Double getFlightTime() {
        return flightTime;
    }
}
