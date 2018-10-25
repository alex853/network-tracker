package flights;

import flights.datasource.MultiSessionDBDatasource;
import flights.model.Flight;
import flights.model.MainContext;
import flights.model.PilotContext;
import net.simforge.commons.legacy.BM;
import net.simforge.commons.legacy.logging.LogHelper;
import net.simforge.commons.io.Marker;
import net.simforge.commons.runtime.RunningMarker;
import net.simforge.commons.runtime.ThreadMonitor;
import net.simforge.tracker.Network;
import net.simforge.tracker.SessionManager;
import net.simforge.tracker.datafeeder.persistence.ReportPilotPosition;
import org.joda.time.DateTimeConstants;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Recognise {
    private static final String TASKNAME = "Recognise";

    private static final String ARG_NETWORK = "network:";
    private static final String ARG_SINGLE = "single:";

    private Logger log;

    private Network network;
    private boolean singleRun = false;
    @SuppressWarnings("FieldCanBeLocal")
    private Marker marker;
    private SessionManager sessionManager;

    public static void main(String args[]) throws IOException {
        new Recognise()._main(args);
    }

    private void _main(String args[]) throws IOException {
        String networkStr = null;
        for (String arg : args) {
            if (arg.startsWith(ARG_NETWORK)) {
                networkStr = arg.substring(ARG_NETWORK.length());
            } else if (arg.startsWith(ARG_SINGLE)) {
                singleRun = Boolean.parseBoolean(arg.substring(ARG_SINGLE.length()));
            }
        }


        if ("vatsim".equalsIgnoreCase(networkStr)) {
            network = Network.VATSIM;
        } else if ("ivao".equalsIgnoreCase(networkStr)) {
            network = Network.IVAO;
        } else {
            log = LogHelper.getLogger(TASKNAME);
            log("Specify network:vatsim or network:ivao parameter");
            return;
        }


        String taskname = TASKNAME + "-" + network;

        log = LogHelper.getLogger(taskname);

        RunningMarker.lock(taskname);
        BM.init(taskname);

        Thread.currentThread().setName(taskname);
        ThreadMonitor.checkin();


        log("Network     : " + network);
        log("Single run  : " + singleRun);


        marker = new Marker(taskname);
        sessionManager = new SessionManager();


        MainContext mainContext = new MainContext();

        // todo last report ?

        mainContext.setReportDatasource(new MultiSessionDBDatasource(sessionManager));

        mainContext.setStrategy(new MainContext.Strategy() {
            @Override
            public void initPilotContext(PilotContext pilotContext, ReportPilotPosition pilotPosition) {
                // todo AK load previous positions and movements for 24-48 hours?
            }

            @Override
            public void onPilotContextProcessed(PilotContext pilotContext) {
                List<Flight> flights = pilotContext.getFlights();
                for (Flight flight : flights) {
                    //Date firstSeenDt = flight.getFirstSeen().getReportDt();
                    flight.getStatus();
                    // todo AK
                }
            }
        });

        try {
            while (!ThreadMonitor.isStopRequested()) {
                //String lastProcessedReport = marker.getString();

                long sleepTime;
                BM.start("Recognise.process");
                try {
                    int processed = mainContext.processReports(1);

                    if (processed == 0) { // no reports found
                        sleepTime = DateTimeConstants.MILLIS_PER_MINUTE;
                    } else {
                        sleepTime = DateTimeConstants.MILLIS_PER_SECOND;
                    }

                    // todo marker.setString(nextReport);
                } catch (Exception e) {
                    logException(e);
                    log("Long sleeping due to exception");
                    sleepTime = 5 * DateTimeConstants.MILLIS_PER_MINUTE; // 5 mins after exception
                } finally {
                    BM.stop();
                }

                if (singleRun) {
                    break;
                } else {
                    ThreadMonitor.sleepBM(sleepTime);
                }

                BM.logPeriodically(true);
            }
        } finally {
            sessionManager.dispose();
        }

        log("Finished");
    }

    private void log(String msg) {
        log.log(Level.INFO, msg);
    }

    private void logException(Exception e) {
        log.log(Level.SEVERE, "Exception caught", e);
    }

}
