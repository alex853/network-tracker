package net.simforge.networkview.core.report.snapshot;

import net.simforge.commons.io.Csv;
import net.simforge.commons.io.IOHelper;
import net.simforge.networkview.core.Network;
import net.simforge.networkview.core.report.persistence.*;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class MakeSnapshot {
    public static void main(String[] args) throws IOException {
        System.out.print("Enter pilot number: ");
        Scanner in = new Scanner(System.in);
        int pilotNumber = in.nextInt();

        Csv csv = new Csv();
        CsvSnapshotReportOpsService.addColumns(csv);

        ReportSessionManager reportSessionManager = new ReportSessionManager();
        ReportOpsService reportOpsService = new BaseReportOpsService(reportSessionManager, Network.VATSIM);

        Report fromReport = reportOpsService.loadFirstReport();
        Report currentReport = fromReport;

        int reportsAmount = 0;
        while (currentReport != null) {
            System.out.println("    Report " + currentReport.getReport());
            reportsAmount++;
            ReportPilotPosition reportPilotPosition = reportOpsService.loadPilotPosition(pilotNumber, currentReport);
            CsvSnapshotReportOpsService.addRow(csv, currentReport, reportPilotPosition);
            currentReport = reportOpsService.loadNextReport(currentReport.getReport());
        }
        reportSessionManager.dispose();

        String filename = String.format("./pilot-%s_from-%s_amount-%s.csv", pilotNumber, fromReport.getId(), reportsAmount);
        IOHelper.saveFile(new File(filename), csv.getContent());
    }
}
