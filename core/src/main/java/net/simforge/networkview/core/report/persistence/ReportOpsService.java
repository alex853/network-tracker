package net.simforge.networkview.core.report.persistence;

import net.simforge.networkview.core.report.ReportInfo;

import java.util.List;

public interface ReportOpsService {
    Report loadFirstReport();

    Report loadNextReport(String report);

    Report loadReport(long reportId);

    List<Report> loadAllReports();

    List<Report> loadReports(ReportInfo sinceReport, ReportInfo tillReport);

    List<ReportPilotPosition> loadPilotPositions(ReportInfo reportInfo);

    List<ReportPilotPosition> loadPilotPositions(int pilotNumber);

    ReportPilotPosition loadPilotPosition(int pilotNumber, ReportInfo reportInfo);

    List<ReportPilotPosition> loadPilotPositionsSinceTill(int pilotNumber, ReportInfo sinceReport, ReportInfo tillReport);

    List<ReportPilotPosition> loadPilotPositionsTill(int pilotNumber, String tillReport);
}
