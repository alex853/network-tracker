package net.simforge.networkview.flights3.events;

import net.simforge.networkview.datafeeder.ReportInfo;

public class PilotTakeoffEvent extends PilotEvent {

    public PilotTakeoffEvent(int pilotNumber, ReportInfo reportInfo) {
        super(pilotNumber, reportInfo, "pilot/takeoff");
    }

}
