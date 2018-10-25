package net.simforge.networkview.flights.model;

import net.simforge.networkview.TrackerUtil;
import net.simforge.networkview.datafeeder.ReportUtils;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import net.simforge.networkview.flights.datasource.ReportDatasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.*;

public class MainContext {

    private static Logger logger = LoggerFactory.getLogger(MainContext.class.getName());

    private ReportDatasource reportDatasource;
    private Strategy strategy;
    private Report lastReport;
    private Map<Integer, PilotContext> pilotContexts = new HashMap<>();

    public ReportDatasource getReportDatasource() {
        return reportDatasource;
    }

    public void setReportDatasource(ReportDatasource reportDatasource) {
        this.reportDatasource = reportDatasource;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public int processReports(int maxReports) throws IOException {
        int processedReports = 0;
        while (processedReports < maxReports) {
            Report report = reportDatasource.loadNextReport(lastReport != null ? lastReport.getReport() : null);
            if (report == null) {
                logger.info("No more reports found");
                break;
            }

            processReport(report);
            processedReports++;
            lastReport = report;
        }
        return processedReports;
    }

    private void processReport(Report report) throws IOException {
        logger.info("Processing report {} (id {})...", report.getReport(), report.getId());

        List<ReportPilotPosition> pilotPositions = reportDatasource.loadPilotPositions(report.getId());

        Map<Integer, PilotContext> newPilotContexts = new HashMap<>();

        for (ReportPilotPosition pilotPosition : pilotPositions) {
            PilotContext pilotContext = pilotContexts.get(pilotPosition.getPilotNumber());
            if (pilotContext == null) {
                pilotContext = PilotContext.create(this, pilotPosition.getPilotNumber());
                strategy.initPilotContext(pilotContext, pilotPosition);
            }
            processPilot(pilotContext, report, pilotPosition);
            newPilotContexts.put(pilotPosition.getPilotNumber(), pilotContext);
        }

        Set<Integer> pilotNumbersWithoutPositions = new TreeSet<>(pilotContexts.keySet());
        pilotNumbersWithoutPositions.removeAll(newPilotContexts.keySet());

        for (Integer pilotNumber : pilotNumbersWithoutPositions) {
            PilotContext pilotContext = pilotContexts.get(pilotNumber);
            processPilot(pilotContext, report, null);
            newPilotContexts.put(pilotNumber, pilotContext);
        }

        pilotContexts = newPilotContexts;
    }

    private void processPilot(PilotContext pilotContext, Report report, ReportPilotPosition pilotPosition) {
        pilotContext.processReport(report, pilotPosition);
        strategy.onPilotContextProcessed(pilotContext);
    }

    public double getTimeBetween(long fromReportId, long toReportId) {
        Report fromReport;
        try {
            fromReport = reportDatasource.loadReport(fromReportId);
        } catch (IOException e) {
            throw new RuntimeException("Error during report loading", e);
        }
        if (fromReport == null) {
            throw new IllegalArgumentException("Can't find 'from' report by id " + fromReportId);
        }

        Report toReport;
        try {
            toReport = reportDatasource.loadReport(toReportId);
        } catch (IOException e) {
            throw new RuntimeException("Error during report loading", e);
        }
        if (toReport == null) {
            throw new IllegalArgumentException("Can't find 'to' report by id " + toReportId);
        }

        long timeMillis = ReportUtils.fromTimestampJava(toReport.getReport()).toEpochSecond(ZoneOffset.UTC)
                - ReportUtils.fromTimestampJava(fromReport.getReport()).toEpochSecond(ZoneOffset.UTC);

        return TrackerUtil.duration(timeMillis, TrackerUtil.Second);
    }


    public interface Strategy {

        void initPilotContext(PilotContext pilotContext, ReportPilotPosition pilotPosition);

        void onPilotContextProcessed(PilotContext pilotContext);

    }

    public static class StrategyAdapter implements Strategy {
        @Override
        public void initPilotContext(PilotContext pilotContext, ReportPilotPosition pilotPosition) {
        }

        @Override
        public void onPilotContextProcessed(PilotContext pilotContext) {
        }
    }
}
