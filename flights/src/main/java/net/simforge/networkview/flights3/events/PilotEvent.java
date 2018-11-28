package net.simforge.networkview.flights3.events;

import net.simforge.networkview.datafeeder.ReportInfo;

public class PilotEvent extends BaseEvent {

    protected PilotEvent(int pilotNumber, ReportInfo reportInfo, String type) {
        super(pilotNumber, reportInfo, type);
    }

}
