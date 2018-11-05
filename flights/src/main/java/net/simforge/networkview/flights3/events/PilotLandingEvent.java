package net.simforge.networkview.flights3.events;

public class PilotLandingEvent extends PilotEvent {

    public PilotLandingEvent(int pilotNumber, String report) {
        super(pilotNumber, report, "pilot/landing");
    }

}
