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
import net.simforge.networkview.flights.model.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ProcessFlights extends BaseTask {

    private static final String ARG_NETWORK = "network";
    private static final String ARG_INTERVAL = "interval";

    private Network network;
    private int interval = 55;

    private ReportDatasource reportDatasource = new CsvDatasource(Csv.empty()); // todo
    private PersistenceLayer persistenceLayer = null; // todo
    private MainContext mainContext = null;

    public ProcessFlights(Properties properties) {
        super("ProcessFlights-" + properties.getProperty(ARG_NETWORK));
        // todo properties reading
        network = Network.VATSIM;
    }

    @Override
    protected void startup() {
        super.startup();

//        BM.setLoggingPeriod(TimeUnit.HOURS.toMillis(1));
        BM.setLoggingPeriod(TimeUnit.MINUTES.toMillis(10));

        RunningMarker.lock(getTaskName());

        logger.info("Network        : " + network);
        logger.info("Interval       : " + interval + " secs");

        setBaseSleepTime(interval * 1000L);

        mainContext = new MainContext(null, null); // todo
        mainContext.loadActivePilotContexts();
    }

    @Override
    protected void process() {
        BM.start("ProcessFlights.process");
        try {
            mainContext.processReports(1);
        } catch (IOException e) {
            logger.error("I/O error happened", e);
            throw new RuntimeException("I/O error happened", e);
        } finally {
            BM.stop();
        }
    }
}
