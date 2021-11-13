package net.simforge.networkview.flights.method.rangebased;

import net.simforge.commons.io.Csv;
import net.simforge.commons.io.IOHelper;
import net.simforge.networkview.core.report.ReportInfo;
import net.simforge.networkview.core.report.persistence.Report;
import net.simforge.networkview.core.report.snapshot.CsvSnapshotReportOpsService;
import net.simforge.networkview.flights.flight.Flight1;
import net.simforge.networkview.flights.storage.InMemoryFlightStorage;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

@Ignore
public class Test_Pilot913904_2018_11_23 {
    @Test
    public void test() throws IOException {
        int pilotNumber = 913904;

        InputStream is = Class.class.getResourceAsStream("/snapshots/pilot-913904_2018-11-23.csv");
        String csvContent = IOHelper.readInputStream(is);
        Csv csv = Csv.fromContent(csvContent);
        CsvSnapshotReportOpsService reportOpsService = new CsvSnapshotReportOpsService(csv);
        List<Report> allReports = reportOpsService.loadAllReports();
        ReportInfo fromReport = allReports.get(0);
        ReportInfo toReport = allReports.get(allReports.size() - 1);

        InMemoryFlightStorage flightStorageService = new InMemoryFlightStorage();

        ProcessorPOCStatusService statusService = new ProcessorPOCStatusServiceStub();

        ProcessorPOC processor = new ProcessorPOC();
        processor.setReportOpsService(reportOpsService);
        processor.setFlightStorageService(flightStorageService);
        processor.setStatusService(statusService);

        while (true) {
            processor.process();

            ReportInfo lastProcessedReport = statusService.loadLastProcessedReport();
            if (lastProcessedReport.getId().equals(toReport.getId())) {
                break;
            }
        }

        Collection<Flight1> flights = flightStorageService.loadFlights(pilotNumber, fromReport, toReport);
        assertEquals(4, flights.size());

        List<Flight1> sortedFlights = new ArrayList<>(flights);
        sortedFlights.sort(Flight1::compareByTakeoff);

        Flight1 flight = sortedFlights.get(0);
        assertEquals("EGKK", flight.getDepartureIcao());
        assertEquals("EGJJ", flight.getArrivalIcao());

        flight = sortedFlights.get(1);
        assertEquals("EGJJ", flight.getDepartureIcao());
        assertEquals("EGKK", flight.getArrivalIcao());

        flight = sortedFlights.get(2);
        assertEquals("EGKK", flight.getDepartureIcao());
        assertEquals("EHAM", flight.getArrivalIcao());

        flight = sortedFlights.get(3);
        assertEquals("EHAM", flight.getDepartureIcao());
        assertEquals("EHAM", flight.getArrivalIcao());
    }
}
