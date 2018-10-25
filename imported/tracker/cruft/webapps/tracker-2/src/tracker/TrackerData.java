package tracker;

import javax.servlet.ServletContext;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.sql.SQLException;
import java.io.IOException;

import forge.commons.Settings;
import world.Airports;
import world.AirportPAILoader;
import core.DBOps;
import entities.Report;
import entities.ReportPilotPosition;

public class TrackerData {
    private static final Object monitor = new Object();

    public static Airports airports;

    static {
        try {
            airports = AirportPAILoader.load(Settings.get("dataPath"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static TrackerData getOrLoad(ServletContext servletContext) throws SQLException, IOException {
        Report newestReport = DBOps.findNewestReport();

        synchronized(monitor) {
            TrackerData dataFromServletContext = (TrackerData) servletContext.getAttribute("tracker-2.data");
            TrackerData data;
            if ((dataFromServletContext == null) || (dataFromServletContext.newestReport.getId() != newestReport.getId())) {
                data = init(newestReport);
                servletContext.setAttribute("tracker-2.data", data);
            } else {
                data = dataFromServletContext;
            }

            return data;
        }
    }

    private Report newestReport;
    private List<Report> reports;
    private Map<Integer, PilotData> pilots2;
    private List<Integer> sortedPilots;

    private static TrackerData init(Report newestReport) throws SQLException, IOException {
        long started = System.currentTimeMillis();

        TrackerData data = new TrackerData();

        data.newestReport = newestReport;
        data.reports = DBOps.loadNPreviousReports(newestReport.getId(), 16);

        data.pilots2 = new TreeMap<Integer, PilotData>();

        for (Report eachReport : data.reports) {
            List<ReportPilotPosition> positions = net.simforge.ot.db.DBOps.live().loadPilotPositions(eachReport);

            for (ReportPilotPosition position : positions) {
                int pilotNumber = position.getPilotNumber();
                PilotData pilotData = data.pilots2.get(pilotNumber);
                if (pilotData == null) {
                    pilotData = new PilotData(pilotNumber);
                    data.pilots2.put(pilotNumber, pilotData);
                    pilotData.setReports(data.reports);
                }
                pilotData.setReportPosition(eachReport, position);
            }
        }

        long loaded = System.currentTimeMillis();
        System.out.println("Tracker data loaded in " + (loaded - started) + " ms");

        for (PilotData pilotData : data.pilots2.values()) {
            pilotData.build();
        }
        long built = System.currentTimeMillis();
        System.out.println("Tracker data built in " + (built - loaded) + " ms");

//            data.sortedPilots = sortPilots(data);
//        long sorted = System.currentTimeMillis();
//            System.out.println("Tracker data sorted in " + (sorted - loaded) + " ms");

//        data.kml = makeKml(data);
//        long kmlDone = System.currentTimeMillis();
//        System.out.println("Tracker kml done in " + (kmlDone - sorted) + " ms (size " + (data.kml.length() / 1000) + " kb)");

        return data;
    }

/*        private static List<Integer> sortPilots(final Data data) {
            List<Integer> sortedPilots = new ArrayList(data.pilots2.keySet());
            Collections.sort(sortedPilots, new Comparator<Integer>() {
                public int compare(Integer o1, Integer o2) {
                    PilotData p1 = data.pilots2.get(o1);
                    PilotData p2 = data.pilots2.get(o2);
                    return p1.getCallsign().compareTo(p2.getCallsign());
                }
            });
            return sortedPilots;
        }*/

    public List<Report> getReports() {
        return reports;
    }

    public Map<Integer, PilotData> getPilots() {
        return pilots2;
    }
}
