package net.simforge.networkview.flights3.events;

public class PilotTakeoffEvent extends PilotEvent {

    public PilotTakeoffEvent(int pilotNumber, String report) {
        super(pilotNumber, report, "pilot/takeoff");
    }

}
