package net.simforge.fr24.model0;

import forge.commons.Misc;
import forge.commons.TimeMS;
import forge.commons.db.DB;
import forge.commons.io.IOHelper;
import net.simforge.commons.logging.LogHelper;
import net.simforge.commons.persistence.BaseEntity;
import net.simforge.commons.persistence.Column;
import net.simforge.commons.persistence.Persistence;
import net.simforge.commons.persistence.Table;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FlightRadar24 {
    private static final String TASKNAME = "FlightRadar24";
    private static final Logger log = LogHelper.getLogger(TASKNAME);

    public static void main(String[] args) throws SQLException, IOException, JSONException {
//        Runner.start(TASKNAME);
//        BM.init(TASKNAME);

//        Thread.currentThread().setName(TASKNAME);
//        ThreadMonitor.checkin();

        //noinspection InfiniteLoopStatement
        while (true) {

            boolean tryAgain = false;

            try {
                processData();
            } catch(Exception e) {
                log.log(Level.SEVERE, "Unable to process data", e);
                tryAgain = true;
            }

//            BM.logPeriodically(true);
            if (tryAgain) {
                log.info("I'll retry in 30 secs");
                Misc.sleepBM(30 * TimeMS.SECOND);
            } else {
                log.info("Sleeping for 30 mins");
                Misc.sleepBM(30 * TimeMS.MINUTE);
            }

//            ThreadMonitor.alive();

        }
    }

    private static void processData() throws IOException, JSONException, SQLException {

        log.info("Downloading");
        String url = "http://arn.data.fr24.com/zones/full_all.js";
        String content = IOHelper.download(url);
        log.info("Content:[[[" + content + "]]]");

        String pdCallback = "pd_callback";
        int pdCallbackIndex = content.indexOf(pdCallback);
        String data = content.substring(pdCallbackIndex + pdCallback.length());

        data = removeLeading(data, '(');
        data = removeTrailing(data, ';');
        data = removeTrailing(data, ')');
        log.info("Data:[[[" + data + "]]]");

        JSONObject json = new JSONObject(data);
        log.info("JSON OK");

        Iterator keysIterator = json.keys();
        while (keysIterator.hasNext()) {
            String key = (String) keysIterator.next();
            Object eachObj = json.get(key);
            log.info(key + " -> " + eachObj);
            if (!(eachObj instanceof JSONArray)) {
                log.info("Skipped");
                continue;
            }
            JSONArray each = (JSONArray) eachObj;
            Fr24 fr24 = new Fr24();
            fr24.setDt(new DateTime());
            fr24.setFrId(each.getString(0));
            fr24.setLatS(each.getString(1));
            fr24.setLonS(each.getString(2));
            fr24.setHeadingS(each.getString(3));
            fr24.setAltS(each.getString(4));
            fr24.setGsS(each.getString(5));
            fr24.setSquawkS(each.getString(6));
            fr24.setRadar(each.getString(7));
            fr24.setType(each.getString(8));
            fr24.setRegNumber(each.getString(9));
            fr24.setUnknownNumber(each.getString(10));
            fr24.setOriginIata(each.getString(11));
            fr24.setDestinationIata(each.getString(12));
            fr24.setFlightNumber(each.getString(13));
            fr24.setUnknown_1(each.getString(14));
            fr24.setUnknown_2(each.getString(15));
            fr24.setCallsign(each.getString(16));
            fr24.setUnknown_3(each.getString(17));

            Connection connx = null;
            try {
                connx = DB.getConnection();
                Persistence.create(connx, fr24);
                connx.commit();
                log.info("Added");
            } finally {
                if (connx != null) {
                    connx.close();
                }
            }
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

    @Table(name = "fr24")
    public static class Fr24 extends BaseEntity {


        @Column
        private DateTime dt;

        @Column
        private String frId;

        @Column
        private String latS;

        @Column
        private String lonS;

        @Column
        private String headingS;

        @Column
        private String altS;

        @Column
        private String gsS;

        @Column
        private String squawkS;

        @Column
        private String radar;

        @Column
        private String type;

        @Column
        private String regNumber;

        @Column
        private String unknownNumber;

        @Column
        private String originIata;

        @Column
        private String destinationIata;

        @Column
        private String flightNumber;

        @Column
        private String unknown_1;

        @Column
        private String unknown_2;

        @Column
        private String callsign;

        @Column
        private String unknown_3;

        public DateTime getDt() {
            return dt;
        }

        public void setDt(DateTime dt) {
            this.dt = dt;
        }

        public String getFrId() {
            return frId;
        }

        public void setFrId(String frId) {
            this.frId = frId;
        }

        public String getLatS() {
            return latS;
        }

        public void setLatS(String latS) {
            this.latS = latS;
        }

        public String getLonS() {
            return lonS;
        }

        public void setLonS(String lonS) {
            this.lonS = lonS;
        }

        public String getHeadingS() {
            return headingS;
        }

        public void setHeadingS(String headingS) {
            this.headingS = headingS;
        }

        public String getAltS() {
            return altS;
        }

        public void setAltS(String altS) {
            this.altS = altS;
        }

        public String getGsS() {
            return gsS;
        }

        public void setGsS(String gsS) {
            this.gsS = gsS;
        }

        public String getSquawkS() {
            return squawkS;
        }

        public void setSquawkS(String squawkS) {
            this.squawkS = squawkS;
        }

        public String getRadar() {
            return radar;
        }

        public void setRadar(String radar) {
            this.radar = radar;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getRegNumber() {
            return regNumber;
        }

        public void setRegNumber(String regNumber) {
            this.regNumber = regNumber;
        }

        public String getUnknownNumber() {
            return unknownNumber;
        }

        public void setUnknownNumber(String unknownNumber) {
            this.unknownNumber = unknownNumber;
        }

        public String getOriginIata() {
            return originIata;
        }

        public void setOriginIata(String originIata) {
            this.originIata = originIata;
        }

        public String getDestinationIata() {
            return destinationIata;
        }

        public void setDestinationIata(String destinationIata) {
            this.destinationIata = destinationIata;
        }

        public String getFlightNumber() {
            return flightNumber;
        }

        public void setFlightNumber(String flightNumber) {
            this.flightNumber = flightNumber;
        }

        public String getUnknown_1() {
            return unknown_1;
        }

        public void setUnknown_1(String unknown_1) {
            this.unknown_1 = unknown_1;
        }

        public String getUnknown_2() {
            return unknown_2;
        }

        public void setUnknown_2(String unknown_2) {
            this.unknown_2 = unknown_2;
        }

        public String getCallsign() {
            return callsign;
        }

        public void setCallsign(String callsign) {
            this.callsign = callsign;
        }

        public String getUnknown_3() {
            return unknown_3;
        }

        public void setUnknown_3(String unknown_3) {
            this.unknown_3 = unknown_3;
        }
    }
}
