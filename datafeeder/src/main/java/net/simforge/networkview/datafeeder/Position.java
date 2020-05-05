package net.simforge.networkview.datafeeder;

import net.simforge.commons.misc.Geo;
import net.simforge.commons.misc.Str;
import net.simforge.networkview.core.report.ReportUtils;
import net.simforge.networkview.core.report.persistence.Report;
import net.simforge.networkview.core.report.persistence.ReportPilotPosition;
import net.simforge.networkview.world.airports.Airport;
import net.simforge.networkview.world.airports.Airports;
import net.simforge.networkview.world.atmosphere.ActualAltitude;
import net.simforge.networkview.world.atmosphere.AltimeterMode;
import net.simforge.networkview.world.atmosphere.AltimeterRules;

import java.time.LocalDateTime;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Position {

    private long reportId;
    private LocalDateTime dt;

    private Geo.Coords coords;
    private int actualAltitude;
    private String actualFL;
    private boolean onGround;
    private boolean inAirport;
    private String airportIcao;

    private String fpAircraftType;
    private String fpOrigin;
    private String fpDestination;

    private Position() {
    }

    public static Position create(ReportPilotPosition reportPilotPosition) {
        Position result = new Position();

        result.reportId = reportPilotPosition.getReport().getId();
        result.dt = ReportUtils.fromTimestampJava(reportPilotPosition.getReport().getReport());
        result.coords = new Geo.Coords(reportPilotPosition.getLatitude(), reportPilotPosition.getLongitude());

        Airport nearestAirport = Airports.get().findNearest(result.coords);

        if (reportPilotPosition.getQnhMb() != null) { // VATSIM
            AltimeterRules altimeterRules = AltimeterRules.get(result.coords, reportPilotPosition.getQnhMb());

            if (altimeterRules.isValid() && nearestAirport != null) {
                result.actualAltitude = altimeterRules.getActualAltitude(reportPilotPosition.getAltitude());
                result.actualFL = altimeterRules.formatAltitude(result.actualAltitude);
                result.onGround = result.actualAltitude < nearestAirport.getElevation() + 200;
            } else {
                result.actualAltitude = ActualAltitude.get(reportPilotPosition.getAltitude(), reportPilotPosition.getQnhMb()).getActualAltitude();
                result.actualFL = ActualAltitude.formatAltitude(result.actualAltitude, AltimeterMode.STD);
                result.onGround = false;
            }
        } else { // IVAO
            result.onGround = reportPilotPosition.getOnGround();
        }

        result.inAirport = result.onGround
                && nearestAirport != null
                && nearestAirport.isWithinBoundary(result.coords);
        result.airportIcao = result.inAirport ? nearestAirport.getIcao() : null;

        result.fpAircraftType = reportPilotPosition.getFpAircraft();
        result.fpOrigin = reportPilotPosition.getFpOrigin();
        result.fpDestination = reportPilotPosition.getFpDestination();

        return result;
    }

    public static Position createOfflinePosition() {
        Position result = new Position();

        result.reportId = 0;
        result.dt = LocalDateTime.of(2000, 1, 1, 0, 0);

        return result;
    }

    public static Position createOfflinePosition(Report report) {
        Position result = new Position();

        result.reportId = report.getId();
        result.dt = ReportUtils.fromTimestampJava(report.getReport());

        return result;
    }

    public long getReportId() {
        return reportId;
    }

    public LocalDateTime getDt() {
        return dt;
    }

    public Geo.Coords getCoords() {
        checkPositionKnown();
        return coords;
    }

    public int getActualAltitude() {
        checkPositionKnown();
        return actualAltitude;
    }

    public String getActualFL() {
        checkPositionKnown();
        return actualFL;
    }

    public boolean isOnGround() {
        checkPositionKnown();
        return onGround;
    }

    public boolean isInAirport() {
        checkPositionKnown();
        return inAirport;
    }

    public String getAirportIcao() {
        checkPositionKnown();
        return airportIcao;
    }

    private void checkPositionKnown() {
        if (!isPositionKnown()) {
            throw new IllegalStateException("Position is unknown");
        }
    }

    public boolean isPositionKnown() {
        return coords != null;
    }

    public boolean hasFlightplan() {
        return !Str.isEmpty(fpAircraftType) || !Str.isEmpty(fpOrigin) || !Str.isEmpty(fpDestination);
    }

    public String getFpAircraftType() {
        return fpAircraftType;
    }

    public String getFpOrigin() {
        return fpOrigin;
    }

    public String getFpDestination() {
        return fpDestination;
    }

    @Override
    public String toString() {
        return "{" + getStatus() + ", rId=" + reportId + "}";
    }

    public String getStatus() {
        String status;

        if (isPositionKnown()) {
            if (inAirport) {
                status = airportIcao;
            } else if (onGround) {
                status = "On Ground";
            } else {
                status = "Flying";
            }
        } else {
            status = "Unknown";
        }
        return status;
    }
}
