package net.simforge.networkview.flights.datasource;

import net.simforge.networkview.datafeeder.SessionManager;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;

import java.io.IOException;
import java.util.List;

public class MultiSessionDBDatasource implements ReportDatasource {
    private SessionManager sessionManager;

    public MultiSessionDBDatasource(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
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
}
