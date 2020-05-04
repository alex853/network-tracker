package net.simforge.networkview.flights.events;

import net.simforge.networkview.datafeeder.ReportInfo;

public class PilotOnlineEvent extends PilotEvent {

    public static final String NAME = "pilot/online";

    public PilotOnlineEvent(int pilotNumber, ReportInfo reportInfo) {
        super(pilotNumber, reportInfo, NAME);
    }

}
