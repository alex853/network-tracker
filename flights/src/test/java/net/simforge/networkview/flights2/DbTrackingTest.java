package net.simforge.networkview.flights2;

import net.simforge.commons.io.Csv;
import net.simforge.commons.io.IOHelper;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.flights.datasource.CsvDatasource;
import net.simforge.networkview.flights.datasource.ReportDatasource;
import net.simforge.networkview.flights2.persistence.DBPersistenceLayer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DbTrackingTest {
    private static final Logger logger = LoggerFactory.getLogger(DbTrackingTest.class.getName());

    private Report report;

    @Test
    public void test() throws IOException {
        ReportDatasource reportDatasource = loadReportDatasource();
        PersistenceLayer persistenceLayer = new DBPersistenceLayer();

        for (int i = 1; i <= 60; i++) {
            MainContext mainContext = new MainContext(reportDatasource, persistenceLayer);

            mainContext.setLastReport(report);
            mainContext.processReports(1);
            report = mainContext.getLastReport();

            invokeSingleReportCheckMethod();
        }
    }

    private void invokeSingleReportCheckMethod() {
        invokeCheckMethod("report_" + report.getId());
    }

    private void invokeCheckMethod(String checkMethodName) {
        try {
            Method method = getClass().getMethod(checkMethodName);
            method.invoke(this);
            logger.info("Report " + report.getId() + ": checked, check method " + checkMethodName);
        } catch (NoSuchMethodException e) {
            //logger.info("No checks for report " + report.getId());
        } catch (IllegalAccessException e) {
            logger.info("Can't access to checks for report " + report.getId() + ", check method " + checkMethodName);
        } catch (InvocationTargetException e) {
            Throwable throwable = e.getTargetException();
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException) throwable;
            } else if (throwable instanceof Error) {
                throw (Error) throwable;
            } else {
                logger.error("Exception during checks for report " + report.getId(), e);
            }
        }
    }

    private CsvDatasource loadReportDatasource() throws IOException {
        InputStream is = Class.class.getResourceAsStream("/snapshots/pilot-1309680_from-1_amount-60.csv");
        String csvContent = IOHelper.readInputStream(is);
        return new CsvDatasource(Csv.fromContent(csvContent));
    }
}
