package net.simforge.fr24;

import core.Storage;
import core.UpdateStamp;
import forge.commons.BM;
import forge.commons.io.IOHelper;
import forge.commons.runner.Runner;
import net.simforge.commons.logging.LogHelper;
import net.simforge.commons.misc.Marker;
import net.simforge.commons.misc.Misc;
import net.simforge.commons.system.ThreadMonitor;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Feeder {
    private static final String TASKNAME = "FR24Feeder";

    private static final Logger log = LogHelper.getLogger(TASKNAME);

    private static final String ARG_SINGLE = "single:";
    private static final String ARG_STORAGE = "storage:";
    private static final String ARG_PROCESSING = "processing:";

    private static boolean singleRun = false;
    private static String storageRoot = Storage.DEFAULT_STORAGE_ROOT;
    private static boolean copyToProcessingFolder = true;

    private static Storage storage;

    public static void main(String[] args) {
        Runner.start(TASKNAME);
        BM.init(TASKNAME);

        Thread.currentThread().setName(TASKNAME);
        ThreadMonitor.checkin();

        Marker fr24FeederMarker = new Marker(TASKNAME);

        for (String arg : args) {
            if (arg.startsWith(ARG_SINGLE)) {
                singleRun = Boolean.parseBoolean(arg.substring(ARG_SINGLE.length()));
            } else if (arg.startsWith(ARG_STORAGE)) {
                storageRoot = arg.substring(ARG_STORAGE.length()).trim();
            } else if (arg.startsWith(ARG_PROCESSING)) {
                copyToProcessingFolder = Boolean.parseBoolean(arg.substring(ARG_PROCESSING.length()));
            }
        }

        log.info("Single run: " + singleRun);
        log.info("Storage root: " + storageRoot);
        log.info("Copy to processing folder: " + copyToProcessingFolder);

        storage = Storage.getFR24Storage(storageRoot);

        boolean nextTimePrinted = false;
        //noinspection InfiniteLoopStatement
        while (!ThreadMonitor.isStopRequested()) {

            Date date = fr24FeederMarker.getDate();
            long nextTime = date.getTime() + 30 * DateTimeConstants.MILLIS_PER_MINUTE;
            if (!nextTimePrinted) {
                log.info("Next download will be performed at " + Misc.yMdHms.print(new DateTime(nextTime)));
                nextTimePrinted = true;
            }

            boolean itsTimeToDownload = date == null || (System.currentTimeMillis() >= nextTime);

            if (itsTimeToDownload) {
                try {
                    download();
                    fr24FeederMarker.setDate(new Date());
                    nextTimePrinted = false;
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Error during downloading", e);
                }
            }

            BM.logPeriodically(true);
            ThreadMonitor.sleepBM(DateTimeConstants.MILLIS_PER_MINUTE);

        }

        log.info("Finished");
    }

    private static void download() throws IOException, JSONException, SQLException {

        log.info("Downloading");
        String url = "http://arn.data.fr24.com/zones/full_all.js";
        String content = IOHelper.download(url);
        log.info("Downloaded. Size of content is " + content.length());

        File snapshotsRoot = storage.getSnapshotsRoot();

        DateTime nowUtc = new DateTime(DateTimeZone.UTC);

        File dateFolder = new File(snapshotsRoot, DateTimeFormat.forPattern("yyyy-MM-dd").print(nowUtc));
        //noinspection ResultOfMethodCallIgnored
        dateFolder.mkdirs();

        String filename = UpdateStamp.toUpdate(nowUtc);
        File snapshotFile = new File(dateFolder, filename);
        IOHelper.saveFile(snapshotFile, content);
        log.info("Saved to snapshots");

        if (copyToProcessingFolder) {
            File processingRoot = storage.getProcessingRoot();
            //noinspection ResultOfMethodCallIgnored
            processingRoot.mkdirs();
            IOHelper.saveFile(new File(processingRoot, filename), content);
            log.info("Saved to processing folder");
        }

    }
}
