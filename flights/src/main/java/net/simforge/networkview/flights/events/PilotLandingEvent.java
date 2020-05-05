package net.simforge.networkview.flights.events;

import net.simforge.networkview.core.report.ReportInfo;

public class PilotLandingEvent extends PilotEvent {

    public static final String NAME = "pilot/landing";

    public PilotLandingEvent(int pilotNumber, ReportInfo reportInfo) {
        super(pilotNumber, reportInfo, NAME);
    }

}
