package net.simforge.networkview.flights2;

import net.simforge.commons.hibernate.SessionFactoryBuilder;
import net.simforge.commons.io.Marker;
import net.simforge.commons.legacy.BM;
import net.simforge.commons.runtime.BaseTask;
import net.simforge.commons.runtime.RunningMarker;
import net.simforge.networkview.Network;
import net.simforge.networkview.datafeeder.SessionManager;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.flights.datasource.ReportDatasource;
import net.simforge.networkview.flights2.persistence.DBFlight;
import net.simforge.networkview.flights2.persistence.DBPersistenceLayer;
import net.simforge.networkview.flights2.persistence.DBPilotStatus;
import net.simforge.networkview.flights2.persistence.DBReportDatasource;
import org.hibernate.SessionFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class RecognizeFlights extends BaseTask {

    private static final String ARG_NETWORK = "network";
    private static final String ARG_INTERVAL = "interval";

    private Network network;
    private int interval = 55;

    private Marker marker;

    private ReportDatasource reportDatasource;
    private PersistenceLayer persistenceLayer;
    private MainContext mainContext = null;

    public RecognizeFlights(Properties properties) {
        super("ProcessFlights-" + properties.getProperty(ARG_NETWORK));
        network = Network.valueOf(properties.getProperty(ARG_NETWORK));
    }

    @Override
    protected void startup() {
        BM.start("RecognizeFlights.startup");
        try {

            super.startup();

//            BM.setLoggingPeriod(TimeUnit.HOURS.toMillis(1));
//            BM.setLoggingPeriod(TimeUnit.MINUTES.toMillis(10));
            BM.setLoggingPeriod(TimeUnit.MINUTES.toMillis(1));

            RunningMarker.lock(getTaskName());

            logger.info("Network        : " + network);
            logger.info("Interval       : " + interval + " secs");

            marker = new Marker(getTaskName());

            SessionManager datafeederSessionManager = new SessionManager();
            reportDatasource = new DBReportDatasource(network, datafeederSessionManager);

            SessionFactory flightsSessionFactory = SessionFactoryBuilder
                    .forDatabase("flights." + network.name()) // todo move it to separate class
                    .entities(new Class[]{DBPilotStatus.class, DBFlight.class})
                    .build();
            persistenceLayer = new DBPersistenceLayer(flightsSessionFactory, reportDatasource);

            mainContext = new MainContext(reportDatasource, persistenceLayer);

            String lastReportTimestamp = marker.getString();
            if (lastReportTimestamp != null) {
                mainContext.setLastReport(reportDatasource.loadReport(lastReportTimestamp));
            }

            mainContext.loadActivePilotContexts();

            setBaseSleepTime(interval * 1000L);

        } catch (IOException e) {
            logger.error("Error on startup", e);
            throw new RuntimeException("Error on startup", e);
        } finally {
            BM.stop();
        }
    }

    @Override
    protected void process() {
        BM.start("RecognizeFlights.process");
        try {

            mainContext.processReports(1);

            Report lastReport = mainContext.getLastReport();
            marker.setString(lastReport.getReport());

            if (reportDatasource.loadNextReport(lastReport.getReport()) != null) {
                setNextSleepTime(100);
            }

        } catch (IOException e) {
            logger.error("I/O error happened", e);
            throw new RuntimeException("I/O error happened", e);
        } finally {
            BM.stop();
        }
    }
}
