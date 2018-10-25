package net.simforge.tracker.flights.datasource;

import net.simforge.commons.legacy.BM;
import net.simforge.tracker.datafeeder.persistence.Report;
import net.simforge.tracker.datafeeder.persistence.ReportPilotPosition;
import org.hibernate.Session;

import java.io.IOException;
import java.util.List;

public class TrivialDBDatasource implements ReportDatasource {
    private Session session;

    public TrivialDBDatasource(Session session) {
        this.session = session;
    }

    public Report loadReport(long reportId) throws IOException {
        BM.start("TrivialDBDatasource.loadReport");
        try {
            return session.byId(Report.class).load(reportId);
        } finally {
            BM.stop();
        }
    }

    public Report loadNextReport(String report) throws IOException {
        BM.start("TrivialDBDatasource.loadNextReport");
        try {
            //noinspection JpaQlInspection
            return (Report) session
                    .createQuery("select r from Report r where r.report > :report and r.parsed = true order by r.report asc")
                    .setString("report", report)
                    .setMaxResults(1)
                    .uniqueResult();
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
        throw new UnsupportedOperationException("TrivialDBDatasource.loadPilotPositions");
    }
}
