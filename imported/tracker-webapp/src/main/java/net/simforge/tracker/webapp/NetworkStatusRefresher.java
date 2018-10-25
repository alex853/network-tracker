package net.simforge.tracker.webapp;

import net.simforge.commons.misc.JavaTime;
import net.simforge.tracker.Network;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import net.simforge.tracker.tools.ReportUtils;
import net.simforge.tracker.webapp.dto.NetworkStatusDto;
import net.simforge.tracker.webapp.dto.PilotPositionDto;
import net.simforge.tracker.webapp.util.DtoHelper;
import net.simforge.tracker.world.Position;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NetworkStatusRefresher implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(NetworkStatusRefresher.class.getName());

    private final WebAppContext webAppContext;
    private final Network network;

    public NetworkStatusRefresher(WebAppContext webAppContext, Network network) {
        this.webAppContext = webAppContext;
        this.network = network;
    }

    @Override
    public void run() {
        log.info(String.format("Network status refresh - %s: started", network.name()));

        Session session = null;
        try {
            try {
                session = webAppContext.getSessionManager().getSession(network);

                NetworkStatusDto result = new NetworkStatusDto();

                result.setNetwork(network.name());

                String nowTimestamp = ReportUtils.toTimestamp(JavaTime.nowUtc());

                //noinspection JpaQlInspection
                Report report = (Report) session
                        .createQuery("select r from Report r where r.report < :now and r.parsed = true order by r.report desc")
                        .setString("now", nowTimestamp)
                        .setMaxResults(1)
                        .uniqueResult();

                result.setCurrentReport(report.getReport());


                //noinspection JpaQlInspection
                @SuppressWarnings("unchecked")
                List<ReportPilotPosition> reportPilotPositions = session
                        .createQuery("select p from ReportPilotPosition p where p.report = :report")
                        .setEntity("report", report)
                        .list();
                List<PilotPositionDto> pilotPositionDtos = new ArrayList<>();
                for (ReportPilotPosition reportPilotPosition : reportPilotPositions) {
                    Position position = Position.create(reportPilotPosition);

                    PilotPositionDto pilotPositionDto = DtoHelper.getPilotPositionDto(reportPilotPosition, position);

                    pilotPositionDtos.add(pilotPositionDto);
                }
                result.setPilotPositions(pilotPositionDtos);


                LocalDateTime reportDt = ReportUtils.fromTimestampJava(report.getReport());
                long timeDifferenceMillis = JavaTime.nowUtc().toEpochSecond(ZoneOffset.UTC) - reportDt.toEpochSecond(ZoneOffset.UTC);
                long timeDifference = timeDifferenceMillis / TimeUnit.MINUTES.toMillis(1);

                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

                String statusCode;
                String statusMessage;
                String statusDetails;

                if (timeDifference < 5) {
                    statusCode = "OK";
                    statusMessage = String.format("%s flights online", reportPilotPositions.size());
                    switch ((int) timeDifference) {
                        case 0:
                            statusDetails = String.format("Report %s, it is actual data", timeFormatter.format(reportDt));
                            break;
                        case 1:
                            statusDetails = String.format("Report %s, it is actual data", timeFormatter.format(reportDt));
                            break;
                        default:
                            statusDetails = String.format("Report %s, it is %s minutes behind", timeFormatter.format(reportDt), timeDifference);
                            break;
                    }
                } else if (timeDifference < 15) {
                    statusCode = "GAP";
                    statusMessage = String.format("%s flights online", reportPilotPositions.size());
                    statusDetails = String.format("It seems like there is a GAP in reports. Last report %s, it is %s minutes behind", timeFormatter.format(reportDt), timeDifference);
                } else {
                    statusCode = "OUTDATED";
                    statusMessage = "Outdated positions";
                    statusDetails = String.format("Data feed is down most probably. Last report %s, it is %s minutes behind", timeFormatter.format(reportDt), timeDifference);
                }

                result.setCurrentStatusCode(statusCode);
                result.setCurrentStatusMessage(statusMessage);
                result.setCurrentStatusDetails(statusDetails);


                webAppContext.setNetworkStatus(network, result);

                log.info(String.format("Network status refresh - %s: done", network.name()));
            } catch (Exception e) {
                // do not throw it higher as it cracks down Scheduler work
                log.error("Unable to refresh status for network " + network.name(), e);

                NetworkStatusDto result = new NetworkStatusDto();

                result.setNetwork(network.name());
                result.setCurrentStatusCode("ERROR");
                result.setCurrentStatusMessage("Refresh error");
                result.setCurrentStatusDetails(e.getClass().getSimpleName() + ": " + e.getMessage());

                webAppContext.setNetworkStatus(network, result);
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
