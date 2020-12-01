package net.simforge.networkview.core.report.file;

import net.simforge.commons.misc.Str;
import net.simforge.networkview.core.Network;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class ReportFile {
    private static final int INT_NaN = Integer.MIN_VALUE;
    private static final float FLOAT_NaN = Float.NaN;
    private static final double DOUBLE_NaN = Double.NaN;

    private static final String FILE = "File";
    private static final String CLIENTS = "Clients";

    private static final String DOES_NOT_MATTER = "!!@@##DOES_NOT_MATTER##@@!!";
    private static final String END_NOT_FOUND = "END not found";

    private Network network;

    private Map<String, Section> sections = new HashMap<>();
    private List<LogEntry> log = new ArrayList<>();

    private List<ClientInfo> clientInfos;

    private boolean endFound;

    public ReportFile(Network network, String data) {
        this.network = network;

        String[] lines = data.split("\n");

        Section currentSection = null;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.endsWith("\r")) {
                line = line.substring(0, line.length()-1);
                lines[i] = line;
            }

            if (line.startsWith(";")) {
                continue;
            }

            if (line.startsWith("!")) {
                String sectionName = line.substring(1);
                if (sectionName.endsWith(":")) {
                    sectionName = sectionName.substring(0, sectionName.length() - 1);
                }

                currentSection = new Section(sectionName);
                sections.put(sectionName, currentSection);
                continue;
            }

            if (currentSection == null) {
                throw new IllegalStateException("Current section is null, line index is " + i);
            }

            currentSection.addLine(line);
        }

        endFound = true;
        if (lines.length > 0) {
            String last = lines[lines.length-1];
            if (!(last.startsWith(";") && last.contains("END"))) {
                log.add(new LogEntry(FILE, FILE, END_NOT_FOUND, END_NOT_FOUND));
                endFound = false;
            }
        }
    }

    public boolean isEndFound() {
        return endFound;
    }

    public Section getSection(String sectionName) {
        return sections.get(sectionName);
    }

    private List<LogEntry> findLogEntries(String section, String object, String msg, String value) {
        List<LogEntry> result = new ArrayList<>();
        for (LogEntry entry : log) {
            if (!DOES_NOT_MATTER.equals(section)) {
                if (!section.equals(entry.getSection())) {
                    continue;
                }
            }

            if (!DOES_NOT_MATTER.equals(object)) {
                if (!object.equals(entry.getObject())) {
                    continue;
                }
            }

            if (!DOES_NOT_MATTER.equals(msg)) {
                if (!msg.equals(entry.getMsg())) {
                    continue;
                }
            }

            if (!DOES_NOT_MATTER.equals(value)) {
                if (!value.equals(entry.getValue())) {
                    continue;
                }
            }

            result.add(entry);
        }
        return result;
    }

    public List<LogEntry> getLog() {
        return Collections.unmodifiableList(log);
    }

    public class Section {
        private String sectionName;
        private List<String> lines = new ArrayList<>();

        public Section(String sectionName) {
            this.sectionName = sectionName;
        }

        public String getValue(String propertyName) {
            for (String line : lines) {
                if (line.startsWith(propertyName)) {
                    String value = line;
                    value = value.substring(propertyName.length() + " = ".length());
                    value = value.trim();
                    return value;
                }
            }
            return null;
        }

        public List<String> getLines() {
            return Collections.unmodifiableList(lines);
        }

        public void addLine(String line) {
            lines.add(line);
        }
    }

    public static class LogEntry {
        private String section;
        private String object;
        private String msg;
        private String value;

        public LogEntry(String section, String object, String msg, String value) {
            this.section = Str.limit(section, 50);
            this.object = Str.limit(object, 50);
            this.msg = Str.limit(msg, 200);
            this.value = Str.limit(value, 1000);
        }

        public String getSection() {
            return section;
        }

        public String getObject() {
            return object;
        }

        public String getMsg() {
            return msg;
        }

        public String getValue() {
            return value;
        }
    }

    public List<ClientInfo> getClientInfos() {
        parseClientInfos();
        return clientInfos;
    }

    public List<ClientInfo> getClientInfos(ReportFile.ClientType clientType) {
        parseClientInfos();

        List<ClientInfo> result = new ArrayList<>();
        for (ClientInfo clientInfo : clientInfos) {
            if (clientInfo.getClienttype() == clientType) {
                result.add(clientInfo);
            }
        }

        return result;
    }

    private void parseClientInfos() {
        if (clientInfos == null) {
            List<ClientInfo> clientInfos = new ArrayList<>();
            Section section = getSection("CLIENTS");
            List<String> strings;
            if (section == null) {
                strings = new ArrayList<>();
                log.add(new LogEntry(CLIENTS, "Section", "Section CLIENTS not found", ""));
            } else {
                strings = section.getLines();
            }
            for (int i = 0; i < strings.size(); i++) {
                String string = strings.get(i);
                try {
                    ClientInfo clientInfo = parseClientInfo(string, i);
                    if (clientInfo != null) {
                        clientInfos.add(clientInfo);
                    }
                } catch(Exception e) {
                    log.add(new LogEntry(CLIENTS, "Line " + i, "Exception occured", e.getMessage() + " (" + e.getClass().getName() + "); line '" + string + "'"));
                }
            }
            this.clientInfos = clientInfos;
        }
    }

    private ClientInfo parseClientInfo(String clientData, int lineIndex) {
        if (network == Network.VATSIM) {
            return parseVatsimClientInfo(clientData, lineIndex);
        } else { // IVAO
            return parseIvaoClientInfo(clientData, lineIndex);
        }
    }

    // See vatsim-data.txt file:
    // ; !CLIENTS section -         callsign:cid:realname:clienttype:frequency:latitude:longitude:altitude:groundspeed:planned_aircraft:planned_tascruise:planned_depairport:planned_altitude:planned_destairport:server:protrevision:rating:transponder:facilitytype:visualrange:planned_revision:planned_flighttype:planned_deptime:planned_actdeptime:planned_hrsenroute:planned_minenroute:planned_hrsfuel:planned_minfuel:planned_altairport:planned_remarks:planned_route:planned_depairport_lat:planned_depairport_lon:planned_destairport_lat:planned_destairport_lon:atis_message:time_last_atis_received:time_logon:heading:QNH_iHg:QNH_Mb:
    private ClientInfo parseVatsimClientInfo(String clientData, int lineIndex) {
        String[] strings = clientData.split(":");

        ClientInfo clientInfo = new ClientInfo();

        clientInfo.callsign                 = parseString(strings[0], strings[0], "Callsign", 10);
        String callsign = clientInfo.callsign;

        clientInfo.cid                      = parseInt(strings[1], callsign, "CID is incorrect");
        clientInfo.clienttype               = ClientType.parseString(strings[3]);
        boolean isPilot = ClientType.PILOT.equals(clientInfo.clienttype);

        clientInfo.latitude                 = parseCoord(strings[5], callsign, "Latitude", -90D, 90D);
        clientInfo.longitude                = parseCoord(strings[6], callsign, "Longitude", -180D, 180D);
        clientInfo.altitude                 = parseAltitude(strings[7], callsign);
        clientInfo.groundspeed              = isPilot ? parseGroundspeed(strings[8], callsign) : 0;
        clientInfo.plannedAircraft          = parseString(strings[9], callsign, "Aircraft", 40);
        clientInfo.plannedDepAirport        = parseString(strings[11], callsign, "Departure ICAO", 4);
        clientInfo.plannedDestAirport       = parseString(strings[13], callsign, "Destination ICAO", 4);
        clientInfo.plannedRemarks           = parseString(strings[29], callsign, "Remarks", 300);
        clientInfo.heading                  = isPilot ? parseHeading(strings[38], callsign) : -1;
        clientInfo.qnhMb = isPilot ? (
                (strings[40] != null && strings[40].length() > 0) ?
                        parseInt(strings[40], callsign, "QNH Mb is incorrect: " + strings[40]) :
                        0) : 0;
        if (clientInfo.qnhMb < 0 || clientInfo.qnhMb > 2000) {
            log.add(new LogEntry(CLIENTS, callsign, "QNH MB is out of range", strings[40]));
            clientInfo.qnhMb = 0;
        }

        if(Double.isNaN(clientInfo.latitude) || Double.isNaN(clientInfo.longitude))
            return null;
        else
            return clientInfo;
    }

    // See http://doc.sd.ivao.aero/doku.php?id=da:whazzup:desc
    private ClientInfo parseIvaoClientInfo(String clientData, int lineIndex) {
        String[] strings = clientData.split(":");

        ClientInfo clientInfo = new ClientInfo();

        clientInfo.callsign                 = parseString(strings[0], strings[0], "Callsign", 10);
        String callsign = clientInfo.callsign;

        clientInfo.cid                      = parseInt(strings[1], callsign, "CID is incorrect");
        clientInfo.clienttype               = ClientType.parseString(strings[3]);
        boolean isPilot = ClientType.PILOT.equals(clientInfo.clienttype);

        clientInfo.latitude                 = parseCoord(strings[5], callsign, "Latitude", -90D, 90D);
        clientInfo.longitude                = parseCoord(strings[6], callsign, "Longitude", -180D, 180D);
        clientInfo.altitude                 = parseAltitude(strings[7], callsign);
        clientInfo.groundspeed              = isPilot ? parseGroundspeed(strings[8], callsign) : 0;
        clientInfo.plannedAircraft          = parseString(strings[9], callsign, "Aircraft", 40);
        clientInfo.plannedDepAirport        = parseString(strings[11], callsign, "Departure ICAO", 4);
        clientInfo.plannedDestAirport       = parseString(strings[13], callsign, "Destination ICAO", 4);
        clientInfo.plannedRemarks           = parseString(strings[29], callsign, "Remarks", 300);
        clientInfo.heading                  = isPilot ? parseHeading(strings[45], callsign) : -1;
        clientInfo.onGround                 = isPilot ? parseInt(strings[46], callsign, "OnGround is incorrect", -1) == 1 : null;

        if(Double.isNaN(clientInfo.latitude) || Double.isNaN(clientInfo.longitude))
            return null;
        else
            return clientInfo;
    }

    private String parseString(String value, String callsign, String desc, int maxLen) {
        if (value == null) {
            return null;
        }

        value = value.trim();
        if (value.length() == 0) {
            return null;
        }

        if (value.length() > maxLen) {
            log.add(new LogEntry(CLIENTS, callsign, desc + " is too long", value));
            return value.substring(0, maxLen);
        }
        return value;
    }

    private int parseAltitude(String value, String callsign) {
        int altitude = parseInt(value, callsign, "Could not parse altitude value");
        if (altitude == INT_NaN) {
            return 0; // could not parse
        }
        if (altitude < -10000) {
            log.add(new LogEntry(CLIENTS, callsign, "Altitude is too small to store in DB. Restricted to -10000.", value));
            return 0;
        }
        if (altitude > 1000000) {
            log.add(new LogEntry(CLIENTS, callsign, "Altitude is too great to store in DB. Restricted to 1000000.", value));
            return 0;
        }
        return altitude;
    }

    private int parseGroundspeed(String value, String callsign) {
        int groundspeed = parseInt(value, callsign, "Could not parse groundspeed value");
        if (groundspeed == INT_NaN) {
            return 0; // could not parse
        }
        if (groundspeed < 0) {
            log.add(new LogEntry(CLIENTS, callsign, "Groundspeed is negative. Restricted to 0.", value));
            return 0;
        }
        if (groundspeed > 32767) {
            log.add(new LogEntry(CLIENTS, callsign, "Groundspeed is too great to store in DB. Restricted to 32767.", value));
            return 32767;
        }
        return groundspeed;
    }

    private int parseHeading(String value, String callsign) {
        int heading = parseInt(value, callsign, "Could not parse heading value");
        if (heading == INT_NaN) {
            return 0; // could not parse
        }
        if (heading < 0) {
            log.add(new LogEntry(CLIENTS, callsign, "Heading is negative, reset to 0", value));
            return 0;
        }
        if (heading > 360) {
            log.add(new LogEntry(CLIENTS, callsign, "Heading is too great, reset to 0", value));
            return 0;
        }
        return heading;
    }

    private double parseCoord(String value, String callsign, String coordName, double min, double max) {
        double coord = parseDouble(value, callsign, coordName + " is incorrect");

        BigDecimal bigDecimalCoord = BigDecimal.valueOf(coord);
        if (bigDecimalCoord.scale() > 6) {
            log.add(new LogEntry("Clients", callsign, coordName + " has too high scale, limiting to scale 6", value));
            coord = bigDecimalCoord.setScale(6, RoundingMode.HALF_UP).doubleValue();
        }

        if(Double.isNaN(coord))
            return coord;
        if(coord > max) {
            log.add(new LogEntry("Clients", callsign, coordName + " is greater than " + max, value));
            return max;
        }
        if(coord < min) {
            log.add(new LogEntry("Clients", callsign, coordName + " is lower than " + min, value));
            return min;
        }
        return coord;
    }

    private int parseInt(String value, String callsign, String msg) {
        return parseInt(value, callsign, msg, INT_NaN);
    }

    private int parseInt(String value, String callsign, String msg, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch(NumberFormatException e) {
            log.add(new LogEntry(CLIENTS, callsign, msg, value));
            return defaultValue;
        }
    }

    private float parseFloat(String value, String callsign, String msg) {
        try {
            return Float.parseFloat(value);
        } catch(NumberFormatException e) {
            log.add(new LogEntry(CLIENTS, callsign, msg, value));
            return FLOAT_NaN;
        }
    }

    private double parseDouble(String value, String callsign, String msg) {
        try {
            return Double.parseDouble(value);
        } catch(NumberFormatException e) {
            log.add(new LogEntry(CLIENTS, callsign, msg, value));
            return DOUBLE_NaN;
        }
    }

    public class ClientInfo {
        private String callsign;
        private int cid;
        private ClientType clienttype;
        private double latitude;
        private double longitude;
        private int altitude;
        private int groundspeed;
        private String plannedAircraft;
        private String plannedDepAirport;
        private String plannedDestAirport;
        private String plannedRemarks;
        private int heading;
        private Boolean onGround;
        private Integer qnhMb;

        public int getCid() {
            return cid;
        }

        public String getCallsign() {
            return callsign;
        }

        public ClientType getClienttype() {
            return clienttype;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public int getAltitude() {
            return altitude;
        }

        public int getGroundspeed() {
            return groundspeed;
        }

        public int getHeading() {
            return heading;
        }

        public String getPlannedAircraft() {
            return plannedAircraft;
        }

        public String getPlannedDepAirport() {
            return plannedDepAirport;
        }

        public String getPlannedDestAirport() {
            return plannedDestAirport;
        }

        public String getPlannedRemarks() {
            return plannedRemarks;
        }

        public Boolean isOnGround() {
            return onGround;
        }

        public Integer getQnhMb() {
            return qnhMb;
        }
    }

    public enum ClientType {
        ATC,
        PILOT;

        public static ClientType parseString(String str) {
            if ("PILOT".equals(str))
                return PILOT;
            if ("ATC".equals(str))
                return ATC;
            throw new IllegalArgumentException();
        }
    }
}
