package net.simforge.networkview.flights3.events;

import net.simforge.networkview.datafeeder.ReportInfo;

public class PilotLandingEvent extends PilotEvent {

    public PilotLandingEvent(int pilotNumber, ReportInfo reportInfo) {
        super(pilotNumber, reportInfo, "pilot/landing");
    }

}
