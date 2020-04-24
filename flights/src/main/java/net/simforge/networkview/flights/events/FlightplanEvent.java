package net.simforge.networkview.flights.events;

import net.simforge.networkview.datafeeder.ReportInfo;

public class FlightplanEvent extends BaseEvent {

    public FlightplanEvent(int pilotNumber, ReportInfo reportInfo) {
        super(pilotNumber, reportInfo, "flight/flightplan");
    }

}