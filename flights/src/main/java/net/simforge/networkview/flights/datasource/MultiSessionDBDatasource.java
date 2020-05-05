package net.simforge.networkview.flights.datasource;

import net.simforge.networkview.core.report.persistence.Report;
import net.simforge.networkview.core.report.persistence.ReportPilotPosition;
import net.simforge.networkview.core.report.persistence.ReportSessionManager;

import java.io.IOException;
import java.util.List;

public class MultiSessionDBDatasource implements ReportDatasource {
    private ReportSessionManager reportSessionManager;

    public MultiSessionDBDatasource(ReportSessionManager reportSessionManager) {
        this.reportSessionManager = reportSessionManager;
    }

    @Override
    public Report loadReport(long reportId) throws IOException {
        throw new UnsupportedOperationException("MultiSessionDBDatasource.loadReport");
    }

    @Override
    public Report loadReport(String report) throws IOException {
        return null;
    }

    @Override
    public Report loadNextReport(String report) throws IOException {
        throw new UnsupportedOperationException("MultiSessionDBDatasource.loadNextReport");
    }

    @Override
    public ReportPilotPosition loadPilotPosition(long reportId, int pilotNumber) throws IOException {
        throw new UnsupportedOperationException("MultiSessionDBDatasource.loadPilotPosition");
    }

    @Override
    public List<ReportPilotPosition> loadPilotPositions(long reportId) throws IOException {
        throw new UnsupportedOperationException("MultiSessionDBDatasource.loadPilotPositions");
    }

    @Override
    public List<Report> loadReports(long fromReportId, long toReportId) {
        throw new UnsupportedOperationException("MultiSessionDBDatasource.loadReports");
    }

    @Override
    public List<ReportPilotPosition> loadPilotPositions(int pilotNumber, long fromReportId, long toReportId) {
        throw new UnsupportedOperationException("MultiSessionDBDatasource.loadPilotPositions");
    }
}
