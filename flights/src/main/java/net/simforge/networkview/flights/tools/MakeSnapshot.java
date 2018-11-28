package net.simforge.networkview.flights.tools;

import net.simforge.commons.io.Csv;
import net.simforge.commons.io.IOHelper;
import net.simforge.networkview.Network;
import net.simforge.networkview.datafeeder.SessionManager;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import net.simforge.networkview.flights.datasource.CsvDatasource;
import net.simforge.networkview.flights.datasource.DBReportDatasource;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class MakeSnapshot {
    public static void main(String[] args) throws IOException {
        System.out.print("Enter pilot number: ");
        Scanner in = new Scanner(System.in);
        int pilotNumber = in.nextInt();

        Csv csv = new Csv();
        CsvDatasource.addColumns(csv);

        SessionManager sessionManager = new SessionManager();
        DBReportDatasource reportDatasource = new DBReportDatasource(Network.VATSIM, sessionManager);

        Report fromReport = reportDatasource.loadNextReport(null);
        Report currentReport = fromReport;

        int reportsAmount = 0;
        while (currentReport != null) {
            System.out.println("    Report " + currentReport.getReport());
            reportsAmount++;
            ReportPilotPosition reportPilotPosition = reportDatasource.loadPilotPosition(currentReport.getId(), pilotNumber);
            CsvDatasource.addRow(csv, currentReport, reportPilotPosition);
            currentReport = reportDatasource.loadNextReport(currentReport.getReport());
        }
        sessionManager.dispose();

        String filename = String.format("./pilot-%s_from-%s_amount-%s.csv", pilotNumber, fromReport.getId(), reportsAmount);
        IOHelper.saveFile(new File(filename), csv.getContent());
    }
}
