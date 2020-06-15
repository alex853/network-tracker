package net.simforge.networkview.flights.method.eventbased.datasource;

import net.simforge.commons.legacy.BM;
import net.simforge.networkview.core.report.persistence.Report;
import net.simforge.networkview.core.report.persistence.ReportPilotPosition;
import org.hibernate.Session;

import java.io.IOException;
import java.util.List;

public class TrivialDBDatasource implements ReportDatasource {
    private Session session;

    public TrivialDBDatasource(Session session) {
        this.session = session;
    }

    public Report loadReport(long reportId) throws IOException {
        BM.start("TrivialDBDatasource.loadReport(long)");
        try {

            return session.byId(Report.class).load(reportId);

        } finally {
            BM.stop();
        }
    }

    public Report loadReport(String report) throws IOException {
        BM.start("TrivialDBDatasource.loadReport(String)");
        try {

            //noinspection JpaQlInspection
            return (Report) session
                    .createQuery("select r from Report r where report = :report")
                    .setString("report", report)
                    .uniqueResult();

        } finally {
            BM.stop();
        }
    }

    public Report loadNextReport(String report) throws IOException {
        BM.start("TrivialDBDatasource.loadNextReport");
        try {

            if (report == null) {
                //noinspection JpaQlInspection
                return (Report) session
                        .createQuery("select r from Report r where r.parsed = true order by r.report asc")
                        .setMaxResults(1)
                        .uniqueResult();
            } else {
                //noinspection JpaQlInspection
                return (Report) session
                        .createQuery("select r from Report r where r.report > :report and r.parsed = true order by r.report asc")
                        .setString("report", report)
                        .setMaxResults(1)
                        .uniqueResult();
            }

        } finally {
            BM.stop();
        }
    }

    public ReportPilotPosition loadPilotPosition(long reportId, int pilotNumber) throws IOException {
        BM.start("TrivialDBDatasource.loadPilotPosition");
        try {

            //noinspection JpaQlInspection
            return (ReportPilotPosition) session
                    .createQuery("select p from ReportPilotPosition p where p.report.id = :reportId and p.pilotNumber = :pilotNumber")
                    .setLong("reportId", reportId)
                    .setLong("pilotNumber", pilotNumber)
                    .uniqueResult();

        } finally {
            BM.stop();
        }
    }

    @Override
    public List<ReportPilotPosition> loadPilotPositions(long reportId) throws IOException {
        BM.start("TrivialDBDatasource.loadPilotPositions");
        try {

            //noinspection JpaQlInspection,unchecked
            return (List<ReportPilotPosition>) session
                    .createQuery("select p from ReportPilotPosition p where p.report.id = :reportId")
                    .setLong("reportId", reportId)
                    .list();

        } finally {
            BM.stop();
        }
    }


    @Override
    public List<Report> loadReports(long fromReportId, long toReportId) {
        BM.start("TrivialDBDatasource.loadReports");
        try {

            //noinspection unchecked,JpaQlInspection
            return session
                    .createQuery("select r from Report r where r.id between :fromReportId and :toReportId and r.parsed = true order by r.report")
                    .setLong("fromReportId", fromReportId)
                    .setLong("toReportId", toReportId)
                    .list();

        } finally {
            BM.stop();
        }
    }

    @Override
    public List<ReportPilotPosition> loadPilotPositions(int pilotNumber, long fromReportId, long toReportId) {
        BM.start("TrivialDBDatasource.loadPilotPositions(int,long,long)");
        try {

            //noinspection unchecked,JpaQlInspection
            return session
                    .createQuery("select p from ReportPilotPosition p where p.pilotNumber = :pilotNumber and p.report.id between :fromReportId and :toReportId order by p.report.id")
                    .setInteger("pilotNumber", pilotNumber)
                    .setLong("fromReportId", fromReportId)
                    .setLong("toReportId", toReportId)
                    .list();

        } finally {
            BM.stop();
        }
    }
}
