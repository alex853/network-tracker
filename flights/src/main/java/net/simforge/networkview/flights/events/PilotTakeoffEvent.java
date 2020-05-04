package net.simforge.networkview.flights.events;

import net.simforge.networkview.datafeeder.ReportInfo;

public class PilotTakeoffEvent extends PilotEvent {

    public static final String NAME = "pilot/takeoff";

    public PilotTakeoffEvent(int pilotNumber, ReportInfo reportInfo) {
        super(pilotNumber, reportInfo, NAME);
    }

}
