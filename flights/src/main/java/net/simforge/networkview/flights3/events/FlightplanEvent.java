package net.simforge.networkview.flights3.events;

public class FlightplanEvent extends BaseEvent {

    public FlightplanEvent(int pilotNumber, String report) {
        super(pilotNumber, report, "flight/flightplan");
    }

}
