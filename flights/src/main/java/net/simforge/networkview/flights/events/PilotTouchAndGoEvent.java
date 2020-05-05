package net.simforge.networkview.flights.events;

import net.simforge.networkview.core.report.ReportInfo;

public class PilotTouchAndGoEvent extends PilotEvent {

    public static final String NAME = "pilot/touch&go";

    public PilotTouchAndGoEvent(int pilotNumber, ReportInfo reportInfo) {
        super(pilotNumber, reportInfo, NAME);
    }

}
