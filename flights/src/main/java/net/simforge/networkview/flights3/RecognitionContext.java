package net.simforge.networkview.flights3;

import net.simforge.commons.legacy.BM;
import net.simforge.networkview.datafeeder.ReportUtils;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import net.simforge.networkview.flights.datasource.ReportDatasource;
import net.simforge.networkview.flights2.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public class RecognitionContext {

    private static Logger logger = LoggerFactory.getLogger(RecognitionContext.class.getName());

    private ReportDatasource reportDatasource;
    private PersistenceLayer persistenceLayer;
    private Report lastProcessedReport;
    private Map<Integer, PilotContext> pilotContexts = new HashMap<>();

    public RecognitionContext(ReportDatasource reportDatasource, PersistenceLayer persistenceLayer, Report lastProcessedReport) {
        this.reportDatasource = reportDatasource;
        this.persistenceLayer = persistenceLayer;
        this.lastProcessedReport = lastProcessedReport;
    }

    public void loadActivePilotContexts() throws IOException {
        BM.start("RecognitionContext.loadActivePilotContexts");
        try {

            if (lastProcessedReport == null) {
                return; // nothing to do
            }

            logger.info("Loading pilot contexts...");
            List<PilotContext> loadedPilotContexts = persistenceLayer.loadActivePilotContexts(ReportUtils.fromTimestampJava(lastProcessedReport.getReport()));
            logger.info("Loaded {} pilot contexts, processing missing positions.....", loadedPilotContexts.size());

            long lastDt = System.currentTimeMillis();
            int done = 0;
            for (PilotContext loadedPilotContext : loadedPilotContexts) {
                PilotContext pilotContext = processMissingPositions(loadedPilotContext);
                pilotContexts.put(pilotContext.getPilotNumber(), pilotContext);
                done++;

                long now = System.currentTimeMillis();
                if (now - lastDt > 10000) {
                    logger.info("    {} % done", Math.round((100.0 * done) / loadedPilotContexts.size()));
                    lastDt = now;
                }
            }

            logger.info("All done");

        } finally {
            BM.stop();
        }
    }

    public void processNextReport() throws IOException {
        BM.start("RecognitionContext.processNextReport");
        try {

            Report report = reportDatasource.loadNextReport(lastProcessedReport != null ? lastProcessedReport.getReport() : null);
            if (report == null) {
                logger.info("There is no any report to process");
                return;
            }

            logger.info("Processing report {} (id {})...", report.getReport(), report.getId());

            List<ReportPilotPosition> pilotPositions = reportDatasource.loadPilotPositions(report.getId());

            Map<Integer, PilotContext> newPilotContexts = new HashMap<>();

            BM.start("RecognitionContext.processReports/online");
            try {

                for (ReportPilotPosition pilotPosition : pilotPositions) {
                    int pilotNumber = pilotPosition.getPilotNumber();

                    PilotContext pilotContext = pilotContexts.get(pilotNumber);
                    if (pilotContext == null) {
                        pilotContext = persistenceLayer.loadContext(pilotNumber);
                        if (pilotContext != null) {
                            pilotContext = processMissingPositions(pilotContext);
                        } else {
                            pilotContext = persistenceLayer.createContext(pilotNumber, report);
                        }
                    }

                    PilotContext dirtyPilotContext = pilotContext.processPosition(report, pilotPosition);

                    PilotContext newPilotContext;
                    if (dirtyPilotContext.isDirty() || !dirtyPilotContext.isActive()) {
                        newPilotContext = persistenceLayer.saveChanges(dirtyPilotContext);
                    } else {
                        newPilotContext = dirtyPilotContext;
                    }

                    if (newPilotContext.isActive()) {
                        newPilotContexts.put(pilotNumber, newPilotContext);
                    }
                }

            } finally {
                BM.stop();
            }

            BM.start("RecognitionContext.processReports/offline");
            try {

                Set<Integer> pilotNumbersWithoutPositions = new TreeSet<>(pilotContexts.keySet());
                pilotNumbersWithoutPositions.removeAll(newPilotContexts.keySet());

                for (Integer pilotNumber : pilotNumbersWithoutPositions) {
                    PilotContext pilotContext = pilotContexts.get(pilotNumber);

                    PilotContext dirtyPilotContext = pilotContext.processPosition(report, null);

                    PilotContext newPilotContext;
                    if (dirtyPilotContext.isDirty() || !dirtyPilotContext.isActive()/* || Math.random() < 0.02*/) { // todo Math.random() < ... has to be replaced by counter in PilotContext
                        newPilotContext = persistenceLayer.saveChanges(dirtyPilotContext);
                    } else {
                        newPilotContext = dirtyPilotContext;
                    }

                    if (newPilotContext.isActive()) {
                        newPilotContexts.put(pilotNumber, newPilotContext);
                    }
                }

            } finally {
                BM.stop();
            }

            pilotContexts = newPilotContexts;

            lastProcessedReport = report;

        } finally {
            BM.stop();
        }
    }

    private PilotContext processMissingPositions(PilotContext pilotContext) {
        BM.start("RecognitionContext.processMissingPositions");
        try {
            Position lastProcessedPosition = pilotContext.getLastProcessedPosition();

            if (lastProcessedReport == null || lastProcessedPosition.getReportId() >= lastProcessedReport.getId()) {
                return pilotContext;
            }

            long fromReportId = lastProcessedPosition.getReportId();
            long toReportId = lastProcessedReport.getId();

            List<Report> reports = reportDatasource.loadReports(fromReportId, toReportId);
            List<ReportPilotPosition> reportPilotPositions = reportDatasource.loadPilotPositions(pilotContext.getPilotNumber(), fromReportId, toReportId);
            Map<Long, ReportPilotPosition> reportPilotPositionMap = reportPilotPositions.stream().collect(toMap(p -> p.getReport().getId(), Function.identity()));

            for (Report report : reports) {
                ReportPilotPosition reportPilotPosition = reportPilotPositionMap.get(report.getId());

                PilotContext dirtyPilotContext = pilotContext.processPosition(report, reportPilotPosition);

                boolean stopNow = report.getId().equals(lastProcessedReport.getId());

                if (dirtyPilotContext.isDirty() || stopNow) {
                    pilotContext = persistenceLayer.saveChanges(dirtyPilotContext);
                } else {
                    pilotContext = dirtyPilotContext;
                }
            }

/*            Report currReport = reportDatasource.loadReport(lastProcessedPosition.getReportId());
            while (true) {
                currReport = reportDatasource.loadNextReport(currReport.getReport());

                if (currReport == null) {
                    throw new IllegalStateException("Unable to complete a processing of missing positions");
                }

                ReportPilotPosition reportPilotPosition = reportDatasource.loadPilotPosition(currReport.getId(), pilotContext.getPilotNumber());

                PilotContext dirtyPilotContext = pilotContext.processPosition(currReport, reportPilotPosition);

                boolean stopNow = currReport.getId().equals(lastProcessedReport.getId());

                if (dirtyPilotContext.isDirty() || stopNow) {
                    pilotContext = persistenceLayer.saveChanges(dirtyPilotContext);
                } else {
                    pilotContext = dirtyPilotContext;
                }

                if (stopNow) {
                    // yeah, we've done!
                    break;
                }
            }*/
//        } catch (IOException e) {
//            logger.error("Error on a processing of missing positions", e);
        } finally {
            BM.stop();
        }

        return pilotContext;
    }

    public Report getLastProcessedReport() {
        return lastProcessedReport;
    }

    public PilotContext getPilotContext(int pilotNumber) {
        return pilotContexts.get(pilotNumber);
    }
}
