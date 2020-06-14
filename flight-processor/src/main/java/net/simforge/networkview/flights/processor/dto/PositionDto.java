package net.simforge.networkview.flights.processor.dto;

import net.simforge.commons.misc.Geo;
import net.simforge.networkview.core.report.ReportInfoDto;
import net.simforge.networkview.flights.processors.eventbased.Position;

public class PositionDto {
    private ReportInfoDto reportInfo;
    private Geo.Coords coords;
    private String status;

    public PositionDto(Position position) {
        this.reportInfo = new ReportInfoDto(position.getReportInfo());
        this.coords = position.isPositionKnown() ? position.getCoords() : null;
        this.status = position.getStatus();
    }

    public ReportInfoDto getReportInfo() {
        return reportInfo;
    }

    public Geo.Coords getCoords() {
        return coords;
    }

    public String getStatus() {
        return status;
    }
}
