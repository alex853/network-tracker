package net.simforge.networkview.flights3.events;

public class PilotOnlineEvent extends PilotEvent {

    public PilotOnlineEvent(int pilotNumber, String report) {
        super(pilotNumber, report, "pilot/online");
    }

}
