package web;

import entities.Movement;
import entities.Report;
import entities.Pilot;
import entities.ReportPilotPosition;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import world.Airport;
import forge.commons.db.DB;
import net.simforge.commons.persistence.Persistence;
import forge.commons.Geo;
import core.UpdateStamp;
import core.MovementExtra;
import tracker.TrackerData;

import java.sql.SQLException;
import java.sql.Connection;

public class MovementExtraCalculator {

    public static void calculate(Movement movement) throws SQLException {
        MovementExtra extra = new MovementExtra(movement);
        movement.setExtra(extra);

        Report report = WebCache.getReport(movement.getDepReportId());
        DateTime depDt = report != null ? report.getReportDt() : null;

        boolean inProgress = Movement.State.InProgress.equals(movement.getState());
        String plannedArrIcao = movement.getPlannedArrIcao();

        if (inProgress) {
            Airport dep = TrackerData.airports.getByIcao(movement.getDepIcao());
            Airport plannedArr = TrackerData.airports.getByIcao(plannedArrIcao);

            if (dep != null && plannedArr != null) {
                Connection connx = DB.getConnection();

                Pilot pilot = Persistence.load(connx, Pilot.class, movement.getPilotId());
                DateTime currReportDt = UpdateStamp.updateToDateJT(pilot.getReport());
                Duration flownTime = new Duration(depDt, currReportDt);

                ReportPilotPosition position = Persistence.loadSingleWhere(connx, ReportPilotPosition.class, "pilot_number = " + pilot.getPilotNumber() + " and report_id = " + pilot.getReportId());

                connx.close();

                double coveredDist =
                        Geo.distanceNM(
                                dep.getLatitude(), dep.getLongitude(),
                                position.getLatitude(), position.getLongitude());
                double remainedDist =
                        Geo.distanceNM(
                                position.getLatitude(), position.getLongitude(),
                                plannedArr.getLatitude(), plannedArr.getLongitude());

                long remainedMillis = (long) (flownTime.getMillis() * remainedDist / Math.max(coveredDist, 0.001));
                extra.setEstimatedArrTime(new DateTime(currReportDt.getMillis() + remainedMillis));
            }
        }
    }
}
