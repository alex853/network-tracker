package net.simforge.networkview.flights2.persistence;

import net.simforge.commons.legacy.BM;
import net.simforge.networkview.Network;
import net.simforge.networkview.datafeeder.SessionManager;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import net.simforge.networkview.flights.datasource.TrivialDBDatasource;
import org.hibernate.Session;

import java.io.IOException;
import java.util.List;

public class DBReportDatasource implements ReportDatasource2 {
    private Network network;
    private SessionManager sessionManager;

    public DBReportDatasource(Network network, SessionManager sessionManager) {
        this.network = network;
        this.sessionManager = sessionManager;
    }

    @Override
    public Report loadReport(long reportId) throws IOException {
        BM.start("DBReportDatasource.loadReport");
        try (Session session = sessionManager.getSession(network)) {
            return new TrivialDBDatasource(session).loadReport(reportId);
        } finally {
            BM.stop();
        }
    }

    @Override
    public Report loadNextReport(String report) throws IOException {
        BM.start("DBReportDatasource.loadNextReport");
        try (Session session = sessionManager.getSession(network)) {
            return new TrivialDBDatasource(session).loadNextReport(report);
        } finally {
            BM.stop();
        }
    }

    @Override
    public ReportPilotPosition loadPilotPosition(long reportId, int pilotNumber) throws IOException {
        BM.start("DBReportDatasource.loadPilotPosition");
        try (Session session = sessionManager.getSession(network)) {
            return new TrivialDBDatasource(session).loadPilotPosition(reportId, pilotNumber);
        } finally {
            BM.stop();
        }
    }

    @Override
    public List<ReportPilotPosition> loadPilotPositions(long reportId) throws IOException {
        BM.start("DBReportDatasource.loadPilotPositions");
        try (Session session = sessionManager.getSession(network)) {
            return new TrivialDBDatasource(session).loadPilotPositions(reportId);
        } finally {
            BM.stop();
        }
    }
}