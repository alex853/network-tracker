package net.simforge.networkview.flights2;

import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import net.simforge.networkview.flights.datasource.ReportDatasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class MainContext {

    private static Logger logger = LoggerFactory.getLogger(MainContext.class.getName());

    private ReportDatasource reportDatasource; // todo
    private PersistenceLayer persistenceLayer; // todo
    private Report lastReport;
    private Map<Integer, PilotContext> pilotContexts = new HashMap<>();

    public MainContext(ReportDatasource reportDatasource, PersistenceLayer persistenceLayer) {
        this.reportDatasource = reportDatasource;
        this.persistenceLayer = persistenceLayer;
    }

    public void loadActivePilotContexts() {
        throw new UnsupportedOperationException("MainContext.loadContexts");
        // todo also inits lastReport
    }

    public int processReports(int maxReports) throws IOException {
        int processedReports = 0;
        while (processedReports < maxReports) {
            Report report = reportDatasource.loadNextReport(lastReport != null ? lastReport.getReport() : null);
            if (report == null) {
                logger.info("No more reports found");
                break;
            }

            logger.info("Processing report {} (id {})...", report.getReport(), report.getId());

            List<ReportPilotPosition> pilotPositions = reportDatasource.loadPilotPositions(report.getId());

            Map<Integer, PilotContext> newPilotContexts = new HashMap<>();

            for (ReportPilotPosition pilotPosition : pilotPositions) {
                PilotContext pilotContext = pilotContexts.get(pilotPosition.getPilotNumber());
                if (pilotContext == null) {
                    pilotContext = persistenceLayer.loadContext(pilotContext.getPilotNumber());
                    if (pilotContext == null) {
                        pilotContext = persistenceLayer.createContext(pilotContext.getPilotNumber());
                    }
                }

                PilotContext dirtyPilotContext = pilotContext.processPosition(report, pilotPosition);
                PilotContext newPilotContext = persistenceLayer.saveChanges(dirtyPilotContext);

                if (newPilotContext.isActive()) {
                    newPilotContexts.put(pilotPosition.getPilotNumber(), newPilotContext);
                }
            }

            Set<Integer> pilotNumbersWithoutPositions = new TreeSet<>(pilotContexts.keySet());
            pilotNumbersWithoutPositions.removeAll(newPilotContexts.keySet());

            for (Integer pilotNumber : pilotNumbersWithoutPositions) {
                PilotContext pilotContext = pilotContexts.get(pilotNumber);

                PilotContext dirtyPilotContext = pilotContext.processPosition(report, null);
                PilotContext newPilotContext = persistenceLayer.saveChanges(dirtyPilotContext);

                if (newPilotContext.isActive()) {
                    newPilotContexts.put(pilotNumber, newPilotContext);
                }
            }

            pilotContexts = newPilotContexts;

            processedReports++;

            lastReport = report;
        }

        return processedReports;
    }

}
