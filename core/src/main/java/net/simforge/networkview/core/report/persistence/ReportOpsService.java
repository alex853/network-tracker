package net.simforge.networkview.core.report.persistence;

import java.util.List;

public interface ReportOpsService {
    Report loadFirstReport();

    Report loadNextReport(String report);

    List<Report> loadAllReports();

    List<Report> loadReports(String sinceReport, String tillReport);

    List<ReportPilotPosition> loadPilotPositions(Report report);

    List<ReportPilotPosition> loadPilotPositions(int pilotNumber);

    List<ReportPilotPosition> loadPilotPositionsSinceTill(int pilotNumber, String sinceReport, String tillReport);

    List<ReportPilotPosition> loadPilotPositionsTill(int pilotNumber, String tillReport);
}
