package net.simforge.networkview.flights.events;

import net.simforge.networkview.core.report.ReportInfo;

public interface TrackingEvent {

    int getPilotNumber();

    ReportInfo getReportInfo();

    String getType();

}
