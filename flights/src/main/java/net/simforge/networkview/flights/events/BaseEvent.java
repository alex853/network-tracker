package net.simforge.networkview.flights.events;

import net.simforge.networkview.datafeeder.ReportInfo;

public class BaseEvent implements TrackingEvent {
    private int pilotNumber;
    private ReportInfo reportInfo;
    private String type;

    protected BaseEvent(int pilotNumber, ReportInfo reportInfo, String type) {
        this.pilotNumber = pilotNumber;
        this.reportInfo = reportInfo;
        this.type = type;
    }

    @Override
    public int getPilotNumber() {
        return pilotNumber;
    }

    @Override
    public ReportInfo getReportInfo() {
        return reportInfo;
    }

    @Override
    public String getType() {
        return type;
    }
}
