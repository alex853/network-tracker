package net.simforge.networkview.flights2;

import net.simforge.commons.legacy.BM;
import net.simforge.networkview.datafeeder.ReportUtils;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import net.simforge.networkview.flights.datasource.ReportDatasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public class MainContext {

    private static Logger logger = LoggerFactory.getLogger(MainContext.class.getName());

    private ReportDatasource reportDatasource;
    private PersistenceLayer persistenceLayer;
    private Report lastReport;
    private Map<Integer, PilotContext> pilotContexts = new HashMap<>();

    public MainContext(ReportDatasource reportDatasource, PersistenceLayer persistenceLayer) {
        this.reportDatasource = reportDatasource;
        this.persistenceLayer = persistenceLayer;
    }

    public void loadActivePilotContexts() throws IOException {
        BM.start("MainContext.loadActivePilotContexts");
        try {

            if (lastReport == null) {
                return; // nothing to do
            }
            List<PilotContext> loadedPilotContexts = persistenceLayer.loadActivePilotContexts(ReportUtils.fromTimestampJava(lastReport.getReport()));
            loadedPilotContexts.stream().forEach(this::processMissingPositions);
            pilotContexts = loadedPilotContexts.stream().collect(toMap(PilotContext::getPilotNumber, Function.identity()));

        } finally {
            BM.stop();
        }
    }

    public int processReports(int maxReports) throws IOException {
        BM.start("MainContext.processReports");
        try {

            int processedReports = 0;
            while (processedReports < maxReports) {
                Report report = reportDatasource.loadNextReport(lastReport != null ? lastReport.getReport() : null);
                if (report == null) {
                    logger.info("There is no any report to process");
                    break;
                }

                logger.info("Processing report {} (id {})...", report.getReport(), report.getId());

                List<ReportPilotPosition> pilotPositions = reportDatasource.loadPilotPositions(report.getId());

                Map<Integer, PilotContext> newPilotContexts = new HashMap<>();

                BM.start("MainContext.processReports/online");
                try {

                    for (ReportPilotPosition pilotPosition : pilotPositions) {
                        int pilotNumber = pilotPosition.getPilotNumber();

                        PilotContext pilotContext = pilotContexts.get(pilotNumber);
                        if (pilotContext == null) {
                            pilotContext = persistenceLayer.loadContext(pilotNumber);
                            if (pilotContext != null) {
                                processMissingPositions(pilotContext);
                            } else {
                                pilotContext = persistenceLayer.createContext(pilotNumber, report);
                            }
                        }

                        PilotContext dirtyPilotContext = pilotContext.processPosition(report, pilotPosition);
                        PilotContext newPilotContext = persistenceLayer.saveChanges(dirtyPilotContext);

                        if (newPilotContext.isActive(report)) {
                            newPilotContexts.put(pilotNumber, newPilotContext);
                        }
                    }

                } finally {
                    BM.stop();
                }

                BM.start("MainContext.processReports/offline");
                try {

                    Set<Integer> pilotNumbersWithoutPositions = new TreeSet<>(pilotContexts.keySet());
                    pilotNumbersWithoutPositions.removeAll(newPilotContexts.keySet());

                    for (Integer pilotNumber : pilotNumbersWithoutPositions) {
                        PilotContext pilotContext = pilotContexts.get(pilotNumber);

                        PilotContext dirtyPilotContext = pilotContext.processPosition(report, null);
                        PilotContext newPilotContext = persistenceLayer.saveChanges(dirtyPilotContext);

                        if (newPilotContext.isActive(report)) {
                            newPilotContexts.put(pilotNumber, newPilotContext);
                        }
                    }

                } finally {
                    BM.stop();
                }

                pilotContexts = newPilotContexts;

                processedReports++;

                lastReport = report;
            }

            return processedReports;

        } finally {
            BM.stop();
        }
    }

    private void processMissingPositions(PilotContext pilotContext) {
        BM.start("MainContext.processMissingPositions");
        try {
            Position lastProcessedPosition = pilotContext.getCurrPosition();

            if (lastReport == null || lastProcessedPosition.getReportId() >= lastReport.getId()) {
                return;
            }

            Report currReport = reportDatasource.loadReport(lastProcessedPosition.getReportId());
            while (true) {
                currReport = reportDatasource.loadNextReport(currReport.getReport());

                if (currReport == null) {
                    throw new IllegalStateException("Unable to complete a processing of missing positions");
                }

                ReportPilotPosition reportPilotPosition = reportDatasource.loadPilotPosition(currReport.getId(), pilotContext.getPilotNumber());
                pilotContext.processPosition(currReport, reportPilotPosition);

                if (currReport.getId().equals(lastReport.getId())) {
                    // yeah, we've done!
                    return;
                }
            }
        } catch (IOException e) {
            logger.error("Error on a processing of missing positions", e);
        } finally {
            BM.stop();
        }
    }

    public Report getLastReport() {
        return lastReport;
    }

    public void setLastReport(Report lastReport) {
        this.lastReport = lastReport;
    }

    public PilotContext getPilotContext(int pilotNumber) {
        return pilotContexts.get(pilotNumber);
    }
}
