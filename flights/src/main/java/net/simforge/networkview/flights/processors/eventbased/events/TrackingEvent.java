package net.simforge.networkview.flights.processors.eventbased.events;

import net.simforge.networkview.core.report.ReportInfo;

public interface TrackingEvent {

    int getPilotNumber();

    ReportInfo getReportInfo();

    String getType();

}
