package net.simforge.networkview.flights.processors.eventbased.events;

import net.simforge.networkview.core.report.ReportInfo;

public class FlightplanEvent extends BaseEvent {

    public static final String NAME = "flight/flightplan";

    public FlightplanEvent(int pilotNumber, ReportInfo reportInfo) {
        super(pilotNumber, reportInfo, NAME);
    }

}
