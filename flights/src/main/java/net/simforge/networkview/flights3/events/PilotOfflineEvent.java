package net.simforge.networkview.flights3.events;

import net.simforge.networkview.datafeeder.ReportInfo;

public class PilotOfflineEvent extends PilotEvent {

    public PilotOfflineEvent(int pilotNumber, ReportInfo reportInfo) {
        super(pilotNumber, reportInfo, "pilot/offline");
    }

}
