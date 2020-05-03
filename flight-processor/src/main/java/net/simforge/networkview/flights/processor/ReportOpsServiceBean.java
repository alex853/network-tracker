package net.simforge.networkview.flights.processor;

import net.simforge.networkview.Network;
import net.simforge.networkview.core.report.BaseReportOpsService;
import net.simforge.networkview.core.report.ReportOpsService;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class ReportOpsServiceBean implements ReportOpsService {
    @Autowired
    private SessionManagerBean sessionManager;

    private ReportOpsService reportOpsService;

    private Network network = Network.VATSIM;

    @PostConstruct
    public void init() {
        reportOpsService = new BaseReportOpsService(sessionManager.getSessionManager(), network);
    }

    @Override
    public Report loadFirstReport() {
        return reportOpsService.loadFirstReport();
    }

    @Override
    public Report loadNextReport(String report) {
        return reportOpsService.loadNextReport(report);
    }

    @Override
    public List<ReportPilotPosition> loadPilotPositions(Report report) {
        return reportOpsService.loadPilotPositions(report);
    }

    @Override
    public List<ReportPilotPosition> loadPilotPositionsSinceTill(int pilotNumber, String sinceReport, String tillReport) {
        return reportOpsService.loadPilotPositionsSinceTill(pilotNumber, sinceReport, tillReport);
    }

    @Override
    public List<ReportPilotPosition> loadPilotPositionsTill(int pilotNumber, String tillReport) {
        return reportOpsService.loadPilotPositionsTill(pilotNumber, tillReport);
    }

    @Override
    public List<Report> loadReports(String sinceReport, String tillReport) {
        return reportOpsService.loadReports(sinceReport, tillReport);
    }
}
