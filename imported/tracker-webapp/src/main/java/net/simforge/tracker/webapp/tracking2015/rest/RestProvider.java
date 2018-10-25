package net.simforge.tracker.webapp.tracking2015.rest;

import net.simforge.commons.io.Csv;
import net.simforge.commons.legacy.misc.Settings;
import net.simforge.tracker.flights.model.MainContext;
import net.simforge.tracker.flights.model.PilotContext;
import net.simforge.tracker.webapp.WebAppContext;
import net.simforge.tracker.Network;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import net.simforge.tracker.flights.datasource.CsvDatasource;
import net.simforge.tracker.flights.datasource.SinglePilotDBDatasource;
import net.simforge.tracker.flights.datasource.ReportDatasource;
import net.simforge.tracker.webapp.tracking2015.rest.dto.PilotStatusDto;
import net.simforge.tracker.webapp.tracking2015.rest.dto.SnapshotDto;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Path("tracking2015")
public class RestProvider {

    private static final Logger log = LoggerFactory.getLogger(RestProvider.class.getName());

    private WebAppContext webAppContext;

    @Context
    public void setServletContext(ServletContext servletContext) {
        log.info("servletContext is being set");
        this.webAppContext = WebAppContext.get(servletContext);
    }

    @GET
    @Path("snapshot/list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SnapshotDto> getSnapshots() {
        List<SnapshotDto> result = new ArrayList<>();

        String snapshotsFolder = getSnapshotsFolder();
        if (snapshotsFolder == null) {
            return result;
        }

        File storage = new File(snapshotsFolder);
        //noinspection ResultOfMethodCallIgnored
        storage.mkdirs();
        File[] files = storage.listFiles();
        if (files != null) {
            for (File file : files) {
                result.add(new SnapshotDto(file.getName()));
            }
        }

        return result;
    }

    @POST
    @Path("tracking/data/snapshot")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PilotStatusDto> getTrackingDataFromSnapshot(
            @FormParam("snapshotName") String snapshotName) throws IOException, SQLException {
        Csv csv = Csv.load(new File(getSnapshotsFolder() + snapshotName));
        CsvDatasource datasource = new CsvDatasource(csv);

        // ugly hack is below
        String s = snapshotName.split("\\.")[0];
        String[] parts = s.split("_");
//        int pilotNumber  = Integer.parseInt(parts[0].split("-")[1]);
//        int fromReportId = Integer.parseInt(parts[1].split("-")[1]);
        int reportsAmount = Integer.parseInt(parts[2].split("-")[1]);

        return doTracking(datasource, reportsAmount);
    }

    @POST
    @Path("tracking/data/database")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PilotStatusDto> getTrackingDataFromDatabase(
            @FormParam("network") String networkName,
            @FormParam("pilot") String pilotNumberStr,
            @FormParam("fromReportDt") String fromReportDt,
            @FormParam("reports") String reportsAmountStr) throws IOException, SQLException {
        Network network = Network.valueOf(networkName);

        int pilotNumber = Integer.parseInt(pilotNumberStr);
        int reportsAmount = Integer.parseInt(reportsAmountStr);

        Session session = webAppContext.getSessionManager().getSession(network, fromReportDt);
        //noinspection TryFinallyCanBeTryWithResources
        try {
            ReportDatasource datasource = SinglePilotDBDatasource.load(session, pilotNumber, fromReportDt, reportsAmount);

            return doTracking(datasource, reportsAmount);
        } finally {
            session.close();
        }
    }

    private List<PilotStatusDto> doTracking(ReportDatasource reportDatasource, int reportsAmount) {
        final List<PilotStatusDto> result = new ArrayList<>();

        try {
            MainContext mainContext = new MainContext();
            mainContext.setReportDatasource(reportDatasource);
            mainContext.setStrategy(new MainContext.StrategyAdapter() {
                @Override
                public void onPilotContextProcessed(PilotContext pilotContext) {
                    try {
                        result.add(PilotStatusDto.create(pilotContext));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            mainContext.processReports(reportsAmount);
        } catch (Exception e) {
            log.error("Processing error", e);
        }

        return result;
    }

    @GET
    @Path("tracking/data/makeSnapshot")
    @Produces(MediaType.TEXT_PLAIN)
    public Response makeSnapshot(
            @QueryParam("network") String networkName,
            @QueryParam("pilot") String pilotNumberStr,
            @QueryParam("fromReportDt") String fromReportDt,
            @QueryParam("reports") String reportsAmountStr) throws IOException, SQLException {
        Network network = Network.valueOf(networkName);

        int pilotNumber = Integer.parseInt(pilotNumberStr);
        int reportsAmount = Integer.parseInt(reportsAmountStr);

        Csv csv = new Csv();
        CsvDatasource.addColumns(csv);

        Report fromReport;

        Session session = webAppContext.getSessionManager().getSession(network, fromReportDt);
        //noinspection TryFinallyCanBeTryWithResources
        try {
            ReportDatasource datasource = SinglePilotDBDatasource.load(session, pilotNumber, fromReportDt, reportsAmount);

            fromReport = datasource.loadNextReport(fromReportDt);

            int reportsProcessed = 0;
            Report currentReport = fromReport;

            while (reportsProcessed <= reportsAmount && currentReport != null) {

                ReportPilotPosition reportPilotPosition = datasource.loadPilotPosition(currentReport.getId(), pilotNumber);

                CsvDatasource.addRow(csv, currentReport, reportPilotPosition);

                currentReport = datasource.loadNextReport(currentReport.getReport());
                reportsProcessed++;
            }
        } finally {
            session.close();
        }

        String filename = String.format("pilot-%s_from-%s_amount-%s.csv", pilotNumber, fromReport.getId(), reportsAmount);

        return Response.ok(csv.getContent()).header("Content-Disposition", "attachment; filename=\"" + filename + "\"").build();
    }

    private String getSnapshotsFolder() {
        String snapshotsFolder = Settings.get("tracking2015.snapshotsFolder");

        if (snapshotsFolder == null) {
            log.warn("Setting 'tracking2015.snapshotsFolder' is not specified");
            return null;
        }

        snapshotsFolder = snapshotsFolder.trim();
        if (!snapshotsFolder.endsWith("/") && !snapshotsFolder.endsWith("\\")) {
            snapshotsFolder += File.separator;
        }

        return snapshotsFolder;
    }
}
