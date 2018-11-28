package net.simforge.networkview.flights3.events;

import net.simforge.networkview.datafeeder.ReportInfo;

public class PilotOnlineEvent extends PilotEvent {

    public PilotOnlineEvent(int pilotNumber, ReportInfo reportInfo) {
        super(pilotNumber, reportInfo, "pilot/online");
    }

}
