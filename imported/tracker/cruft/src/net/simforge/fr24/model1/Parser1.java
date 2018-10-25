package net.simforge.fr24.model1;

import core.Storage;
import core.UpdateStamp;
import forge.commons.BM;
import forge.commons.Misc;
import forge.commons.db.DB;
import forge.commons.io.IOHelper;
import forge.commons.runner.Runner;
import net.simforge.commons.logging.LogHelper;
import net.simforge.commons.misc.Str;
import net.simforge.commons.persistence.Persistence;
import net.simforge.commons.system.ThreadMonitor;
import net.simforge.fr24.FR24;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Parser1 {
    private static final String TASKNAME = "FR24Parser1";
    private static final Logger log = LogHelper.getLogger(TASKNAME);

    private static final String ARG_SINGLE = "single:";
    private static final String ARG_STORAGE = "storage:";

    private static boolean singleRun = false;
    private static String storageRoot = Storage.DEFAULT_STORAGE_ROOT;

    private static Storage storage;

    public static void main(String[] args) {
        Runner.start(TASKNAME);
        BM.init(TASKNAME);

        Thread.currentThread().setName(TASKNAME);
        ThreadMonitor.checkin();

        for (String arg : args) {
            if (arg.startsWith(ARG_SINGLE)) {
                singleRun = Boolean.parseBoolean(arg.substring(ARG_SINGLE.length()));
            } else if (arg.startsWith(ARG_STORAGE)) {
                storageRoot = arg.substring(ARG_STORAGE.length()).trim();
            }
        }

        log.info("Single run: " + singleRun);
        log.info("Storage root: " + storageRoot);

        storage = Storage.getFR24Storage(storageRoot);

        List<String> snapshots = new ArrayList<String>();

        //noinspection InfiniteLoopStatement
        while (!ThreadMonitor.isStopRequested()) {

            long sleepTime;

            if (snapshots.isEmpty()) {
                File files[] = storage.getProcessingRoot().listFiles();
                if (files != null) {
                    for (File file : files) {
                        String filename = file.getName();
                        if (file.isFile() && UpdateStamp.isUpdate(filename)) {
                            snapshots.add(filename);
                        }
                    }
                }

                if (snapshots.size() > 1) {
                    log.info("Found " + snapshots.size() + " snapshots for processing");
                }

                Collections.sort(snapshots);
            }


            if (snapshots.isEmpty()) {
                sleepTime = DateTimeConstants.MILLIS_PER_MINUTE;
            } else {
                sleepTime = 100;

                String snapshot = snapshots.remove(0);

                BM.start("process");
                try {
                    process(snapshot);
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Error during processing", e);
                    sleepTime = 10 * DateTimeConstants.MILLIS_PER_MINUTE;
                } finally {
                    BM.stop();
                }

                if (singleRun) {
                    break;
                }
            }

            BM.logPeriodically(true);
            ThreadMonitor.sleepBM(sleepTime);

        }
    }

    private static void process(String snapshot) throws IOException, JSONException, SQLException {

        log.info("Processing snapshot " + snapshot);
        File snapshotFile = new File(storage.getProcessingRoot(), snapshot);
        DateTime dateTime = UpdateStamp.updateToDateJT(snapshot).toDateMidnight().toDateTime();

        String content = IOHelper.loadFile(snapshotFile);

        String pdCallback = "pd_callback";
        int pdCallbackIndex = content.indexOf(pdCallback);
        String data = content.substring(pdCallbackIndex + pdCallback.length());

        data = removeLeading(data, '(');
        data = removeTrailing(data, ';');
        data = removeTrailing(data, ')');
        //log.info("Data:[[[" + data + "]]]");

        JSONObject json;
        BM.start("process#json");
        try {
            json = new JSONObject(data);
        } finally {
            BM.stop();
        }
        log.info("JSON OK");

        Iterator keysIterator = json.keys();
        while (keysIterator.hasNext()) {
            String key = (String) keysIterator.next();
            Object eachObj = json.get(key);
            if (!(eachObj instanceof JSONArray)) {
                log.info("JSON " + key + " SKIPPED");
                continue;
            }
            JSONArray each = (JSONArray) eachObj;
            Flight flight = new Flight();
            flight.setDof(dateTime);
            flight.setFlightNumber(each.getString(13));
            flight.setOriginIata(each.getString(11));
            flight.setDestinationIata(each.getString(12));
            flight.setType(each.getString(8));
            flight.setRegNumber(each.getString(9));
            flight.setCallsign(each.getString(16));

            log.info(flight.toString());

            if (Str.isEmpty(flight.getFlightNumber())
                    || Str.isEmpty(flight.getOriginIata())
                    || Str.isEmpty(flight.getDestinationIata())) {
                log.warning("Skipping due to empty fields");
                continue;
            }

            createIfAbsent(flight);
        }

        snapshotFile.delete();
    }

    private static void createIfAbsent(Flight flight) throws SQLException {
        BM.start("createIfAbsent");
        Connection connx = null;
        try {
            connx = FR24.cp.getConnection();

            List<Flight> existingFlights = loadExistingFlights(connx, flight);

            boolean exists = false;
            for (Flight existingFlight : existingFlights) {
                if (Misc.areEqual(existingFlight.getType(), flight.getType())
                        && Misc.areEqual(existingFlight.getRegNumber(), flight.getRegNumber())
                        && Misc.areEqual(existingFlight.getCallsign(), flight.getCallsign())) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                Persistence.create(connx, flight);
                connx.commit();
                log.info("Added");
            } else {
                log.info("Already exists, skipping");
            }
        } finally {
            if (connx != null) {
                connx.close();
            }
            BM.stop();
        }
    }

    private static List<Flight> loadExistingFlights(Connection connx, Flight flight) throws SQLException {
        BM.start("loadExistingFlights");
        try {
            return Persistence.loadWhere(
                    connx,
                    Flight.class,
                    String.format("dof = '%s' " +
                            "and flight_number = '%s' " +
                            "and origin_iata = '%s' " +
                            "and destination_iata = '%s'",
                            DB.postgresDateFormat.print(flight.getDof()),
                            flight.getFlightNumber(),
                            flight.getOriginIata(),
                            flight.getDestinationIata()));
        } finally {
            BM.stop();
        }
    }

    private static String removeTrailing(String data, char c) {
        if (data.endsWith(String.valueOf(c))) {
            return data.substring(0, data.length() - 1);
        } else {
            return data;
        }
    }

    private static String removeLeading(String data, char c) {
        if (data.startsWith(String.valueOf(c))) {
            return data.substring(1);
        } else {
            return data;
        }
    }


}
