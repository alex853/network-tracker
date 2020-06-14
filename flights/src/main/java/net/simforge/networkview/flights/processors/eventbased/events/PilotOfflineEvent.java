package net.simforge.networkview.flights.processors.eventbased.events;

import net.simforge.networkview.core.report.ReportInfo;

public class PilotOfflineEvent extends PilotEvent {

    public static final String NAME = "pilot/offline";

    public PilotOfflineEvent(int pilotNumber, ReportInfo reportInfo) {
        super(pilotNumber, reportInfo, NAME);
    }

}
