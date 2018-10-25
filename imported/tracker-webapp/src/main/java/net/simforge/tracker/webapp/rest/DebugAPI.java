package net.simforge.tracker.webapp.rest;

import net.simforge.commons.misc.Misc;
import net.simforge.commons.misc.RestUtils;
import net.simforge.tracker.Network;
import net.simforge.tracker.datafeeder.persistence.ReportPilotPosition;
import net.simforge.tracker.flights.datasource.ReportDatasource;
import net.simforge.tracker.flights.datasource.SinglePilotDBDatasource;
import net.simforge.tracker.flights.model.MainContext;
import net.simforge.tracker.flights.model.PilotContext;
import net.simforge.tracker.tools.ReportUtils;
import net.simforge.tracker.webapp.WebAppContext;
import net.simforge.tracker.webapp.dto.PilotStatusDto;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Path("debug")
public class DebugAPI {
    private static final Logger logger = LoggerFactory.getLogger(DebugAPI.class.getName());

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private ServletContext servletContext;
    private WebAppContext webAppContext;

    @Context
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
        this.webAppContext = WebAppContext.get(servletContext);
    }

    @POST
    @Path("pilotPositions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response pilotPositions(
            @FormParam("pilotNumber") int pilotNumber,
            @FormParam("date") String dateStr) throws IOException, SQLException {
        logger.debug("loading pilot positions");

        try (Session session = webAppContext.getSessionManager().getSession(Network.VATSIM)) {
            LocalDate fromDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            LocalDate toDate = fromDate.plusDays(1);

            ReportDatasource datasource = SinglePilotDBDatasource.load(session, pilotNumber, toTimestamp(fromDate), toTimestamp(toDate));
            List<PilotStatusDto> pilotStatusDtos = doTracking(datasource, Integer.MAX_VALUE);

            return Response.ok(RestUtils.success(pilotStatusDtos)).build();
        } catch (Exception e) {
            logger.error("Could not load flights", e);

            String msg = String.format("Could not load flights: %s", Misc.messagesBr(e));
            return Response.ok(RestUtils.failure(msg)).build();
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
            logger.error("Processing error", e);
        }

        return result;
    }

    private String toTimestamp(LocalDate localDate) {
        return ReportUtils.toTimestamp(localDate.atStartOfDay());
    }
}
