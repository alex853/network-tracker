package net.simforge.networkview.datafeeder;

import net.simforge.commons.hibernate.HibernateUtils;
import net.simforge.commons.legacy.BM;
import net.simforge.commons.io.Marker;
import net.simforge.commons.runtime.BaseTask;
import net.simforge.commons.runtime.RunningMarker;
import net.simforge.commons.runtime.ThreadMonitor;
import net.simforge.networkview.Network;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportPilotFpRemarks;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.Expirations;
import org.hibernate.Session;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Archive extends BaseTask {
    private static final String ARG_NETWORK = "network";

    private static final int REPORT_EVERY_N_MINUTES = 10;

    private SessionManager sessionManager;
    private Network network;
    private Marker reportMarker;

    private Map<Integer, PilotTrack> pilotTracks = new HashMap<>();
    private boolean firstReport = true;

    private CacheManager cacheManager;
    private Cache<String, Report> archivedReportsCache;
    private Cache<String, ReportPilotFpRemarks> archivedFpRemarksCache;

    @SuppressWarnings("unused")
    public Archive(Properties properties) {
        this(DatafeederTasks.getSessionManager(), Network.valueOf(properties.getProperty(ARG_NETWORK)));
    }

    @SuppressWarnings("WeakerAccess")
    public Archive(SessionManager sessionManager, Network network) {
        super("Archive-" + network);
        this.sessionManager = sessionManager;
        this.network = network;
    }

    @Override
    protected void startup() {
        super.startup();

        BM.setLoggingPeriod(TimeUnit.HOURS.toMillis(1));
//        BM.setLoggingPeriod(TimeUnit.MINUTES.toMillis(5));

        RunningMarker.lock(getTaskName());

        reportMarker = new Marker(getTaskName());

        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
        archivedReportsCache = cacheManager.createCache("archivedReportsCache-" + network,
                CacheConfigurationBuilder
                        .newCacheConfigurationBuilder(
                                String.class,
                                Report.class,
                                ResourcePoolsBuilder.heap(1000))
                        .withExpiry(Expirations.timeToIdleExpiration(org.ehcache.expiry.Duration.of(10, TimeUnit.MINUTES)))
                        .build());
        archivedFpRemarksCache = cacheManager.createCache("archivedFpRemarksCache-" + network,
                CacheConfigurationBuilder
                        .newCacheConfigurationBuilder(
                                String.class,
                                ReportPilotFpRemarks.class,
                                ResourcePoolsBuilder.heap(10000))
                        .withExpiry(Expirations.timeToIdleExpiration(org.ehcache.expiry.Duration.of(10, TimeUnit.MINUTES)))
                        .build());

        setBaseSleepTime(1000);
    }

    @Override
    protected void shutdown() {
        super.shutdown();

        cacheManager.close();
    }

    @Override
    protected void process() {
        BM.start("Archive.process");
        try (Session liveSession = sessionManager.getSession(network)) {
            Report report;

            String lastProcessedReport = reportMarker.getString();
            if (lastProcessedReport == null) {
                report = ReportOps.loadFirstReport(liveSession);
            } else {
                report = ReportOps.loadNextReport(liveSession, lastProcessedReport);
            }

            if (report == null) {
                return; // standard sleep time
            }

            logger.debug(ReportOps.logMsg(report.getReport(), "Archiving..."));

            buildPilotTracks(liveSession, report);

            try (Session archiveSession = sessionManager.getSession(network, report.getReport())) {
                createArchivedReport(archiveSession, report);
            }

            savePositionsToArchive();

            logger.info(ReportOps.logMsg(report.getReport(), "Archived"));

            cleanupObsolete(report);

            reportMarker.setString(report.getReport());

            setNextSleepTime(1000); // short sleep time
        } finally {
            BM.stop();
        }
    }

    private Report getArchivedReport(Session archiveSession, String report) {
        BM.start("Archive.getArchivedReport");
        try {
            Report archivedReport = archivedReportsCache.get(report);
            if (archivedReport != null) {
                // BM tag
                BM.start("#cache-hit");
                BM.stop();

                return archivedReport;
            }

            archivedReport = ReportOps.loadParsedReport(archiveSession, report);
            if (archivedReport != null) {
                // BM tag
                BM.start("#loaded-from-db");
                BM.stop();

                archivedReportsCache.put(report, archivedReport);
            }

            return archivedReport;
        } finally {
            BM.stop();
        }
    }

    private void createArchivedReport(Session archiveSession, Report report) {
        BM.start("Archive.createArchivedReport");
        try {
            Report archivedReport = getArchivedReport(archiveSession, report.getReport());
            if (archivedReport != null) {
                return;
            }

            archivedReport = copy(report);
            archivedReport.setId(null);
            archivedReport.setVersion(null);

            archiveSession.getTransaction().begin();
            archiveSession.save(archivedReport);
            archiveSession.getTransaction().commit();

            archivedReportsCache.put(report.getReport(), archivedReport);
        } finally {
            BM.stop();
        }
    }

    private ReportPilotFpRemarks getArchivedFpRemarks(Session archiveSession, int reportYear, ReportPilotFpRemarks fpRemarks) {
        BM.start("Archive.getArchivedFpRemarks");
        try {
            ReportPilotFpRemarks archivedFpRemarks = archivedFpRemarksCache.get(reportYear + fpRemarks.getRemarks());
            if (archivedFpRemarks != null) {
                // BM tag
                BM.start("#cache-hit");
                BM.stop();

                return archivedFpRemarks;
            }

            archivedFpRemarks = ReportOps.loadFpRemarks(archiveSession, fpRemarks.getRemarks());
            if (archivedFpRemarks != null) {
                // BM tag
                BM.start("#loaded-from-db");
                BM.stop();

                archivedFpRemarksCache.put(reportYear + archivedFpRemarks.getRemarks(), archivedFpRemarks);
                return archivedFpRemarks;
            }

            final ReportPilotFpRemarks archivedFpRemarksCopy = copy(fpRemarks);
            archivedFpRemarksCopy.setId(null);
            archivedFpRemarksCopy.setVersion(null);

            HibernateUtils.saveAndCommit(archiveSession, "#save", archivedFpRemarksCopy);

            archivedFpRemarksCache.put(reportYear + archivedFpRemarksCopy.getRemarks(), archivedFpRemarksCopy);
            return archivedFpRemarksCopy;
        } finally {
            BM.stop();
        }
    }

    private void savePositionsToArchive() {
        BM.start("Archive.savePositionsToArchive");
        try {
            for (PilotTrack pilotTrack : pilotTracks.values()) {
                List<PositionInfo> positions = pilotTrack.getPositions();
                for (PositionInfo position : positions) {
                    PositionStatus status = position.getStatus();

                    if (!(status == PositionStatus.PositionReport || status == PositionStatus.TakeoffLanding)) {
                        continue;
                    }

                    if (position.hasArchivedCopy()) {
                        continue;
                    }

                    String report = position.getReportPilotPosition().getReport().getReport();
                    try (Session archiveSession = sessionManager.getSession(network, report)) {
                        Report archivedReport = getArchivedReport(archiveSession, report);

                        ReportPilotPosition archivedPositionCopy = ReportOps.loadPilotPosition(archiveSession, archivedReport, pilotTrack.getPilotNumber());

                        if (archivedPositionCopy != null) {
                            position.setHasArchivedCopy();
                            continue;
                        }

                        archivedPositionCopy = copy(position.getReportPilotPosition());
                        archivedPositionCopy.setId(null);
                        archivedPositionCopy.setVersion(null);
                        archivedPositionCopy.setReport(archivedReport);

                        ReportPilotFpRemarks fpRemarks = position.getReportPilotPosition().getFpRemarks();
                        if (fpRemarks != null) {
                            int reportYear = ReportUtils.fromTimestampJava(report).getYear();
                            archivedPositionCopy.setFpRemarks(getArchivedFpRemarks(archiveSession, reportYear, fpRemarks));
                        } else {
                            archivedPositionCopy.setFpRemarks(null);
                        }

                        archiveSession.getTransaction().begin();
                        archiveSession.save(archivedPositionCopy);
                        archiveSession.getTransaction().commit();
                        position.setHasArchivedCopy();
                    }
                }
            }
        } finally {
            BM.stop();
        }
    }

    private void buildPilotTracks(Session session, Report currentReport) {
        BM.start("Archive.buildPilotTracks");
        try {
            List<ReportPilotPosition> currentPositions = ReportOps.loadPilotPositions(session, currentReport);
            logger.debug(ReportOps.logMsg(currentReport.getReport(), "    loaded {} positions"), currentPositions.size());

            Map<Long, List<ReportPilotPosition>> previousReports = null;

            for (ReportPilotPosition currentPosition : currentPositions) {
                Integer pilotNumber = currentPosition.getPilotNumber();
                PilotTrack pilotTrack = pilotTracks.get(pilotNumber);
                if (pilotTrack == null) {
                    if (firstReport) {
                        previousReports = loadPreviousLiveReports(session, currentReport);

                        firstReport = false;
                    }

                    pilotTrack = new PilotTrack(currentPosition.getPilotNumber());

                    if (previousReports != null) {
                        for (List<ReportPilotPosition> reportPilotPositions : previousReports.values()) {
                            ReportPilotPosition position = reportPilotPositions.stream().filter(eachPosition -> eachPosition.getPilotNumber().equals(pilotNumber)).findFirst().orElse(null);
                            if (position != null) {
                                pilotTrack.addPosition(position);
                            }
                        }
                    }

                    pilotTracks.put(pilotNumber, pilotTrack);
                }

                pilotTrack.addPosition(currentPosition);

                ThreadMonitor.alive();
            }
        } finally {
            BM.stop();
        }
    }

    private Map<Long, List<ReportPilotPosition>> loadPreviousLiveReports(Session session, Report currentReport) {
        Map<Long, List<ReportPilotPosition>> previousReports = new TreeMap<>();

        logger.debug("Loading bunch of previous reports...");
        int previousReportsCounter = 180;
        Report previousReport = currentReport;
        while (previousReportsCounter > 0) {
            previousReport = ReportOps.loadPrevReport(session, previousReport.getReport());
            if (previousReport == null) {
                break;
            }
            List<ReportPilotPosition> previousReportPositions = ReportOps.loadPilotPositions(session, previousReport);

            previousReports.put(previousReport.getId(), previousReportPositions);
            previousReportsCounter--;

            ThreadMonitor.alive();
        }
        logger.debug("Loading done");

        return previousReports;
    }

    private void cleanupObsolete(Report report) {
        BM.start("Archive.cleanupObsolete");
        try {
            int totalPositions = 0;

            // to cut everything older than 24 days
            // however to remain at least 1 position in "confirmed" state - Waypoint, TakeoffLanding
            // so most of tracks will have a state like:
            //   Waypoint
            //   Unknown (as last position is always in Unknown state)

            for (PilotTrack pilotTrack : pilotTracks.values()) {
                PositionInfo lastSavedPosition = pilotTrack.getLastIn(PositionStatus.PositionReport, PositionStatus.TakeoffLanding);
                if (lastSavedPosition == null) {
                    totalPositions += pilotTrack.getPositions().size();
                    continue;
                }

                List<PositionInfo> toRemove = new ArrayList<>();
                List<PositionInfo> positions = pilotTrack.getPositions();
                for (PositionInfo position : positions) {
                    if (position == lastSavedPosition) {
                        break;
                    }

                    toRemove.add(position);
                }

                pilotTrack.removePositions(toRemove);

                totalPositions += pilotTrack.getPositions().size();
            }

            logger.info(ReportOps.logMsg(report.getReport(), "") + "\t\t\t\tStats | {} tracks, {} positions, avg {} per track, reports {}, remarks {}",
                    pilotTracks.size(),
                    totalPositions,
                    String.format("%.1f", totalPositions / (double) (pilotTracks.size() > 0 ? pilotTracks.size() : 1)),
                    CacheHelper.getEstimatedCacheSize(this.archivedReportsCache),
                    CacheHelper.getEstimatedCacheSize(this.archivedFpRemarksCache)
            );
        } finally {
            BM.stop();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T copy(T src) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(src);
            oos.close();

            byte[] bytes = bos.toByteArray();

            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
            Object o = ois.readObject();
            return (T) o;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class PilotTrack {
        private int pilotNumber;
        private List<PositionInfo> positions = new ArrayList<>();

        PilotTrack(int pilotNumber) {
            this.pilotNumber = pilotNumber;
        }

        void addPosition(ReportPilotPosition position) {
            positions.add(new PositionInfo(position));
            updateStatuses();
        }

        private void updateStatuses() {
            for (int i = 0; i < positions.size(); i++) {
                PositionInfo current = positions.get(i);

                if (current.getStatus() != PositionStatus.Unknown) {
                    continue;
                }

                if (i == 0) {
                    current.setStatus(PositionStatus.PositionReport);
                    continue;
                }

                PositionInfo previous = positions.get(i - 1);

                Position currentPP = current.getPosition();
                Position previousPP = previous.getPosition();

                if (currentPP.isOnGround() && !previousPP.isOnGround()
                        || !currentPP.isOnGround() && previousPP.isOnGround()) {
                    // we have landing or airborne, lets have archive it
                    current.setStatus(PositionStatus.TakeoffLanding);
                    previous.setStatus(PositionStatus.TakeoffLanding);
                    continue;
                }

                PositionInfo previousSaved = getLastIn(PositionStatus.TakeoffLanding, PositionStatus.PositionReport);
                if (previousSaved == null) {
                    throw new IllegalStateException("Could not find previous saved position");
                }
                Position previousSavedPP = previousSaved.getPosition();

                Duration difference = Duration.between(previousSavedPP.getDt(), currentPP.getDt());
                long differenceMillis = difference.toMillis();

                if (differenceMillis > TimeUnit.MINUTES.toMillis(REPORT_EVERY_N_MINUTES)) {
                    current.setStatus(PositionStatus.PositionReport);
                } else {
                    current.setStatus(PositionStatus.Excessive);
                }
            }
        }

        private PositionInfo getLastIn(PositionStatus... statuses) {
            for (int j = positions.size() - 1; j >= 0; j--) {
                PositionInfo positionInfo = positions.get(j);

                PositionStatus status = positionInfo.getStatus();
                for (PositionStatus eachStatus : statuses) {
                    if (status == eachStatus) {
                        return positionInfo;
                    }
                }
            }

            return null;
        }

        void removePositions(List<PositionInfo> positions) {
            this.positions.removeAll(positions);
        }

        List<PositionInfo> getPositions() {
            return positions;
        }

        int getPilotNumber() {
            return pilotNumber;
        }
    }

    private static class PositionInfo {
        private PositionStatus status = PositionStatus.Unknown;
        private ReportPilotPosition reportPilotPosition;
        private Position position;
        private boolean hasArchivedCopy;

        PositionInfo(ReportPilotPosition reportPilotPosition) {
            this.reportPilotPosition = reportPilotPosition;
            this.position = Position.create(reportPilotPosition);
        }

        ReportPilotPosition getReportPilotPosition() {
            return reportPilotPosition;
        }

        Position getPosition() {
            return position;
        }

        PositionStatus getStatus() {
            return status;
        }

        void setStatus(PositionStatus status) {
            this.status = status;
        }

        boolean hasArchivedCopy() {
            return hasArchivedCopy;
        }

        void setHasArchivedCopy() {
            this.hasArchivedCopy = true;
        }

        @Override
        public String toString() {
            return "Position " + position.getDt() + ", " + (position.isOnGround() ? "On Ground" : "Flying") + ", " + status;
        }
    }

    enum PositionStatus {
        Unknown, TakeoffLanding, PositionReport, Excessive
    }
}
