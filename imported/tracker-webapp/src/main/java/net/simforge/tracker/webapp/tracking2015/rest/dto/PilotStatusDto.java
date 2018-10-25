package net.simforge.tracker.webapp.tracking2015.rest.dto;

import net.simforge.commons.misc.Misc;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import net.simforge.networkview.flights.model.Flight;
import net.simforge.networkview.flights.model.PilotContext;
import net.simforge.networkview.flights.model.events.TrackingEvent;
import net.simforge.tracker.tools.ReportUtils;
import net.simforge.tracker.world.Position;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PilotStatusDto {

    private long reportId;
    private String reportDate;
    private String reportTime;
    private Double reportLatitude;
    private Double reportLongitude;
    private Integer reportAltitude;

    private String positionFpAircraft;
    private String positionFpOrigin;
    private String positionFpDestination;
    private String positionStatus;

    private String psEvents;

    private List<FlightDto> flights;

    public static PilotStatusDto create(PilotContext pilotContext) throws IOException {
        PilotStatusDto result = new PilotStatusDto();

        Position position = pilotContext.getPosition();

        result.reportId = position.getReportId();

        Report report = pilotContext.getMainContext().getReportDatasource().loadReport(position.getReportId());
        DateTime reportDt = ReportUtils.fromTimestamp(report.getReport());
        result.reportDate = Misc.yMd.print(reportDt);
        result.reportTime = Misc.Hms.print(reportDt);

        ReportPilotPosition reportPilotPosition = pilotContext.getMainContext().getReportDatasource().loadPilotPosition(position.getReportId(), pilotContext.getPilotNumber());
        if (reportPilotPosition != null) {
            result.reportLatitude = reportPilotPosition.getLatitude();
            result.reportLongitude = reportPilotPosition.getLongitude();
            result.reportAltitude = reportPilotPosition.getAltitude();
        }

        result.positionStatus = position.getStatus();

        if (position.isPositionKnown()) {
            result.positionFpAircraft = position.getFpAircraftType();
            result.positionFpOrigin = position.getFpOrigin();
            result.positionFpDestination = position.getFpDestination();
        }

        result.psEvents = "";
        List<TrackingEvent> events = pilotContext.getEvents(position.getReportId(), false);
        Iterator<TrackingEvent> iterator = events.iterator();
        while (iterator.hasNext()) {
            TrackingEvent event = iterator.next();
            result.psEvents += event.getType();
            if (iterator.hasNext()) {
                result.psEvents += "<br>";
            }
        }

        result.flights = new ArrayList<>();

        List<Flight> flights = pilotContext.getFlights();
        for (Flight flight : flights) {
            // adding in backward order
            result.flights.add(0, FlightDto.create(flight));
        }

        return result;
    }

    public long getReportId() {
        return reportId;
    }

    public String getReportDate() {
        return reportDate;
    }

    public String getReportTime() {
        return reportTime;
    }

    public Double getReportLatitude() {
        return reportLatitude;
    }

    public Double getReportLongitude() {
        return reportLongitude;
    }

    public Integer getReportAltitude() {
        return reportAltitude;
    }

    public String getPositionStatus() {
        return positionStatus;
    }

    public String getPositionFpAircraft() {
        return positionFpAircraft;
    }

    public String getPositionFpOrigin() {
        return positionFpOrigin;
    }

    public String getPositionFpDestination() {
        return positionFpDestination;
    }

    public String getPsEvents() {
        return psEvents;
    }

    public List<FlightDto> getFlights() {
        return flights;
    }
}
