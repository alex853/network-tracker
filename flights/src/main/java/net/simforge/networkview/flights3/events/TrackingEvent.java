package net.simforge.networkview.flights3.events;

import net.simforge.networkview.datafeeder.ReportInfo;

public interface TrackingEvent {

    int getPilotNumber();

    ReportInfo getReportInfo();

    String getType();

}
