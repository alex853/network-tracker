package net.simforge.tracker.webapp.dto;

import net.simforge.commons.misc.JavaTime;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import net.simforge.networkview.flights.model.Flight;
import net.simforge.networkview.flights.model.PilotContext;
import net.simforge.tracker.world.Position;

import java.io.IOException;
import java.time.LocalDateTime;

public class PilotStatusDto {

    private long reportId;
    private String reportDate;
    private String reportTime;
    private int reportStatus;
    private Double reportLatitude;
    private Double reportLongitude;
    private Integer reportAltitude;

    private Integer positionActualAltitude;
    private String positionFL;
    private Integer positionOnGround;
    private Integer positionInAirport;
    private String positionAirport;
    private String positionFpAircraft;
    private String positionFpOrigin;
    private String positionFpDestination;

    private int flightStatus;
    private String flightFirstSeen;
    private String flightOrigin;
    private String flightDestination;
    private String flightLastSeen;
    private String flightTime;

    public static PilotStatusDto create(PilotContext pilotContext) throws IOException {
        PilotStatusDto result = new PilotStatusDto();

        Position position = pilotContext.getPosition();

        result.reportId = position.getReportId();
        LocalDateTime reportDt = position.getDt();
        result.reportDate = JavaTime.yMd.format(reportDt);
        result.reportTime = JavaTime.Hms.format(reportDt);

        ReportPilotPosition reportPilotPosition = pilotContext.getMainContext().getReportDatasource().loadPilotPosition(position.getReportId(), pilotContext.getPilotNumber());
        if (reportPilotPosition != null) {
            result.reportStatus = 1;
            result.reportLatitude = reportPilotPosition.getLatitude();
            result.reportLongitude = reportPilotPosition.getLongitude();
            result.reportAltitude = reportPilotPosition.getAltitude();
        } else {
            result.reportStatus = 0;
        }

        if (position.isPositionKnown()) {
            result.positionActualAltitude = position.getActualAltitude();
            result.positionFL = position.getActualFL();
            result.positionOnGround = position.isOnGround() ? 1 : 0;
            result.positionInAirport = position.isInAirport() ? 1 : 0;
            result.positionAirport = position.getAirportIcao();
            result.positionFpAircraft = position.getFpAircraftType();
            result.positionFpOrigin = position.getFpOrigin();
            result.positionFpDestination = position.getFpDestination();
        }

        Flight currentFlight = pilotContext.getCurrentFlight();
        if (currentFlight != null) {
            result.flightStatus = currentFlight.getStatus().ordinal();
            result.flightFirstSeen = printFlightPosition(currentFlight.getFirstSeen());
            result.flightOrigin = printFlightPosition(currentFlight.getOrigin());
            result.flightDestination = printFlightPosition(currentFlight.getDestination());
            result.flightLastSeen = printFlightPosition(currentFlight.getLastSeen());
            result.flightTime = JavaTime.toHhmm(currentFlight.getFlightTime());
        } else {
            result.flightStatus = -1;
        }

        return result;
    }

    public long getRId() {
        return reportId;
    }

    public String getRDate() {
        return reportDate;
    }

    public String getRTime() {
        return reportTime;
    }

    public int getRSt() {
        return reportStatus;
    }

    public Integer getRAlt() {
        return reportAltitude;
    }

    public Integer getPAA() {
        return positionActualAltitude;
    }

    public String getPFL() {
        return positionFL;
    }

    public Integer getPGnd() {
        return positionOnGround;
    }

    public Integer getPInPort() {
        return positionInAirport;
    }

    public String getPPort() {
        return positionAirport;
    }

    public String getFpAcft() {
        return positionFpAircraft;
    }

    public String getFpOri() {
        return positionFpOrigin;
    }

    public String getFpDest() {
        return positionFpDestination;
    }

    public int getFSt() {
        return flightStatus;
    }

    public String getFFS() {
        return flightFirstSeen;
    }

    public String getFOrig() {
        return flightOrigin;
    }

    public String getFDest() {
        return flightDestination;
    }

    public String getFLS() {
        return flightLastSeen;
    }

    public String getFTime() {
        return flightTime;
    }

    private static String printFlightPosition(Position position) {
        if (position != null) {
            return String.format("%s at %s", position.isInAirport() ? position.getAirportIcao().trim() : "Flying", JavaTime.hhmm.format(position.getDt()));
        } else {
            return null;
        }
    }
}
