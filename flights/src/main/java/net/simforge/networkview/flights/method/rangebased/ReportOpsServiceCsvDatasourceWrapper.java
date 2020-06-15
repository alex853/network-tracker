package net.simforge.networkview.flights.method.rangebased;

import net.simforge.commons.io.Csv;
import net.simforge.commons.io.IOHelper;
import net.simforge.networkview.core.report.persistence.Report;
import net.simforge.networkview.core.report.persistence.ReportOpsService;
import net.simforge.networkview.core.report.persistence.ReportPilotPosition;
import net.simforge.networkview.flights.method.eventbased.datasource.CsvDatasource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReportOpsServiceCsvDatasourceWrapper implements ReportOpsService {
    private CsvDatasource datasource;

    public ReportOpsServiceCsvDatasourceWrapper(String filename) {
        String csvContent;
        try {
            csvContent = IOHelper.loadFile(new File(filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        datasource = new CsvDatasource(Csv.fromContent(csvContent));
    }

    @Override
    public Report loadFirstReport() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Report loadNextReport(String report) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Report> loadAllReports() {
        List<Report> result = new ArrayList<>();
        Report report = null;
        while (true) {
            try {
                report = datasource.loadNextReport(report != null ? report.getReport() : null);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (report == null) {
                break;
            }
            result.add(report);
        }
        return result;
    }

    @Override
    public List<Report> loadReports(String sinceReport, String tillReport) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ReportPilotPosition> loadPilotPositions(Report report) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ReportPilotPosition> loadPilotPositions(int pilotNumber) {
        return datasource.loadPilotPositions(pilotNumber, 0, Integer.MAX_VALUE);
    }

    @Override
    public List<ReportPilotPosition> loadPilotPositionsSinceTill(int pilotNumber, String sinceReport, String tillReport) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ReportPilotPosition> loadPilotPositionsTill(int pilotNumber, String tillReport) {
        throw new UnsupportedOperationException();
    }
}
