package net.simforge.networkview.flights3.events;

public class PilotOfflineEvent extends PilotEvent {

    public PilotOfflineEvent(int pilotNumber, String report) {
        super(pilotNumber, report, "pilot/offline");
    }

}
