package net.simforge.networkview.flights.method.eventbased.datasource;

import net.simforge.commons.io.Csv;
import net.simforge.networkview.core.report.ReportInfoDto;
import net.simforge.networkview.core.report.persistence.Report;
import net.simforge.networkview.core.report.persistence.ReportPilotPosition;
import net.simforge.networkview.core.report.snapshot.CsvSnapshotReportOpsService;

import java.util.*;

public class CsvDatasource implements ReportDatasource {
    private CsvSnapshotReportOpsService reportOpsService;

    public CsvDatasource(Csv csv) {
        reportOpsService = new CsvSnapshotReportOpsService(csv);
    }

    @Override
    public Report loadReport(long reportId) {
        return reportOpsService.loadReport(reportId);
    }

    @Override
    public Report loadReport(String report) {
        //return reportOpsService.loadReport(report);
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Report> loadReports(long fromReportId, long toReportId) {
        return reportOpsService.loadReports(new ReportInfoDto(fromReportId, null), new ReportInfoDto(toReportId, null));
    }

    @Override
    public Report loadNextReport(String report) {
        return reportOpsService.loadNextReport(report);
    }

    @Override
    public ReportPilotPosition loadPilotPosition(long reportId, int pilotNumber) {
        return reportOpsService.loadPilotPosition(pilotNumber, new ReportInfoDto(reportId, null));
    }

    @Override
    public List<ReportPilotPosition> loadPilotPositions(long reportId) {
        return reportOpsService.loadPilotPositions(new ReportInfoDto(reportId, null));
    }

    @Override
    public List<ReportPilotPosition> loadPilotPositions(int pilotNumber, long fromReportId, long toReportId) {
        return reportOpsService.loadPilotPositionsSinceTill(pilotNumber, new ReportInfoDto(fromReportId, null), new ReportInfoDto(toReportId, null));
    }
}
