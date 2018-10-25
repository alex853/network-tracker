package net.simforge.tracker.webapp.rest;

import net.simforge.commons.misc.JavaTime;
import net.simforge.commons.misc.RestUtils;
import net.simforge.tracker.Network;
import net.simforge.networkview.datafeeder.ReportStorage;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import net.simforge.tracker.tools.ReportUtils;
import net.simforge.tracker.webapp.WebAppContext;
import net.simforge.tracker.webapp.dto.NetworkStatusDto;
import net.simforge.tracker.webapp.dto.PilotPositionDto;
import net.simforge.tracker.webapp.util.DtoHelper;
import net.simforge.tracker.webapp.util.IconHelper;
import net.simforge.tracker.world.Position;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("service/map")
public class MapProvider {

    private ServletContext servletContext;
    private WebAppContext webAppContext;

    private static final Logger log = LoggerFactory.getLogger(MapProvider.class.getName());

    @Context
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
        this.webAppContext = WebAppContext.get(servletContext);
    }

    @GET
    @Path("status")
    @Produces(MediaType.APPLICATION_JSON)
    public NetworkStatusDto getNetworkStatus(
            @QueryParam("network") String networkName) throws IOException, SQLException {
        Network network = Network.valueOf(networkName);
        return webAppContext.getNetworkStatus(network);
    }

    @GET
    @Path("lastDownloadedReport")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLastDownloadedReport(
            @QueryParam("network") String networkName) throws IOException, SQLException {
        String storageRoot = "/simforge/data";

        Network network = Network.valueOf(networkName);

        ReportStorage storage = ReportStorage.getStorage(storageRoot, network);
        String lastReport = storage.getLastReport();
        LocalDateTime lastReportDt = ReportUtils.fromTimestampJava(lastReport);

        Duration timeSinceLastReport = Duration.between(lastReportDt, JavaTime.nowUtc());
        long minutesSinceLastReport = timeSinceLastReport.toMinutes();

        Map<String, Object> result = new HashMap<>();
        result.put("minutesSinceLastReport", minutesSinceLastReport);
        result.put("status", minutesSinceLastReport < 10 ? "actual" : "outdated");

        return Response.ok(RestUtils.success(result)).build();
    }

    @GET
    @Path("icon")
    @Produces("image/png")
    public Response getIcon(
            @QueryParam("image") String imageName,
            @QueryParam("angle") int angle,
            @QueryParam("status") String status) throws IOException {
        String cacheKey = String.format("%s|%s|%s", imageName, angle, status);

        Map<String, byte[]> iconCache = webAppContext.getIconCache();
        byte[] imageBytes;

        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (iconCache) {
            imageBytes = iconCache.get(cacheKey);
        }

        if (imageBytes == null) {
            InputStream is = servletContext.getResourceAsStream("/" + imageName);
            BufferedImage image = IconHelper.load(is);

            image = IconHelper.rotateIcon(image, angle);

            if ("outdated".equals(status)) {
                image = IconHelper.makeImageGray(image);
                image = IconHelper.makeImageTransparent(image, 64);
            } else if ("detailed".equals(status)) {
                image = IconHelper.makeImageGray(image);
                image = IconHelper.changeColor(image, new Color(0xFF, 0xD0, 0x10));
            } else if ("highlighted".equals(status)) {
                image = IconHelper.makeImageGray(image);
                image = IconHelper.changeColor(image, new Color(0xFF, 0x60, 0x00));
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            imageBytes = baos.toByteArray();

            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (iconCache) {
                iconCache.put(cacheKey, imageBytes);
            }
        }

        return Response.ok(imageBytes).build();
    }

    @GET
    @Path("track")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPilotTrack(
            @QueryParam("network") String networkName,
            @QueryParam("pilotNumber") int pilotNumber) throws IOException {
        log.info(String.format("Loading track data for pilot %s, network %s", pilotNumber, networkName));

        Session session = null;
        try {
            try {
                Network network = Network.valueOf(networkName);
                session = webAppContext.getSessionManager().getSession(network);

                String thresholdTimestamp = ReportUtils.toTimestamp(JavaTime.nowUtc().minusDays(1));

                //noinspection JpaQlInspection
                Report thresholdReport = (Report) session
                        .createQuery("select r from Report r where r.report < :threshold and r.parsed = true order by r.report desc")
                        .setString("threshold", thresholdTimestamp)
                        .setMaxResults(1)
                        .uniqueResult();

                if (thresholdReport == null) {
                    // what to do in this case?
                    throw new IllegalStateException("Could not find ");
                }

                //noinspection JpaQlInspection
                @SuppressWarnings("unchecked")
                List<ReportPilotPosition> reportPilotPositions = session
                        .createQuery("select p from ReportPilotPosition p where p.pilotNumber = :pilotNumber and p.report >= :report order by report desc")
                        .setInteger("pilotNumber", pilotNumber)
                        .setEntity("report", thresholdReport)
                        .list();
                List<PilotPositionDto> pilotPositionDtos = new ArrayList<>();
                Position currentPosition = null;
                int tailCounter = -1;
                for (ReportPilotPosition reportPilotPosition : reportPilotPositions) {
                    Position position = Position.create(reportPilotPosition);

                    if (tailCounter != -1) {
                        tailCounter--;
                        if (tailCounter == 0) {
                            break;
                        }
                    } else {
                        if (currentPosition == null) {
                            currentPosition = position;
                        } else {
                            if (currentPosition.isOnGround() != position.isOnGround()) {
                                tailCounter = 5;
                            }
                            currentPosition = position;
                        }
                    }

                    PilotPositionDto pilotPositionDto = DtoHelper.getPilotPositionDto(reportPilotPosition, position);

                    pilotPositionDtos.add(pilotPositionDto);
                }

                return Response.ok(pilotPositionDtos).build();
            } catch (Exception e) {
                String msg = String.format("Unable to retrieve track for pilot %s, network %s", pilotNumber, networkName);
                log.error(msg, e);
                return Response.serverError().entity(msg).build();
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
