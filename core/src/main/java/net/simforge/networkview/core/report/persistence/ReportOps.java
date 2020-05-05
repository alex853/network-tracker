package net.simforge.networkview.core.report.persistence;

import net.simforge.commons.legacy.BM;
import org.hibernate.Session;

import java.util.List;

public class ReportOps {

    @Deprecated
    @SuppressWarnings("unchecked")
    public static List<ReportPilotPosition> loadPilotPositions(Session session, Report report) {
        BM.start("ReportOps.loadPilotPositions");
        try {
            return session
                    .createQuery("from ReportPilotPosition where report = :report")
                    .setEntity("report", report)
                    .list();
        } finally {
            BM.stop();
        }
    }

    public static ReportPilotPosition loadPilotPosition(Session session, Report report, int pilotNumber) {
        BM.start("ReportOps.loadPilotPosition");
        try {
            return (ReportPilotPosition) session
                    .createQuery("from ReportPilotPosition where report = :report and pilotNumber = :pilotNumber")
                    .setEntity("report", report)
                    .setInteger("pilotNumber", pilotNumber)
                    .uniqueResult();
        } finally {
            BM.stop();
        }
    }

    public static Report loadParsedReport(Session session, String report) {
        BM.start("ReportOps.loadReport");
        try {
            return (Report) session
                    .createQuery("from Report where report = :report and parsed = true")
                    .setString("report", report)
                    .setMaxResults(1)
                    .uniqueResult();
        } finally {
            BM.stop();
        }
    }

    public static Report loadReport(Session session, String report) {
        BM.start("ReportOps.loadReport");
        try {
            return (Report) session
                    .createQuery("from Report where report = :report")
                    .setString("report", report)
                    .setMaxResults(1)
                    .uniqueResult();
        } finally {
            BM.stop();
        }
    }

    @Deprecated
    public static Report loadFirstReport(Session session) {
        BM.start("ReportOps.loadFirstReport");
        try {
            return (Report) session
                    .createQuery("from Report where parsed = true order by report asc")
                    .setMaxResults(1)
                    .uniqueResult();
        } finally {
            BM.stop();
        }
    }

    @Deprecated
    public static Report loadNextReport(Session session, String report) {
        BM.start("ReportOps.loadNextReport");
        try {
            return (Report) session
                    .createQuery("from Report where parsed = true and report > :report order by report asc")
                    .setString("report", report)
                    .setMaxResults(1)
                    .uniqueResult();
        } finally {
            BM.stop();
        }
    }

    @SuppressWarnings("unused")
    public static Report loadPrevReport(Session session, String report) {
        BM.start("ReportOps.loadPrevReport");
        try {
            return (Report) session
                    .createQuery("from Report where parsed = true and report < :report order by report desc")
                    .setString("report", report)
                    .setMaxResults(1)
                    .uniqueResult();
        } finally {
            BM.stop();
        }
    }

    public static ReportPilotFpRemarks loadFpRemarks(Session session, String fpRemarksStr) {
        BM.start("ReportOps.loadFpRemarks");
        try {
            return (ReportPilotFpRemarks) session
                    .createQuery("from ReportPilotFpRemarks where remarks = :remarks")
                    .setString("remarks", fpRemarksStr)
                    .setMaxResults(1)
                    .uniqueResult();
        } finally {
            BM.stop();
        }
    }
}
