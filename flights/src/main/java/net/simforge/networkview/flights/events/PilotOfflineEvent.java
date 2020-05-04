package net.simforge.networkview.flights.events;

import net.simforge.networkview.datafeeder.ReportInfo;

public class PilotOfflineEvent extends PilotEvent {

    public static final String NAME = "pilot/offline";

    public PilotOfflineEvent(int pilotNumber, ReportInfo reportInfo) {
        super(pilotNumber, reportInfo, NAME);
    }

}
