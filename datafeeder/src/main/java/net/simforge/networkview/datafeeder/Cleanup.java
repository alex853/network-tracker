package net.simforge.networkview.datafeeder;

import net.simforge.commons.hibernate.HibernateUtils;
import net.simforge.commons.legacy.BM;
import net.simforge.commons.io.Marker;
import net.simforge.commons.misc.JavaTime;
import net.simforge.commons.runtime.BaseTask;
import net.simforge.commons.runtime.RunningMarker;
import net.simforge.tracker.Network;
import net.simforge.tracker.SessionManager;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.tracker.tools.ReportUtils;
import org.hibernate.Session;

import java.time.LocalDateTime;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Cleanup extends BaseTask {
    private static final String ARG_NETWORK = "network";

    private SessionManager sessionManager;
    private Network network;
    private long keepDays = 30;
    private Marker archivedReportMarker;

    public Cleanup(Properties properties) {
        this(TrackerTasks.getSessionManager(), Network.valueOf(properties.getProperty(ARG_NETWORK)));
    }

    public Cleanup(SessionManager sessionManager, Network network) {
        super("Cleanup-" + network);
        this.sessionManager = sessionManager;
        this.network = network;
    }

    @Override
    protected void startup() {
        super.startup();

        BM.setLoggingPeriod(TimeUnit.HOURS.toMillis(1));

        RunningMarker.lock(getTaskName());

        archivedReportMarker = new Marker("Archive-" + network);
    }

    @Override
    protected void process() {
        BM.start("Cleanup.process");
        try (Session liveSession = sessionManager.getSession(network)) {
            Report report = ReportOps.loadFirstReport(liveSession);;
            if (report == null) {
                logger.debug("No reports found");
                return; // standard sleep time
            }

            LocalDateTime reportDt = ReportUtils.fromTimestampJava(report.getReport());
            logger.debug(ReportOps.logMsg(report.getReport(), "Cleanup started"));

            String lastProcessedReport = archivedReportMarker.getString();
            if (lastProcessedReport == null) {
                logger.warn(ReportOps.logMsg(report.getReport(), "    Archived report marker is empty"));
                return; // standard sleep time
            }

            LocalDateTime lastProcessedReportDt = ReportUtils.fromTimestampJava(lastProcessedReport);
            LocalDateTime threshold = lastProcessedReportDt.minusDays(keepDays);
            logger.debug(ReportOps.logMsg(report.getReport(), "    Threshold is {}"), JavaTime.yMdHms.format(threshold));

            if (reportDt.isAfter(threshold)) {
                logger.debug(ReportOps.logMsg(report.getReport(), "    Report is after threshold"));
                return; // standard sleep time
            }

            removeReport(liveSession, report);

            logger.info(ReportOps.logMsg(report.getReport(), "Cleanup complete"));

            setNextSleepTime(1000); // short sleep time
        } finally {
            BM.stop();
        }
    }

    private void removeReport(Session liveSession, Report report) {
        BM.start("Cleanup.removeReport");
        try {
            HibernateUtils.transaction(liveSession, () -> {
                //noinspection JpaQlInspection
                liveSession
                        .createQuery("delete from ReportPilotPosition where report = :report")
                        .setEntity("report", report)
                        .executeUpdate();

                //noinspection JpaQlInspection
                liveSession
                        .createQuery("delete from ReportLogEntry where report = :report")
                        .setEntity("report", report)
                        .executeUpdate();

                liveSession.delete(report);
            });
        } finally {
            BM.stop();
        }
    }
}
