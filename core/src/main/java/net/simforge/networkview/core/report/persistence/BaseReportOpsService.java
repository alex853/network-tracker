package net.simforge.networkview.core.report.persistence;

import net.simforge.commons.legacy.BM;
import net.simforge.networkview.core.Network;
import org.hibernate.Session;

import java.util.List;

public class BaseReportOpsService implements ReportOpsService {
    private final ReportSessionManager reportSessionManager;
    private final Network network;

    public BaseReportOpsService(ReportSessionManager reportSessionManager, Network network) {
        this.reportSessionManager = reportSessionManager;
        this.network = network;
    }

    @Override
    public Report loadFirstReport() {
        try (Session session = reportSessionManager.getSession(network)) {
            return ReportOps.loadFirstReport(session);
        }
    }

    @Override
    public Report loadNextReport(String report) {
        try (Session session = reportSessionManager.getSession(network)) {
            return ReportOps.loadNextReport(session, report);
        }
    }

    @Override
    public List<ReportPilotPosition> loadPilotPositions(Report report) {
        try (Session session = reportSessionManager.getSession(network)) {
            return ReportOps.loadPilotPositions(session, report);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ReportPilotPosition> loadPilotPositionsSinceTill(int pilotNumber, String sinceReport, String tillReport) {
        BM.start("BaseReportOpsService.loadPilotPositionsSinceTill");
        try (Session session = reportSessionManager.getSession(network)) {
            return session
                    .createQuery("from ReportPilotPosition " +
                            "where pilotNumber = :pilotNumber " +
                            "  and report.report between :sinceReport and :tillReport " +
                            "order by report.report")
                    .setInteger("pilotNumber", pilotNumber)
                    .setString("sinceReport", sinceReport)
                    .setString("tillReport", tillReport)
                    .list();
        } finally {
            BM.stop();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ReportPilotPosition> loadPilotPositionsTill(int pilotNumber, String tillReport) {
        BM.start("BaseReportOpsService.loadPilotPositionsSinceTill");
        try (Session session = reportSessionManager.getSession(network)) {
            return session
                    .createQuery("from ReportPilotPosition " +
                            "where pilotNumber = :pilotNumber " +
                            "  and report.report <= :tillReport " +
                            "order by report.report")
                    .setInteger("pilotNumber", pilotNumber)
                    .setString("tillReport", tillReport)
                    .list();
        } finally {
            BM.stop();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Report> loadReports(String sinceReport, String tillReport) {
        BM.start("BaseReportOpsService.loadReports");
        try (Session session = reportSessionManager.getSession(network)) {
            return session
                    .createQuery("from Report where report between :sinceReport and :tillReport order by report")
                    .setString("sinceReport", sinceReport)
                    .setString("tillReport", tillReport)
                    .list();
        } finally {
            BM.stop();
        }
    }
}
