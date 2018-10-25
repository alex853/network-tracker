package net.simforge.networkview.datafeeder;

import net.simforge.commons.legacy.BM;
import net.simforge.commons.misc.JavaTime;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import org.hibernate.Session;

import java.util.List;

public class ReportOps {

    public static String logMsg(String report, String msg) {
        return "Report: " + JavaTime.yMdHms.format(ReportUtils.fromTimestampJava(report)) + " - " + msg;
    }

    @SuppressWarnings("unchecked")
    public static List<ReportPilotPosition> loadPilotPositions(Session session, Report report) {
        BM.start("ReportOps.loadPilotPositions");
        try {
            //noinspection JpaQlInspection
            return session
                    .createQuery("select p from ReportPilotPosition p where p.report = :report")
                    .setEntity("report", report)
                    .list();
        } finally {
            BM.stop();
        }
    }

    public static Report loadReport(Session session, String report) {
        BM.start("ReportOps.loadReport");
        try {
            //noinspection JpaQlInspection
            return (Report) session
                    .createQuery("from Report where report = :report and parsed = true")
                    .setString("report", report)
                    .setMaxResults(1)
                    .uniqueResult();
        } finally {
            BM.stop();
        }
    }

    public static Report loadFirstReport(Session session) {
        BM.start("ReportOps.loadFirstReport");
        try {
            //noinspection JpaQlInspection
            return (Report) session
                    .createQuery("from Report where parsed = true order by report asc")
                    .setMaxResults(1)
                    .uniqueResult();
        } finally {
            BM.stop();
        }
    }

    public static Report loadNextReport(Session session, String report) {
        BM.start("ReportOps.loadNextReport");
        try {
            //noinspection JpaQlInspection
            return (Report) session
                    .createQuery("from Report where parsed = true and report > :report order by report asc")
                    .setString("report", report)
                    .setMaxResults(1)
                    .uniqueResult();
        } finally {
            BM.stop();
        }
    }

    public static Report loadPrevReport(Session session, String report) {
        BM.start("ReportOps.loadPrevReport");
        try {
            //noinspection JpaQlInspection
            return (Report) session
                    .createQuery("from Report where parsed = true and report < :report order by report desc")
                    .setString("report", report)
                    .setMaxResults(1)
                    .uniqueResult();
        } finally {
            BM.stop();
        }
    }
}
