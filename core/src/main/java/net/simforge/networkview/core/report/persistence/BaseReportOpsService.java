package net.simforge.networkview.core.report.persistence;

import net.simforge.commons.legacy.BM;
import net.simforge.networkview.core.Network;
import org.hibernate.Session;

import java.util.List;

public class BaseReportOpsService implements ReportOpsService {
    private final ReportSessionManager reportSessionManager;
    private final Network network;
    private final Integer year = null;

    public BaseReportOpsService(ReportSessionManager reportSessionManager, Network network) {
        this.reportSessionManager = reportSessionManager;
        this.network = network;
    }

    private Session getSession() {
        return year == null
                ? reportSessionManager.getSession(network)
                : reportSessionManager.getSession(network, year);
    }

    @Override
    public Report loadFirstReport() {
        BM.start("BaseReportOpsService.loadFirstReport");
        try (Session session = getSession()) {
            return (Report) session
                    .createQuery("from Report where parsed = true order by report asc")
                    .setMaxResults(1)
                    .uniqueResult();
        } finally {
            BM.stop();
        }
    }

    @Override
    public Report loadNextReport(String report) {
        BM.start("BaseReportOpsService.loadNextReport");
        try (Session session = getSession()) {
            return (Report) session
                    .createQuery("from Report where parsed = true and report > :report order by report asc")
                    .setString("report", report)
                    .setMaxResults(1)
                    .uniqueResult();
        } finally {
            BM.stop();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Report> loadAllReports() {
        BM.start("BaseReportOpsService.loadAllReports");
        try (Session session = getSession()) {
            return session
                    .createQuery("from Report order by report")
                    .list();
        } finally {
            BM.stop();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Report> loadReports(String sinceReport, String tillReport) {
        BM.start("BaseReportOpsService.loadReports");
        try (Session session = getSession()) {
            return session
                    .createQuery("from Report where report between :sinceReport and :tillReport order by report")
                    .setString("sinceReport", sinceReport)
                    .setString("tillReport", tillReport)
                    .list();
        } finally {
            BM.stop();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ReportPilotPosition> loadPilotPositions(Report report) {
        BM.start("BaseReportOpsService.loadPilotPositions");
        try (Session session = getSession()) {
            return session
                    .createQuery("from ReportPilotPosition where report = :report")
                    .setEntity("report", report)
                    .list();
        } finally {
            BM.stop();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ReportPilotPosition> loadPilotPositions(int pilotNumber) {
        BM.start("BaseReportOpsService.loadPilotPositions#pilotNumber");
        try (Session session = getSession()) {
            return session
                    .createQuery("from ReportPilotPosition where pilotNumber = :pilotNumber")
                    .setInteger("pilotNumber", pilotNumber)
                    .list();
        } finally {
            BM.stop();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ReportPilotPosition> loadPilotPositionsSinceTill(int pilotNumber, String sinceReport, String tillReport) {
        BM.start("BaseReportOpsService.loadPilotPositionsSinceTill");
        try (Session session = getSession()) {
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
        try (Session session = getSession()) {
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
}
