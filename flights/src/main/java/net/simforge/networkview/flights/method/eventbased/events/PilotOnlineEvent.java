package net.simforge.networkview.flights.method.eventbased.events;

import net.simforge.networkview.core.report.ReportInfo;

public class PilotOnlineEvent extends PilotEvent {

    public static final String NAME = "pilot/online";

    public PilotOnlineEvent(int pilotNumber, ReportInfo reportInfo) {
        super(pilotNumber, reportInfo, NAME);
    }

}
