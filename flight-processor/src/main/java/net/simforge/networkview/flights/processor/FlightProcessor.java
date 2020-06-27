package net.simforge.networkview.flights.processor;

import net.simforge.networkview.core.report.ReportInfo;
import net.simforge.networkview.core.report.ReportInfoDto;
import net.simforge.networkview.core.report.ReportUtils;
import net.simforge.networkview.core.report.persistence.Report;
import net.simforge.networkview.core.report.persistence.ReportOpsService;
import net.simforge.networkview.core.report.persistence.ReportPilotPosition;
import net.simforge.networkview.flights.processor.dto.FlightDto;
import net.simforge.networkview.flights.processor.dto.PilotContextDto;
import net.simforge.networkview.flights.method.eventbased.Flight;
import net.simforge.networkview.flights.method.eventbased.PilotContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class FlightProcessor {
    private static final Logger logger = LoggerFactory.getLogger(FlightProcessor.class);

    @Autowired
//    @Qualifier("class implementation")
    private ReportOpsService reportOpsService;
    @Autowired
//    @Qualifier("class implementation")
    private FlightPersistenceService flightPersistenceService;

    // todo context data
    private ReportInfo previousProcessedReport = null;
    private Map<Integer, PilotContext> pilotContexts = new HashMap<>();
//    private Network network = Network.VATSIM;

    @Scheduled(fixedDelay = 10)
    public void scheduled() {
        if (previousProcessedReport == null) {
            previousProcessedReport = flightPersistenceService.loadLastProcessedReport();
        }

        Report currentReport;
        if (previousProcessedReport != null) {
            currentReport = reportOpsService.loadNextReport(previousProcessedReport.getReport());
        } else {
            currentReport = reportOpsService.loadFirstReport();
        }

        if (currentReport == null) {
            return;
        }

        if (!Boolean.TRUE.equals(currentReport.getParsed())) {
            return;
        }

        logger.info("{} - Processing...", ReportUtils.log(currentReport));

        List<ReportPilotPosition> currentPositions = reportOpsService.loadPilotPositions(currentReport);
        Map<Integer, ReportPilotPosition> currentPositionsMap = currentPositions.parallelStream().collect(Collectors.toMap(ReportPilotPosition::getPilotNumber, Function.identity()));

        Map<Integer, PilotContext> nextPilotContexts = new HashMap<>();

        long lastPrintTs = System.currentTimeMillis();
        int counter = 0;

        // process online pilots
        for (PilotContext pilotContext : pilotContexts.values()) {
            int pilotNumber = pilotContext.getPilotNumber();
            ReportPilotPosition currentPosition = currentPositionsMap.remove(pilotNumber);
            PilotContext nextPilotContext = processPilotPosition(pilotNumber, currentReport, currentPosition);
            if (nextPilotContext != null) {
                nextPilotContexts.put(pilotNumber, nextPilotContext);
            }

            counter++;
            long now = System.currentTimeMillis();
            if (now - lastPrintTs >= 10000) {
                logger.info("{} -     Contexts : {} of {} done", ReportUtils.log(currentReport), counter, pilotContexts.size());
                lastPrintTs = now;
            }
        }

        counter = 0;

        // all remaining pilots - they have been just appeared online (or it is first run of processing)
        for (ReportPilotPosition currentPosition : currentPositionsMap.values()) {
            int pilotNumber = currentPosition.getPilotNumber();
            PilotContext nextPilotContext = processPilotPosition(pilotNumber, currentReport, currentPosition);
            if (nextPilotContext != null) {
                nextPilotContexts.put(pilotNumber, nextPilotContext);
            }

            counter++;
            long now = System.currentTimeMillis();
            if (now - lastPrintTs >= 10000) {
                logger.info("{} -     Positions: {} of {} done", ReportUtils.log(currentReport), counter, currentPositions.size());
                lastPrintTs = now;
            }
        }

        // removing inactive contexts (which were offline for some time)
        Set<Integer> inactivePilotNumbers = nextPilotContexts.values().parallelStream().filter(pc -> !pc.isActive()).map(PilotContext::getPilotNumber).collect(Collectors.toSet());
        nextPilotContexts.keySet().removeAll(inactivePilotNumbers);

        // clearing recent flights to prevent useless memory usage
        nextPilotContexts.values().parallelStream().forEach(PilotContext::clearRecentFlights);

        flightPersistenceService.saveLastProcessedReport(new ReportInfoDto(currentReport));
        previousProcessedReport = currentReport;
        pilotContexts = nextPilotContexts;

        logger.info("{} - Processing completed | Contexts {}", ReportUtils.log(currentReport), pilotContexts.size());
    }

    private PilotContext processPilotPosition(int pilotNumber, Report currentReport, ReportPilotPosition currentPosition) {
        PilotContext pilotContext = pilotContexts.get(pilotNumber);

        if (pilotContext == null) {
            PilotContextDto pilotContextInfo = flightPersistenceService.loadPilotContextInfo(pilotNumber);

            if (pilotContextInfo != null) {
                ReportInfo lastProcessedReport = pilotContextInfo.getLastProcessedReport();
                if (lastProcessedReport == null) {
                    pilotContext = new PilotContext(pilotNumber);
                    pilotContext = pilotContext.processPosition(currentReport, currentPosition);
                    save(pilotContext);
                    return pilotContext;
                } else if (ReportUtils.isTimestampGreater(currentReport.getReport(), lastProcessedReport.getReport())) {
                    FlightDto currentFlight = pilotContextInfo.getCurrentFlight();
                    ReportInfo processSinceReport;
                    if (currentFlight == null) {
                        processSinceReport = lastProcessedReport;
                    } else {
                        processSinceReport = currentFlight.getFirstSeen().getReportInfo();
                    }
                    pilotContext = processRange(pilotNumber, processSinceReport, currentReport);
                    save(pilotContext);
                    return pilotContext;
                } else { // lastProcessedReport equals report || lastProcessedReport is after report
                    // nothing to process here
                    return null;
                }
            } else {
                pilotContext = processRange(pilotNumber,null, currentReport);
                save(pilotContext);
                return pilotContext;
            }
        } else {
            pilotContext = pilotContext.processPosition(currentReport, currentPosition);
            save(pilotContext);
            return pilotContext;
        }
    }

    private PilotContext processRange(int pilotNumber, ReportInfo sinceReport, ReportInfo tillReport) {
        List<ReportPilotPosition> positions;
        List<Report> reports;
        if (sinceReport != null) {
            positions = reportOpsService.loadPilotPositionsSinceTill(pilotNumber, sinceReport, tillReport);
            reports = reportOpsService.loadReports(sinceReport, tillReport);
        } else {
            positions = reportOpsService.loadPilotPositionsTill(pilotNumber, tillReport.getReport());
            sinceReport = positions.get(0).getReport(); // todo check null
            reports = reportOpsService.loadReports(sinceReport, tillReport);
        }

        Map<Long, ReportPilotPosition> reportToPosition = positions.parallelStream().collect(Collectors.toMap(p -> p.getReport().getId(), Function.identity()));

        PilotContext pilotContext = new PilotContext(pilotNumber);
        for (Report report : reports) {
            ReportPilotPosition position = reportToPosition.get(report.getId());
            pilotContext = pilotContext.processPosition(report, position);
        }

        return pilotContext;
    }

    private void save(PilotContext pilotContext) {
        ReportInfo firstProcessedReport = null;

        List<Flight> recentFlights = pilotContext.getRecentFlights();
        for (Flight flight : recentFlights) {
            upsertFlight(pilotContext, flight);
            firstProcessedReport = chooseEarliestReport(flight.getFirstSeen().getReportInfo(), firstProcessedReport);
        }

        if (pilotContext.getCurrFlight() != null) {
            upsertFlight(pilotContext, pilotContext.getCurrFlight());
            firstProcessedReport = chooseEarliestReport(pilotContext.getCurrFlight().getFirstSeen().getReportInfo(), firstProcessedReport);
        }

        upsertPilotContextInfo(pilotContext, firstProcessedReport);
    }

    private ReportInfo chooseEarliestReport(ReportInfo report1, ReportInfo report2) {
        if (report1 == null && report2 == null) {
            return null;
        } else if (report1 != null && report2 == null) {
            return report1;
        } else if (report1 == null && report2 != null) {
            return report2;
        } else {
            return ReportUtils.isTimestampGreater(report1.getReport(), report2.getReport()) ? report2 : report1;
        }
    }

    private ReportInfo chooseLatestReport(ReportInfo report1, ReportInfo report2) {
        if (report1 == null && report2 == null) {
            return null;
        } else if (report1 != null && report2 == null) {
            return report1;
        } else if (report1 == null && report2 != null) {
            return report2;
        } else {
            return ReportUtils.isTimestampGreater(report1.getReport(), report2.getReport()) ? report1 : report2;
        }
    }

    private void upsertFlight(PilotContext pilotContext, Flight flight) {
        flightPersistenceService.upsertFlight(new FlightDto(flight));
    }

    private void upsertPilotContextInfo(PilotContext pilotContext, ReportInfo firstProcessedReport) {
        int pilotNumber = pilotContext.getPilotNumber();

        PilotContextDto pilotContextInfo = flightPersistenceService.loadPilotContextInfo(pilotNumber);
        if (pilotContextInfo == null) {
            pilotContextInfo = new PilotContextDto(pilotNumber);
        }

        pilotContextInfo.setFirstProcessedReport(new ReportInfoDto(chooseEarliestReport(pilotContextInfo.getFirstProcessedReport(), firstProcessedReport)));
        pilotContextInfo.setLastProcessedReport(new ReportInfoDto(chooseLatestReport(pilotContextInfo.getLastProcessedReport(), pilotContext.getLastProcessedPosition().getReportInfo())));
        if (pilotContext.getCurrFlight() != null) {
            pilotContextInfo.setCurrentFlight(new FlightDto(pilotContext.getCurrFlight()));
        } else {
            pilotContextInfo.setCurrentFlight(null);
        }

        flightPersistenceService.upsertPilotContextInfo(pilotContextInfo);
    }

}
