package net.simforge.networkview.flights.processor;

import net.simforge.networkview.core.Network;
import net.simforge.networkview.core.report.ReportInfo;
import net.simforge.networkview.core.report.persistence.BaseReportOpsService;
import net.simforge.networkview.core.report.persistence.Report;
import net.simforge.networkview.core.report.persistence.ReportOpsService;
import net.simforge.networkview.core.report.persistence.ReportPilotPosition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class ReportOpsServiceBean implements ReportOpsService {
    @Autowired
    private ReportSessionManagerBean sessionManager;

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
    public Report loadReport(long reportId) {
        return null;
    }

    @Override
    public List<Report> loadAllReports() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Report> loadReports(String sinceReport, String tillReport) {
        return reportOpsService.loadReports(sinceReport, tillReport);
    }

    @Override
    public List<ReportPilotPosition> loadPilotPositions(ReportInfo reportInfo) {
        return reportOpsService.loadPilotPositions(reportInfo);
    }

    @Override
    public List<ReportPilotPosition> loadPilotPositions(int pilotNumber) {
        return null;
    }

    @Override
    public ReportPilotPosition loadPilotPosition(int pilotNumber, ReportInfo reportInfo) {
        return null;
    }

    @Override
    public List<ReportPilotPosition> loadPilotPositionsSinceTill(int pilotNumber, String sinceReport, String tillReport) {
        return reportOpsService.loadPilotPositionsSinceTill(pilotNumber, sinceReport, tillReport);
    }

    @Override
    public List<ReportPilotPosition> loadPilotPositionsTill(int pilotNumber, String tillReport) {
        return reportOpsService.loadPilotPositionsTill(pilotNumber, tillReport);
    }

}
