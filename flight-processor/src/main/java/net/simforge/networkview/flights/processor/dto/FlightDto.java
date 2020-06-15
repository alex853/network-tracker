package net.simforge.networkview.flights.processor.dto;

import net.simforge.networkview.flights.method.eventbased.Flight;
import net.simforge.networkview.flights.method.eventbased.FlightStatus;
import net.simforge.networkview.flights.method.eventbased.Flightplan;
import net.simforge.networkview.flights.method.eventbased.Position;

@SuppressWarnings("unused")
public class FlightDto {
    private int pilotNumber;
    private FlightStatus status;
    private String summary;
    private String callsign;
    private PositionDto firstSeen;
    private PositionDto lastSeen;
    private PositionDto takeoff;
    private PositionDto landing;
    private Flightplan flightplan;
    private Double distanceFlown;
    private Double flightTime;

    public FlightDto(Flight flight) {
        this.pilotNumber = flight.getPilotNumber();
        this.status = flight.getStatus();
        this.summary = buildSummary(flight);
        this.callsign = flight.getCallsign();
        this.firstSeen = buildPositionDto(flight.getFirstSeen());
        this.lastSeen = buildPositionDto(flight.getLastSeen());
        this.takeoff = buildPositionDto(flight.getTakeoff());
        this.landing = buildPositionDto(flight.getLanding());
        this.flightplan = flight.getFlightplan();
        this.distanceFlown = flight.getDistanceFlown();
        this.flightTime = flight.getFlightTime();
    }

    private String buildSummary(Flight flight) {
        Flightplan flightplan = flight.getFlightplan();

        String fpDepIcao = flightplan != null ? flightplan.getDeparture() : null;
        String fpArrIcao = flightplan != null ? flightplan.getDestination() : null;

        String depIcao = flight.getTakeoff() != null ? flight.getTakeoff().getStatus() : flight.getFirstSeen().getStatus();
        String arrIcao = flight.getLanding() != null ? flight.getLanding().getStatus() : flight.getLastSeen().getStatus();

        return depIcao + (depIcao.equals(fpDepIcao) ? "" : " (Plan: " + fpDepIcao + ")")
                + " --- "
                + arrIcao + (arrIcao.equals(fpArrIcao) ? "" : " (Plan: " + fpArrIcao + ")");
    }

    private PositionDto buildPositionDto(Position position) {
        return position != null ? new PositionDto(position) : null;
    }

    public int getPilotNumber() {
        return pilotNumber;
    }

    public FlightStatus getStatus() {
        return status;
    }

    public String getSummary() {
        return summary;
    }

    public String getCallsign() {
        return callsign;
    }

    public PositionDto getFirstSeen() {
        return firstSeen;
    }

    public PositionDto getLastSeen() {
        return lastSeen;
    }

    public PositionDto getTakeoff() {
        return takeoff;
    }

    public PositionDto getLanding() {
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
