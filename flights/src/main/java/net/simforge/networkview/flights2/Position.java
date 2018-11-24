package net.simforge.networkview.flights2;

import net.simforge.commons.misc.Geo;
import net.simforge.commons.misc.Str;
import net.simforge.networkview.datafeeder.ParsingLogics;
import net.simforge.networkview.datafeeder.ReportInfo;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import net.simforge.networkview.world.airports.Airport;
import net.simforge.networkview.world.airports.Airports;
import net.simforge.networkview.world.atmosphere.ActualAltitude;
import net.simforge.networkview.world.atmosphere.AltimeterMode;
import net.simforge.networkview.world.atmosphere.AltimeterRules;

import java.time.LocalDateTime;

public class Position {

    private long reportId;
    private String report;
    private ReportInfo reportInfo;

    private Geo.Coords coords;
    private int actualAltitude;
    private String actualFL;
    private boolean onGround;
    private boolean inAirport;
    private String airportIcao;

    private String callsign;
    private String regNo;
    private String fpAircraftType;
    private String fpDeparture;
    private String fpDestination;

    private Position() {
    }

    public static Position create(ReportPilotPosition reportPilotPosition) {
        Position result = new Position();

        result.reportId = reportPilotPosition.getReport().getId();
        result.report = reportPilotPosition.getReport().getReport();
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

        result.callsign = limit10(reportPilotPosition.getCallsign());
        result.regNo = limit10(reportPilotPosition.getParsedRegNo());
        result.fpAircraftType = limit10(ParsingLogics.parseAircraftType(reportPilotPosition.getFpAircraft()));
        result.fpDeparture = limit10(reportPilotPosition.getFpOrigin());
        result.fpDestination = limit10(reportPilotPosition.getFpDestination());

        return result;
    }

    private static String limit10(String s) {
        if (s == null) {
            return null;
        }
        if (s.length() <= 10) {
            return s;
        }
        return s.substring(0, 10);
    }

/*    public static Position createOfflinePosition() {
        Position result = new Position();

        result.reportId = 0;
        result.dt = LocalDateTime.of(2000, 1, 1, 0, 0);

        return result;
    }*/

    public static Position createOfflinePosition(Report report) {
        Position result = new Position();

        result.reportId = report.getId();
        result.report = report.getReport();

        return result;
    }

    public ReportInfo getReportInfo() {
        if (reportInfo == null) {
            reportInfo = new ReportInfo() {
                @Override
                public Long getId() {
                    return reportId;
                }

                @Override
                public String getReport() {
                    return report;
                }
            };
        }
        return reportInfo;
    }

    @Deprecated
    public long getReportId() {
        return reportId;
    }

    @Deprecated
    public String getReport() {
        return report;
    }

    @Deprecated
    public LocalDateTime getDt() {
        return getReportInfo().getDt();
    }

    public Geo.Coords getCoords() {
        checkPositionKnown();
        return coords;
    }

    public int getActualAltitude() {
        checkPropertyUsage();
        checkPositionKnown();
        return actualAltitude;
    }

    public String getActualFL() {
        checkPropertyUsage();
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

    private void checkPropertyUsage() {
        if (Math.random() >= 0) {
            throw new UnsupportedOperationException(); // todo check every method you use!
        }
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
        return !Str.isEmpty(fpAircraftType) || !Str.isEmpty(fpDeparture) || !Str.isEmpty(fpDestination);
    }

    public String getCallsign() {
        return callsign;
    }

    public String getRegNo() {
        return regNo;
    }

    public String getFpAircraftType() {
        return fpAircraftType;
    }

    public String getFpDeparture() {
        return fpDeparture;
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
