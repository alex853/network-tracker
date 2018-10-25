package web;

import entities.Movement;
import entities.Report;
import entities.ReportPilotPosition;
import entities.Pilot;
import forge.commons.Str;
import forge.commons.Geo;
import forge.commons.db.DB;
import net.simforge.commons.misc.Misc;
import net.simforge.commons.persistence.Persistence;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import world.Airport;
import tracker.TrackerData;
import core.UpdateStamp;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.text.DecimalFormat;

public class MovementToHtml {
    private static String nbsp = "&nbsp;";
    private static String br = "<br>";

    private static String fpdata = "fp-data";
    private static String eta = "eta";

    public static String print(Movement movement, Column[] columns) throws SQLException {
        Map<Column, String> columnMap = new HashMap<Column, String>();

        Pilot cachedPilot = WebCache.getPilot(movement.getPilotId());
        boolean inProgress = Movement.State.InProgress.equals(movement.getState());
        boolean isJump = Movement.State.Jump.equals(movement.getState());

        String clazz = getClazz(movement.getState());

        columnMap.put(Column.Callsign, td(Links.movement(movement), clazz));

        String stateAsString = movement.getState().toString();
        columnMap.put(Column.State, td(stateAsString, clazz));
        columnMap.put(Column.StateAsImage, td(img("ms-" + stateAsString + ".png", stateAsString), clazz));

        columnMap.put(Column.PilotNumber, td(Links.pilot(cachedPilot.getPilotNumber()), clazz));

        String depIcao = movement.getDepIcao();
        String plannedDepIcao = movement.getPlannedDepIcao();

        String cell;
        String cellClazz = clazz;
        if (!Str.isEmpty(depIcao)) {
            cell = airport(depIcao, null);
        } else {
            Connection connx = DB.getConnection();
            ReportPilotPosition position =
                    Persistence.loadSingleWhere(
                            connx,
                            ReportPilotPosition.class,
                            "pilot_number = " + cachedPilot.getPilotNumber() + " "
                                    + "and report_id = " + movement.getDepReportId());
            connx.close();

            if (position == null) {
                cell = "?";
            } else {
                cell = coordsToString(position);
            }
            cellClazz = "caution";
        }
        if (!Str.isEmpty(plannedDepIcao)) {
            if (!ifMatch(depIcao, plannedDepIcao)) {
                cell += br + span("(" + airport(plannedDepIcao, fpdata) + " filed)", fpdata);
                cellClazz = "caution";
            }
        }
        columnMap.put(Column.DepFrom, td(cell, cellClazz));

        Report report = WebCache.getReport(movement.getDepReportId());
        DateTime depDt = report != null ? report.getReportDt() : null;

        columnMap.put(Column.DepDate, td(depDt != null ? Web.df.print(depDt) : nbsp, clazz));
        columnMap.put(Column.DepTime, td(depDt != null ? Web.tf.print(depDt) : nbsp, clazz));

        String arrIcao = movement.getArrIcao();
        String plannedArrIcao = movement.getPlannedArrIcao();
        cellClazz = clazz;
        if (!inProgress) {
            if (!Str.isEmpty(arrIcao)) {
                cell = airport(arrIcao, null);
            } else {
                Connection connx = DB.getConnection();
                List<ReportPilotPosition> positions =
                        Persistence.loadByQuery(
                                connx,
                                ReportPilotPosition.class,
                                "select * from %tn% "
                                        + "where pilot_number = " + cachedPilot.getPilotNumber() + " "
                                        + "and report_id <= " + movement.getArrReportId() + " "
                                        + "order by report_id desc limit 1");
                connx.close();

                ReportPilotPosition position = positions != null && !positions.isEmpty() ?
                        positions.get(0) : null;
                if (position == null) {
                    cell = "?";
                } else {
                    cell = coordsToString(position);
                }
                cellClazz = "caution";
            }
            if (!Str.isEmpty(plannedArrIcao)) {
                if (!ifMatch(arrIcao, plannedArrIcao)) {
                    cell += br + span("(" + airport(plannedArrIcao, fpdata) + " filed)", fpdata);
                    cellClazz = "caution";
                }
            }
        } else { // InProgress
            if (!Str.isEmpty(plannedArrIcao)) {
                cell = span(airport(plannedArrIcao, eta), eta);
            } else {
                cell = br + span("(no route filed)", fpdata);
            }
        }
        columnMap.put(Column.ArrTo, td(cell, cellClazz));

        report = WebCache.getReport(movement.getArrReportId());
        DateTime arrDt = report != null ? report.getReportDt() : null;
        boolean arrDtEta = false;

        Duration flownTime = (depDt != null && arrDt != null) ? new Duration(depDt, arrDt) : null;

        if (inProgress) {
            Airport dep = TrackerData.airports.getByIcao(movement.getDepIcao());
            Airport plannedArr = TrackerData.airports.getByIcao(plannedArrIcao);

            if (dep != null && plannedArr != null) {
                Connection connx = DB.getConnection();

                Pilot pilot = Persistence.load(connx, Pilot.class, movement.getPilotId());
                DateTime currReportDt = UpdateStamp.updateToDateJT(pilot.getReport());
                flownTime = new Duration(depDt, currReportDt);

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
                arrDt = new DateTime(currReportDt.getMillis() + remainedMillis);
                arrDtEta = true;
            }

        }

        if (arrDt != null) {
            if (arrDtEta) {
                columnMap.put(Column.ArrTime, td(span(Web.tf.print(arrDt), eta), clazz));
            } else {
                columnMap.put(Column.ArrTime, td(Web.tf.print(arrDt), clazz));
            }
        } else {
            columnMap.put(Column.ArrTime, td(nbsp, clazz));
        }

        columnMap.put(Column.FlownTime, td(flownTime != null ? DateTimeFormat.forPattern("HH:mm").withZone(DateTimeZone.UTC).print(flownTime.getMillis()) : nbsp, clazz));

        if (!isJump) {
            DecimalFormat distanceFormat = new DecimalFormat("0.0");
            columnMap.put(Column.FlownDistance, td(distanceFormat.format(movement.getFlownDistance()), clazz));
        } else {
            columnMap.put(Column.FlownDistance, td(nbsp, clazz));
        }

        columnMap.put(Column.AircraftType, td(Misc.mn(movement.getAircraftType(), nbsp), clazz));

        columnMap.put(Column.AircraftRegNo, td(Misc.mn(movement.getAircraftRegNo(), nbsp), clazz));

        columnMap.put(Column.Actions, td(nbsp, clazz));

        StringBuilder out = new StringBuilder();
        for (Column column : columns) {
            String columnValue = columnMap.get(column);
            if (columnValue == null) {
                columnValue = td(nbsp, clazz);
            }
            out.append(columnValue);
        }
        return out.toString();
    }

    private static String img(String imgFile, String title) {
        return "<img src='img/" + imgFile + "' title='" + title + "'/>";
    }

    private static String airport(String icao, String clazz) {
        return Links.airportClazz(icao, clazz);
    }

    private static String coordsToString(ReportPilotPosition position) {
        return (position.getLatitude() > 0 ? "N" : "S")
                + Str.z((int) Math.round(Math.abs(position.getLatitude())), 2) + " "
                + (position.getLongitude() > 0 ? "E" : "W")
                + Str.z((int) Math.round(Math.abs(position.getLongitude())), 3);
    }

    private static boolean ifMatch(String icao, String plannedIcao) {
        if (Str.isEmpty(icao) && Str.isEmpty(plannedIcao)) {
            return true;
        }
        if (!Str.isEmpty(icao)) {
            return icao.equals(plannedIcao);
        } else {
            return false;
        }
    }

    private static String getClazz(Movement.State state) {
        return state.toString();
    }

    private static String td(String state, String clazz) {
        return tag("td", state, clazz);
    }

    private static String span(String state, String clazz) {
        return tag("span", state, clazz);
    }

    private static String tag(String tag, String state, String clazz) {
        return "<" + tag + (clazz != null ? " class=\"" + clazz + "\"" : "") + ">"
                + state
                + "</" + tag +">";
    }

    public enum Column {
        State,
        StateAsImage,
        PilotNumber,
        Callsign,
        DepFrom,
        DepDate,
        DepTime,
        ArrTo,
        ArrTime,
        FlownTime,
        FlownDistance,
        AircraftType,
        AircraftRegNo,
        Actions
    }

    public static Column[] PilotFlights =
            {Column.State,
                    Column.DepFrom, Column.DepTime,
                    Column.ArrTo, Column.ArrTime,
                    Column.FlownTime, Column.FlownDistance,
                    Column.AircraftType, Column.AircraftRegNo,
                    Column.Actions};
    public static Column[] AirportFlights =
            {Column.State,
                    Column.DepFrom, Column.DepTime,
                    Column.ArrTo, Column.ArrTime,
                    Column.FlownTime, Column.FlownDistance,
                    Column.PilotNumber,
                    Column.AircraftType, Column.AircraftRegNo,
                    Column.Actions};
    public static Column[] AirportTimelineInboundFlights =
            {Column.Callsign, Column.StateAsImage, Column.DepTime, Column.DepFrom, Column.ArrTime};
    public static Column[] AirportTimelineOutboundFlights =
            {Column.DepTime, Column.ArrTo, Column.ArrTime, Column.StateAsImage, Column.Callsign};
}
