package net.simforge.networkview.flights2;

import net.simforge.commons.io.Csv;
import net.simforge.commons.legacy.BM;
import net.simforge.commons.runtime.BaseTask;
import net.simforge.commons.runtime.RunningMarker;
import net.simforge.networkview.Network;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import net.simforge.networkview.flights.datasource.CsvDatasource;
import net.simforge.networkview.flights.datasource.ReportDatasource;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Deprecated
public class Run3 extends BaseTask {

    private static final String ARG_NETWORK = "network";
    private static final String ARG_INTERVAL = "interval";

    private Network network;
    private int interval = 55;

    private ReportDatasource reportDatasource = new CsvDatasource(Csv.empty()); // todo
    private PersistenceLayer persistenceLayer = null; // todo
    private Set<PilotContext> queue = newEmptyQueue();
    private String lastProcessedReport = null;

    public Run3(Properties properties) {
        super("Run3-" + properties.getProperty(ARG_NETWORK));
        // todo properties reading
        network = Network.VATSIM;
    }

    @Override
    protected void startup() {
        super.startup();

        BM.setLoggingPeriod(TimeUnit.HOURS.toMillis(1));
//        BM.setLoggingPeriod(TimeUnit.MINUTES.toMillis(10));

        RunningMarker.lock(getTaskName());

        logger.info("Network        : " + network);
        logger.info("Interval       : " + interval + " secs");

        setBaseSleepTime(interval * 1000L);

        // todo load active and non-error pilot contexts

        lastProcessedReport = null; // todo max report timestamp?
    }

    @Override
    protected void process() {
        BM.start("Run3.process");
        try {
            Report nextReport = reportDatasource.loadNextReport(lastProcessedReport);
            if (nextReport == null) {
                return;
            }

            populateQueue(nextReport);


        } catch (IOException e) {
            logger.error("I/O error happened", e);
            throw new RuntimeException("I/O error happened", e);
        } finally {
            BM.stop();
        }

    }

    private void populateQueue(Report report) throws IOException {
        List<ReportPilotPosition> reportPilotPositions = reportDatasource.loadPilotPositions(report.getId());
        Set<Integer> pilotNumberInReport = new TreeSet<>();
        for (ReportPilotPosition reportPilotPosition : reportPilotPositions) {
            pilotNumberInReport.add(reportPilotPosition.getPilotNumber());
        }

        for (PilotContext pilotContext : queue) {
            pilotNumberInReport.remove(pilotContext.getPilotNumber());
        }

        for (Integer pilotNumber : pilotNumberInReport) {
            PilotContext pilotContext = persistenceLayer.loadContext(pilotNumber);
            if (pilotContext == null) {
                pilotContext = persistenceLayer.createContext(pilotNumber);
            }

            queue.add(pilotContext);
        }
    }

    private void somewhat() throws IOException {
        // for each report, every few mins


        Iterator<PilotContext> it = queue.iterator();

        Set<PilotContext> nextQueue = new TreeSet<>();

        while (it.hasNext()) {
            PilotContext pilotContext = it.next();

            PilotContext newPilotContext;
            try {
                long reportTimestamp = 0;
                Report report = reportDatasource.loadReport(reportTimestamp);
                ReportPilotPosition reportPilotPosition = reportDatasource.loadPilotPosition(report.getId(), pilotContext.getPilotNumber());
                PilotContext dirtyPilotContext = pilotContext.processPosition(null, reportPilotPosition);
                newPilotContext = persistenceLayer.saveChanges(dirtyPilotContext);
            } catch (Exception e) {
                newPilotContext = persistenceLayer.saveChanges(pilotContext/*.errorOccured()*/);
            }

            if (true /*newPilotContext.isRecent() && !newPilotContext.tooManyErrors()*/) {
                nextQueue.add(newPilotContext);
            }
        }
    }


    private Set<PilotContext> newEmptyQueue() {
        throw new UnsupportedOperationException("Run3.newEmptyQueue");
    }
}
