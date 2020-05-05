package net.simforge.networkview.core.report.persistence;

import java.util.List;

public interface ReportOpsService {
    Report loadFirstReport();

    Report loadNextReport(String report);

    List<ReportPilotPosition> loadPilotPositions(Report report);

    List<ReportPilotPosition> loadPilotPositionsSinceTill(int pilotNumber, String sinceReport, String tillReport);

    List<ReportPilotPosition> loadPilotPositionsTill(int pilotNumber, String tillReport);

    List<Report> loadReports(String sinceReport, String tillReport);
}
