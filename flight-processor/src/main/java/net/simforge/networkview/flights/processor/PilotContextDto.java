package net.simforge.networkview.flights.processor;

import net.simforge.networkview.core.report.ReportInfoDto;

public class PilotContextDto {
    private int pilotNumber;
    private ReportInfoDto firstProcessedReport;
    private ReportInfoDto lastProcessedReport;
    private FlightDto currentFlight;

    public PilotContextDto(int pilotNumber) {
        this.pilotNumber = pilotNumber;
    }

    public int getPilotNumber() {
        return pilotNumber;
    }

    public ReportInfoDto getFirstProcessedReport() {
        return firstProcessedReport;
    }

    public void setFirstProcessedReport(ReportInfoDto firstProcessedReport) {
        this.firstProcessedReport = new ReportInfoDto(firstProcessedReport);
    }

    public ReportInfoDto getLastProcessedReport() {
        return lastProcessedReport;
    }

    public void setLastProcessedReport(ReportInfoDto lastProcessedReport) {
        this.lastProcessedReport = new ReportInfoDto(lastProcessedReport);
    }

    public FlightDto getCurrentFlight() {
        return currentFlight;
    }

    public void setCurrentFlight(FlightDto currentFlight) {
        this.currentFlight = currentFlight;
    }
}
