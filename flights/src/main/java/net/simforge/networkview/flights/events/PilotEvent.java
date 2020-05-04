package net.simforge.networkview.flights.events;

import net.simforge.networkview.datafeeder.ReportInfo;

@SuppressWarnings("WeakerAccess")
public abstract class PilotEvent extends BaseEvent {

    PilotEvent(int pilotNumber, ReportInfo reportInfo, String type) {
        super(pilotNumber, reportInfo, type);
    }

}
