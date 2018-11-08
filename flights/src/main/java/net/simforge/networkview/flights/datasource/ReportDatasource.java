package net.simforge.networkview.flights.datasource;

import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;

import java.io.IOException;
import java.util.List;

public interface ReportDatasource {

    Report loadReport(long reportId) throws IOException;

    Report loadReport(String report) throws IOException;

    List<Report> loadReports(long fromReportId, long toReportId);

    /**
     *
     * @param report timestamp of previous report, or null if we need to load "first" report from timeline
     * @return
     * @throws IOException
     */
    Report loadNextReport(String report) throws IOException;

    ReportPilotPosition loadPilotPosition(long reportId, int pilotNumber) throws IOException;

    List<ReportPilotPosition> loadPilotPositions(long reportId) throws IOException;

    List<ReportPilotPosition> loadPilotPositions(int pilotNumber, long fromReportId, long toReportId);

}
