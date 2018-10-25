package net.simforge.networkview.datafeeder;

import net.simforge.commons.legacy.BM;
import net.simforge.commons.io.Marker;
import net.simforge.commons.runtime.BaseTask;
import net.simforge.commons.runtime.RunningMarker;
import net.simforge.commons.misc.Misc;
import net.simforge.networkview.Network;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportLogEntry;
import net.simforge.networkview.datafeeder.persistence.ReportPilotFpRemarks;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import org.hibernate.Session;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Parse extends BaseTask {

    private static final String ARG_NETWORK = "network";
    private static final String ARG_SINGLE = "single";
    private static final String ARG_STORAGE = "storage";

    private Network network;
    private String storageRoot = ReportStorage.DEFAULT_STORAGE_ROOT;
    private ReportStorage storage;
    private boolean singleRun = false;
    @SuppressWarnings("FieldCanBeLocal")
    private Marker marker;
    private SessionManager sessionManager;

    public Parse(Properties properties) {
        super("Parse-" + properties.getProperty(ARG_NETWORK));
        init(properties);
    }

    private void init(Properties properties) {
        String networkStr = properties.getProperty(ARG_NETWORK);
        if ("vatsim".equalsIgnoreCase(networkStr)) {
            network = Network.VATSIM;
        } else if ("ivao".equalsIgnoreCase(networkStr)) {
            network = Network.IVAO;
        } else {
            throw new IllegalArgumentException("Specify correct network name");
        }

        storageRoot = properties.getProperty(ARG_STORAGE, storageRoot);

        singleRun = Boolean.parseBoolean(properties.getProperty(ARG_SINGLE, Boolean.toString(singleRun)));
    }

    @Override
    protected void startup() {
        super.startup();

        BM.setLoggingPeriod(TimeUnit.HOURS.toMillis(1));
//        BM.setLoggingPeriod(TimeUnit.MINUTES.toMillis(10));

        RunningMarker.lock(getTaskName());

        logger.info("Network     : " + network);
        logger.info("Storage root: " + storageRoot);
        logger.info("Single run  : " + singleRun);


        storage = ReportStorage.getStorage(storageRoot, network);
        marker = new Marker(getTaskName());
        sessionManager = TrackerTasks.getSessionManager();

        setBaseSleepTime(10000L);
    }

    @Override
    protected void process() {
        BM.start("Parse.process");
        try {

            String lastProcessedReport = marker.getString();

            String nextReport;
            if (lastProcessedReport == null) {
                nextReport = storage.getFirstReport();
                if (nextReport == null) {
                    logger.info("Still no first report found");
                }
            } else {
                nextReport = storage.getNextReport(lastProcessedReport);
            }

            if (nextReport == null) {
                return;
            }

            try {
                processReport(nextReport);

                marker.setString(nextReport);

                setNextSleepTime(100L); // small interval to catch up all remaining reports
            } catch (Exception e) {
                logger.error("Error on report " + nextReport, e);

                logger.warn("Long sleep due to exception");
                setNextSleepTime(300000L); // 5 mins after exception
            }

        } catch (IOException e) {
            logger.error("I/O exception happened", e);
            throw new RuntimeException("I/O exception happened", e);
        } finally {
            BM.stop();
        }
    }

    private void processReport(String report)
            throws IOException, SQLException {
        BM.start("Parse.processReport");
        try (Session session = sessionManager.getSession(network)) {

            logger.debug(ReportOps.logMsg(report, "Parsing started..."));

            ReportFile reportFile = storage.getReportFile(report);
            logger.debug(ReportOps.logMsg(report, "      Data splitted"));

            List<ReportFile.ClientInfo> clientInfos = reportFile.getClientInfos();
            List<ReportFile.ClientInfo> pilotInfos = reportFile.getClientInfos(ReportFile.ClientType.PILOT);
            List<ReportFile.LogEntry> logEntries = new ArrayList<>(reportFile.getLog());
            logger.debug(ReportOps.logMsg(report, "      Data parsed"));


            Report _report = findReport(session, report);

            if (_report == null) {
                _report = new Report();
                _report.setReport(report);
                _report.setClients(clientInfos.size());
                _report.setPilots(pilotInfos.size());
                _report.setHasLogs(!logEntries.isEmpty());
                _report.setParsed(false);

                save(session, _report, "createReport");

                logger.debug(ReportOps.logMsg(report, "      Report record inserted"));
            } else {
                logger.debug(ReportOps.logMsg(report, "      Report already exists ==> Only absent records will be added"));
            }


            List<ReportLogEntry> existingLogEntries = loadExistingLogEntries(session, _report);

            for (ReportFile.LogEntry logEntry : logEntries) {
                boolean logEntryExists = false;
                for (ReportLogEntry existingLogEntry : existingLogEntries) {
                    logEntryExists = Misc.equal(logEntry.getSection(), existingLogEntry.getSection())
                            && Misc.equal(logEntry.getObject(), existingLogEntry.getObject())
                            && Misc.equal(logEntry.getMsg(), existingLogEntry.getMessage())
                            && Misc.equal(logEntry.getValue(), existingLogEntry.getValue());
                    if (logEntryExists) {
                        break;
                    }
                }

                if (logEntryExists) {
                    continue;
                }

                logger.debug(ReportOps.logMsg(report, "        LogEntry - S: " + logEntry.getSection() + "; O: " + logEntry.getObject() + "; M: " + logEntry.getMsg() + "; V: " + logEntry.getValue()));
                ReportLogEntry l = new ReportLogEntry();
                l.setReport(_report);
                l.setSection(logEntry.getSection());
                l.setObject(logEntry.getObject());
                l.setMessage(logEntry.getMsg());
                l.setValue(logEntry.getValue());

                save(session, l, "createLogEntry");

                existingLogEntries.add(l);
            }
            logger.debug(ReportOps.logMsg(report, "      LogEntries added"));


            List<ReportPilotPosition> existingPositions = ReportOps.loadPilotPositions(session, _report);

            for (ReportFile.ClientInfo pilotInfo : pilotInfos) {
                try {
                    int pilotNumber = pilotInfo.getCid();
                    boolean positionExists = false;
                    for (ReportPilotPosition existingPosition : existingPositions) {
                        if (pilotNumber == existingPosition.getPilotNumber()) {
                            positionExists = true;
                            break;
                        }
                    }

                    if (positionExists) {
                        continue;
                    }

                    ReportPilotFpRemarks fpRemarks = null;
                    String fpRemarksStr = pilotInfo.getPlannedRemarks();
                    fpRemarksStr = fpRemarksStr != null ? fpRemarksStr.trim() : null;
                    if (fpRemarksStr != null && fpRemarksStr.trim().length() > 0) {
                        fpRemarks = findFpRemarks(session, fpRemarksStr);

                        if (fpRemarks == null) {
                            fpRemarks = new ReportPilotFpRemarks();
                            fpRemarks.setRemarks(fpRemarksStr);

                            save(session, fpRemarks, "createFpRemarks");
                        }
                    }

                    ReportPilotPosition p = new ReportPilotPosition();
                    p.setReport(_report);
                    p.setPilotNumber(pilotNumber);
                    p.setCallsign(pilotInfo.getCallsign());
                    p.setLatitude(pilotInfo.getLatitude());
                    p.setLongitude(pilotInfo.getLongitude());
                    p.setAltitude(pilotInfo.getAltitude());
                    p.setGroundspeed(pilotInfo.getGroundspeed());
                    p.setHeading(pilotInfo.getHeading());
                    p.setFpAircraft(pilotInfo.getPlannedAircraft());
                    p.setFpOrigin(pilotInfo.getPlannedDepAirport());
                    p.setFpDestination(pilotInfo.getPlannedDestAirport());
                    p.setFpRemarks(fpRemarks);
                    p.setParsedRegNo(ParsingLogics.parseRegNo(p, fpRemarksStr));
                    p.setQnhMb(pilotInfo.getQnhMb());
                    p.setOnGround(pilotInfo.isOnGround());

                    save(session, p, "createPosition");

                    existingPositions.add(p);
                } catch (Exception e) {
                    String msg = "Error on saving data for PID " + pilotInfo.getCid();
                    logger.error(msg, e);
                    throw new RuntimeException(msg, e);
                }
            }
            logger.debug(ReportOps.logMsg(report, "      Pilot positions inserted"));


            _report.setParsed(true);

            save(session, _report, "markReportParsed");

            logger.info(ReportOps.logMsg(report, "Parsed"));

        } finally {
            BM.stop();
        }
    }

    private ReportPilotFpRemarks findFpRemarks(Session session, String fpRemarksStr) {
        BM.start("Parse.findFpRemarks");
        try {
            //noinspection JpaQlInspection
            return (ReportPilotFpRemarks) session
                    .createQuery("select r from ReportPilotFpRemarks r where r.remarks = :remarks")
                    .setString("remarks", fpRemarksStr)
                    .uniqueResult();
        } finally {
            BM.stop();
        }
    }

    @SuppressWarnings("unchecked")
    private List<ReportLogEntry> loadExistingLogEntries(Session session, Report _report) {
        BM.start("Parse.loadExistingLogEntries");
        try {
            //noinspection JpaQlInspection
            return session
                    .createQuery("select l from ReportLogEntry l where l.report = :report")
                    .setEntity("report", _report)
                    .list();
        } finally {
            BM.stop();
        }
    }

    private void save(Session session, Object entity, String stage) {
        BM.start("Parse.save/" + stage);
        try {
            session.getTransaction().begin();
            session.save(entity);
            session.getTransaction().commit();
        } finally {
            BM.stop();
        }
    }

    private Report findReport(Session session, String report) {
        BM.start("Parse.findReport");
        try {
            // there is no condition for parsed because it can append records to partially parsed report

            // noinspection JpaQlInspection
            return (Report) session
                    .createQuery("select r from Report r where r.report = :report")
                    .setString("report", report)
                    .uniqueResult();
        } finally {
            BM.stop();
        }
    }
}
