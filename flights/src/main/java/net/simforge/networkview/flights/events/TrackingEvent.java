package net.simforge.networkview.flights.events;

import net.simforge.networkview.datafeeder.ReportInfo;

public interface TrackingEvent {

    int getPilotNumber();

    ReportInfo getReportInfo();

    String getType();

}
