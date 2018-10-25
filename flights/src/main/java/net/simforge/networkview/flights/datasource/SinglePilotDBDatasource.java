package net.simforge.networkview.flights.datasource;

import net.simforge.commons.legacy.BM;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import org.hibernate.Session;

import java.io.IOException;
import java.util.*;

public class SinglePilotDBDatasource implements ReportDatasource {

    private int pilotNumber;
    private List<Report> reports;
    private Map<Long, Report> reportById = new HashMap<>();
    private Map<Long, ReportPilotPosition> positionById = new HashMap<>();

    public static ReportDatasource load(Session session, int pilotNumber, String fromReportDt, int reportsAmount) {
        BM.start("SinglePilotDBDatasource.load");

        SinglePilotDBDatasource result = new SinglePilotDBDatasource();
        result.pilotNumber = pilotNumber;

        try {
            //noinspection JpaQlInspection
            @SuppressWarnings("unchecked")
            List<Report> reports = session
                    .createQuery("select r from Report r where r.report > :report and r.parsed = true order by r.report asc")
                    .setString("report", fromReportDt)
                    .setMaxResults(reportsAmount)
                    .list();
            result.reports = reports;

            Long minReportId = null;
            Long maxReportId = null;
            for (Report report : reports) {
                long reportId = report.getId();

                result.reportById.put(reportId, report);

                minReportId = (minReportId == null) ? reportId : Math.min(minReportId, reportId);
                maxReportId = (maxReportId == null) ? reportId : Math.max(maxReportId, reportId);
            }

            if (minReportId != null) {
                //noinspection JpaQlInspection
                @SuppressWarnings("unchecked")
                List<ReportPilotPosition> positions = session
                        .createQuery("select p from ReportPilotPosition p where p.report.id between :fromId and :toId and p.pilotNumber = :pilotNumber")
                        .setLong("fromId", minReportId)
                        .setLong("toId", maxReportId)
                        .setLong("pilotNumber", pilotNumber)
                        .list();

                for (ReportPilotPosition position : positions) {
                    result.positionById.put(position.getReport().getId(), position);
                }
            }
        } finally {
            BM.stop();
        }

        return result;
    }

    public static ReportDatasource load(Session session, int pilotNumber, String fromReportDt, String toReportDt) {
        BM.start("SinglePilotDBDatasource.load");

        SinglePilotDBDatasource result = new SinglePilotDBDatasource();
        result.pilotNumber = pilotNumber;

        try {
            //noinspection JpaQlInspection
            @SuppressWarnings("unchecked")
            List<Report> reports = session
                    .createQuery("select r from Report r where r.report >= :fromReport and r.report <= :toReport and r.parsed = true order by r.report asc")
                    .setString("fromReport", fromReportDt)
                    .setString("toReport", toReportDt)
                    .list();
            result.reports = reports;

            Long minReportId = null;
            Long maxReportId = null;
            for (Report report : reports) {
                long reportId = report.getId();

                result.reportById.put(reportId, report);

                minReportId = (minReportId == null) ? reportId : Math.min(minReportId, reportId);
                maxReportId = (maxReportId == null) ? reportId : Math.max(maxReportId, reportId);
            }

            if (minReportId != null) {
                //noinspection JpaQlInspection
                @SuppressWarnings("unchecked")
                List<ReportPilotPosition> positions = session
                        .createQuery("select p from ReportPilotPosition p where p.report.id between :fromId and :toId and p.pilotNumber = :pilotNumber")
                        .setLong("fromId", minReportId)
                        .setLong("toId", maxReportId)
                        .setLong("pilotNumber", pilotNumber)
                        .list();

                for (ReportPilotPosition position : positions) {
                    result.positionById.put(position.getReport().getId(), position);
                }
            }
        } finally {
            BM.stop();
        }

        return result;
    }

    @Override
    public Report loadReport(long reportId) throws IOException {
        return reportById.get(reportId);
    }

    @Override
    public Report loadNextReport(String report) throws IOException {
        if (report == null) {
            return !reports.isEmpty() ? reports.get(0) : null;
        }

        for (int i = 0; i < reports.size(); i++) {
            Report eachReport = reports.get(i);

            if (eachReport.getReport().equals(report)) {
                // next is a result
                if (i + 1 < reports.size()) {
                    return reports.get(i + 1);
                } else {
                    return null;
                }
            }

            if (eachReport.getReport().compareTo(report) > 0) {
                return eachReport;
            }
        }

        return null;
    }

    @Override
    public ReportPilotPosition loadPilotPosition(long reportId, int pilotNumber) throws IOException {
        if (this.pilotNumber != pilotNumber) {
            return null;
        }

        return positionById.get(reportId);
    }

    @Override
    public List<ReportPilotPosition> loadPilotPositions(long reportId) throws IOException {
        ReportPilotPosition pilotPosition = loadPilotPosition(reportId, pilotNumber);
        List<ReportPilotPosition> result = new ArrayList<>();
        if (pilotPosition != null) {
            result.add(pilotPosition);
        }
        return result;
    }
}
