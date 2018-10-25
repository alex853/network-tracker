package web;

import entities.Movement;

import java.util.Comparator;

import org.joda.time.DateTime;

public class AirportTimelineMovementComparator implements Comparator<Movement> {
    private String icao;

    public AirportTimelineMovementComparator(String icao) {
        this.icao = icao;
    }

    public int compare(Movement m1, Movement m2) {
        DateTime dt1 = get(m1);
        DateTime dt2 = get(m2);

        if (dt1 == null || dt2 == null) {
            if (dt1 != null) {
                return -1;
            } else {
                return 1;
            }
        }

        if (dt1.equals(dt2)) {
            return WebCache.getPilot(m1.getPilotId()).getPilotNumber()
                    - WebCache.getPilot(m2.getPilotId()).getPilotNumber();
        } else {
            return dt1.compareTo(dt2);
        }
    }

    private DateTime get(Movement m) {
        if (icao.equals(m.getDepIcao())) {
            return WebCache.getReport(m.getDepReportId()).getReportDt();
        } else if (icao.equals(m.getArrIcao())) {
            return WebCache.getReport(m.getArrReportId()).getReportDt();
        } else if (icao.equals(m.getPlannedArrIcao())) {
            return m.getExtra().getEstimatedArrTime();
        }
        return null;
    }
}
