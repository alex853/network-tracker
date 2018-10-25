<!-- todo reversed order -->

<%@ page import="tracker.Stats" %>
<%@ page import="web.Web" %>
<%@ page import="org.joda.time.DateTime" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="forge.commons.db.DB" %>
<%@ page import="forge.commons.persistence.Persistence" %>
<%@ page import="entities.Report" %>
<%@ page import="core.UpdateStamp" %>
<%@ page import="org.joda.time.DateTimeConstants" %>
<%@ page import="java.util.List" %>
<%@ page import="entities.ReportPilotPosition" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="core.PilotPosition" %>
<%@ page import="tools.DT" %>
<%@ page import="entities.Pilot" %>
<%@ page import="entities.Movement" %>
<%@ page import="web.MovementSearch" %>
<%@ page import="web.Links" %>
<%

    String pilotStr = request.getParameter("pilot");
    int pilotNumber = Integer.parseInt(pilotStr);

    String dayStr = request.getParameter("day");
    DateTime day = Web.urlDf.parseDateTime(dayStr);
    String updateSince = UpdateStamp.toUpdate(day);
    String updateTill = UpdateStamp.toUpdate(new DateTime(day.getMillis() + DateTimeConstants.MILLIS_PER_DAY));

    Stats.track_noExc("PilotDailyLog", request, String.valueOf(pilotNumber), Web.df.print(day));

    Connection connx = DB.getConnection();

    Pilot pilot = Persistence.loadSingleWhere(connx, Pilot.class, "pilot_number = " + pilotNumber);

    String where = "report >= '%s' and report <= '%s' order by report";
    where = String.format(where, updateSince, updateTill);
    List<Report> reports = Persistence.loadWhere(connx, Report.class, where);

    String sql = "select p.* from report as r "
            + "left join report_pilot_position p on r.id = p.report_id "
            + "where r.report >= '%s' and r.report <= '%s' and p.pilot_number = %s order by r.report";
    sql = String.format(sql, updateSince, updateTill, String.valueOf(pilotNumber));
    List<ReportPilotPosition> positions = Persistence.loadByQuery(connx, ReportPilotPosition.class, sql);

    Map<Integer, ReportPilotPosition> reportIdToPosition = new HashMap<Integer, ReportPilotPosition>();
    for (ReportPilotPosition position : positions) {
        reportIdToPosition.put(position.getReportId(), position);
    }

    DateTime now = DT.nowUtc();
    DateTime nextDay = DT.addDay(day, 1);
    boolean isToday = nextDay.isAfter(now);

    connx.close();

    int tableWidth = 14;

%>

<html>

<head>

    <title>Pilot #<%=pilotNumber%> / Daily position log</title>

    <LINK href="style.css" rel="stylesheet" type="text/css">


    <%

        if (isToday) {

    %>

    <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
    <META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">
    <META HTTP-EQUIV="REFRESH" CONTENT="120">

    <%
        
        }

    %>

    <%@ include file="uservoice.jsp"%>

</head>

<body>

<table class="maininfo">
    <tr>
        <td class="label">Pilot #</td>
        <td class="value"><%=Links.pilot(pilotNumber)%></td>
    </tr>
</table>

<div class="divtable">
<div class="days"><a
                href="pilot-daily-log.jsp?pilot=<%=pilotNumber%>&day=<%=Web.urlDf.print(DT.addDay(day, -1))%>"
                     class="prev">&larr; prev day</a><%

    if (isToday) {

        %><span class="next">next day &rarr;</span><%

    } else {
                     %><a
                href="pilot-daily-log.jsp?pilot=<%=pilotNumber%>&day=<%=Web.urlDf.print(nextDay)%>"
                     class="next">next day &rarr;</a><%

    }

                     %></div>
<div class="header">Position log</div>
<table cellspacing="3">
    <tr>
        <th colspan="3">Report</th>
        <th colspan="3">Nearest airport</th>
        <th colspan="6">Calculations</th>
        <th>Flight</th>
        <th colspan="3">Is Continued A</th>
    </tr>
    <tr>
        <th>Time</th>
        <th style="text-align: center;">Alt</th>
        <th style="text-align: center;">QNH</th>
        <th style="text-align: center;">ICAO</th>
        <th style="text-align: center;">Dist</th>
        <th style="text-align: center;">Elev</th>
        <th style="text-align: center;">Mode</th>
        <th style="text-align: center;">Alt</th>
        <th style="text-align: center;">FL</th>
        <th style="text-align: center;">On ground</th>
        <th style="text-align: center;">In airport</th>
        <th style="text-align: center;">State</th>
        <th>&nbsp;</th>
    </tr>

<%!
    int UNKNOWN = 0;
    int OFFLINE = 1;
    int ONLINE = 2;
    int NO_REPORT = 3;

    public boolean isOnline(Report report, Map<Integer, ReportPilotPosition> reportIdToPosition) {
        return reportIdToPosition.containsKey(report.getId());
    }

    public int getState(Report report, Map<Integer, ReportPilotPosition> reportIdToPosition) {
        return isOnline(report, reportIdToPosition) ? ONLINE : OFFLINE;
    }

    public int getLength(Report report, List<Report> reports, Map<Integer, ReportPilotPosition> reportIdToPosition) {
        int state = getState(report, reportIdToPosition);
        int length = 1;
        int i = reports.indexOf(report) + 1;
        for (; i < reports.size(); i++) {
            if (isNoReportsGap(reports.get(i-1).getReportDt(), reports.get(i).getReportDt())) {
                break;
            }
            int currState = getState(reports.get(i), reportIdToPosition);
            if (currState == state) {
                length++;
            } else {
                break;
            }
        }
        return length;
    }

    private boolean isNoReportsGap(DateTime dt1, DateTime dt2) {
        return (dt2.getMillis() - dt1.getMillis()) > 5* DateTimeConstants.MILLIS_PER_MINUTE;
    }

%>


    <%

        DecimalFormat altitudeFormat = new DecimalFormat("###,##0");
        DecimalFormat distanceFormat = new DecimalFormat("0.0");

        int state = UNKNOWN;
        int stateLength = 0;
        int stateIndex = 0;

        MovementSearch movementSearch = null;
        int movementSearchCounter = 0;
        long movementSearchDuration = 0;

        DateTime prevDt = day;

        for (Report report : reports) {

            DateTime reportDt = report.getDt();
            if (isNoReportsGap(prevDt, reportDt)) {

                %>
    <tr class="no-reports">
        <td style="text-align: center;">...</td>
        <td colspan="<%=tableWidth-1%>">No reports (probably due to data feed unavailability or scanning utility failure)</td>
    </tr>
    <%

                state = UNKNOWN;
            }

            prevDt = reportDt;

            int currState = getState(report, reportIdToPosition);
            if (state != currState) {
                state = currState;
                stateLength = getLength(report, reports, reportIdToPosition);
                stateIndex = 0;
            }

            if (state == OFFLINE) {

                if ((stateIndex == 0 || stateIndex == stateLength - 1) || stateLength < 3) {

                    %>
    <tr class="offline">
        <td style="text-align: center;"><%=Web.hm.print(reportDt)%></td>
        <td colspan="<%=tableWidth-1%>">offline</td>
    </tr>
    <%
                } else if (stateIndex == 1) {

                    %>
    <tr class="offline">
        <td style="text-align: center;">...</td>
        <td colspan="<%=tableWidth-1%>">&nbsp;</td>
    </tr>
    <%

                }

            } else {

                ReportPilotPosition reportPosition = reportIdToPosition.get(report.getId());
                PilotPosition pilotPosition = new PilotPosition(reportPosition);

    %>
    <tr>
        <td style="text-align: center;"><%=Web.hm.print(reportDt)%></td>
        <td style="text-align: right;"><%=altitudeFormat.format(reportPosition.getAltitude())%></td>
        <td style="text-align: right;"><%=reportPosition.getQnhMb()%></td>

    <%
        



                if (pilotPosition.getNearestAirport() != null) {
    %>
        <td style="text-align: center;"><%=pilotPosition.getNearestAirport().getAirport().getIcao()%></td>
        <td style="text-align: right;"><%=distanceFormat.format(pilotPosition.getNearestAirport().getDistance())%></td>
        <td style="text-align: right;"><%=altitudeFormat.format(pilotPosition.getNearestAirport().getAirport().getAltitude())%></td>
    <%
                } else {

%>
        <td colspan="3">n/a</td>
    <%
                }


                Pilot.State pilotState = Pilot.State.get(reportPosition);

    %>
        <td style="text-align: center;"><%=pilotPosition.getAltimeterMode()%></td>
        <td style="text-align: right;"><%=altitudeFormat.format(pilotPosition.getActualAltitude())%></td>
        <td style="text-align: right;"><%=pilotPosition.getActualFL()%></td>
        <td style="text-align: center;"><%=pilotPosition.isOnGround()%></td>
        <td style="text-align: center;"><%=pilotPosition.isInNearestAirport()%></td>
        <td style="text-align: center;"><%=pilotState%> <%=Pilot.State.InAirport.equals(pilotState) ? pilotPosition.getNearestAirport().getAirport().getIcao() : ""%></td>

<%
                // movement
                if (movementSearch == null || !movementSearch.isActual(reportPosition.getReportId())) {
                    long t = System.currentTimeMillis();
                    movementSearch = MovementSearch.search(pilot.getId(), reportPosition.getReportId());
                    movementSearchDuration += (System.currentTimeMillis() - t);
                    movementSearchCounter++;
                }

                Movement current = movementSearch.getCurrent();
                if (current != null) {

                    %>
        <td><b><%=movementSearch.toString()%></b></td>
        <%
                } else {

                    %>
        <td><%=movementSearch.toString()%></td>
        <%

                }


%>


    </tr>

    <%
            }

            stateIndex++;
        }

        if (isNoReportsGap(prevDt, isToday ? now : nextDay)) {

            %>
    <tr class="no-reports">
        <td style="text-align: center;">...</td>
        <td colspan="<%=tableWidth-1%>">No reports (probably due to data feed unavailability or scanning utility failure)</td>
    </tr>
    <%
            
        }




        if (isToday) {


            %>

    <tr class="now">
        <td style="text-align: center;"><%=Web.hm.print(now)%></td>
        <td colspan="<%=tableWidth-1%>">Now</td>
    </tr>

    <%

        }


    %>

</table>
<div class="days"><a
                href="pilot-daily-log.jsp?pilot=<%=pilotNumber%>&day=<%=Web.urlDf.print(DT.addDay(day, -1))%>"
                     class="prev">&larr; prev day</a><%

    if (isToday) {

        %><span class="next">next day &rarr;</span><%

    } else {
                     %><a
                href="pilot-daily-log.jsp?pilot=<%=pilotNumber%>&day=<%=Web.urlDf.print(nextDay)%>"
                     class="next">next day &rarr;</a><%

    }

                     %></div>
</div>
movementSearchCounter = <%=movementSearchCounter%>
movementSearchDuration = <%=movementSearchDuration / Math.max(movementSearchCounter, 1)%> per search
<br><%=prevDt%><br><%=now%><br><%=nextDay%><br><%=new DateTime()%>
</body>

</html>
