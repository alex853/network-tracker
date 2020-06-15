package net.simforge.networkview.flights.method.eventbased;

import net.simforge.commons.legacy.BM;
import net.simforge.networkview.core.report.ReportUtils;
import net.simforge.networkview.core.report.persistence.Report;
import net.simforge.networkview.core.report.persistence.ReportPilotPosition;
import net.simforge.networkview.flights.method.eventbased.datasource.ReportDatasource;
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

    RecognitionContext(ReportDatasource reportDatasource, PersistenceLayer persistenceLayer, Report lastProcessedReport) {
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
//                logger.info("There is no any report to process");
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
                            pilotContext = new PilotContext(pilotNumber);
                        }
                    }

                    Position lastProcessedPosition = pilotContext.getLastProcessedPosition();
                    if (lastProcessedPosition == null || lastProcessedPosition.getReportInfo().getDt().isBefore(report.getDt())) {
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
                    } else {
                        // it seems like we already processed this position for this pilot and then the process stopped
                        // and now we are restarting, some of pilots will be already processed
                        newPilotContexts.put(pilotNumber, pilotContext);
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

                    Position lastProcessedPosition = pilotContext.getLastProcessedPosition();
                    if (lastProcessedPosition == null || lastProcessedPosition.getReportInfo().getDt().isBefore(report.getDt())) {
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
                    } else {
                        // it seems like we already processed this position for this pilot and then the process stopped
                        // and now we are restarting, some of pilots will be already processed
                        newPilotContexts.put(pilotNumber, pilotContext);
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

            if (lastProcessedReport == null || lastProcessedPosition.getReportInfo().getId() >= lastProcessedReport.getId()) {
                return pilotContext;
            }

            long fromReportId = lastProcessedPosition.getReportInfo().getId();
            long toReportId = lastProcessedReport.getId();

            List<Report> reports = reportDatasource.loadReports(fromReportId, toReportId);
            List<ReportPilotPosition> reportPilotPositions = reportDatasource.loadPilotPositions(pilotContext.getPilotNumber(), fromReportId, toReportId);
            Map<Long, ReportPilotPosition> reportPilotPositionMap = reportPilotPositions.stream().collect(toMap(p -> p.getReport().getId(), Function.identity()));

            for (Report report : reports) {
                if (report.getId().equals(fromReportId)) {
                    // skip already processed report
                    continue;
                }

                ReportPilotPosition reportPilotPosition = reportPilotPositionMap.get(report.getId());

                PilotContext dirtyPilotContext = pilotContext.processPosition(report, reportPilotPosition);

                boolean stopNow = report.getId().equals(lastProcessedReport.getId());

                if (dirtyPilotContext.isDirty() || stopNow) {
                    pilotContext = persistenceLayer.saveChanges(dirtyPilotContext);
                } else {
                    pilotContext = dirtyPilotContext;
                }
            }
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
