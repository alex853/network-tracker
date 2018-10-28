package net.simforge.networkview.flights2;

import net.simforge.commons.io.Csv;
import net.simforge.commons.io.IOHelper;
import net.simforge.networkview.flights.datasource.CsvDatasource;
import net.simforge.networkview.flights.datasource.ReportDatasource;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class Test1 {
    @Test
    public void test1() throws IOException {
        InputStream is = Class.class.getResourceAsStream("/snapshots/pilot-1309680_from-1_amount-60.csv");
        String csvContent = IOHelper.readInputStream(is);
        ReportDatasource reportDatasource = new CsvDatasource(Csv.fromContent(csvContent));
        PersistenceLayer persistenceLayer = new InMemoryNoOpPersistenceLayer();

        MainContext mainContext = new MainContext(reportDatasource, persistenceLayer);

        for (int i = 0; i < 60; i++) {
            mainContext.processReports(1);
        }
    }
}
