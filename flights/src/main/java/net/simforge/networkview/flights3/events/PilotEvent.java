package net.simforge.networkview.flights3.events;

public class PilotEvent extends BaseEvent {

    protected PilotEvent(int pilotNumber, String report, String type) {
        super(pilotNumber, report, type);
    }

}
