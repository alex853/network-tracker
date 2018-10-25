<%@ page import="tracker.Stats" %>
<%@ page import="forge.commons.persistence.Persistence" %>
<%@ page import="entities.Pilot" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="forge.commons.db.DB" %>
<%@ page import="java.util.List" %>
<%@ page import="org.joda.time.DateMidnight" %>
<%@ page import="org.joda.time.DateTime" %>
<%@ page import="entities.Movement" %>
<%@ page import="entities.Report" %>
<%@ page import="web.Links" %>
<%@ page import="web.Web" %>
<%@ page import="web.MovementToHtml" %>
<%@ page import="web.WebCache" %>
<%

    String pilotNumberStr = request.getParameter("pilot");
    int pilotNumber = Integer.parseInt(pilotNumberStr);

    Stats.track_noExc("Pilot", request, String.valueOf(pilotNumber));

    Connection connx = DB.getConnection();

    Pilot pilot = Persistence.loadSingleWhere(connx, Pilot.class, "pilot_number = " + pilotNumber);
    List<Movement> movements = null;
    if (pilot != null) {
        movements = Persistence.loadWhere(connx, Movement.class, "pilot_id = " + pilot.getId() + " order by int_order desc");
    }



%>

<html>

<head>

    <title>Pilot #<%=pilotNumber%> / Status and flights</title>

    <LINK href="style.css" rel="stylesheet" type="text/css">

    <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
    <META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">
    <META HTTP-EQUIV="REFRESH" CONTENT="120">

    <%@ include file="uservoice.jsp"%>

</head>

<body>

<table class="maininfo">
    <tr>
        <td class="label">Pilot #</td>
        <td class="value"><%=pilotNumber%></td>
    </tr>
    <tr>
        <td class="label">State</td>
        <td class="value"><%=pilot != null ? pilot.getState() : "unknown"%></td>
    </tr>
    <tr>
        <td class="label">Current location</td>
        <td class="value"><%=pilot != null ? Links.airport(pilot.getIcao()) : "unknown"%></td>
    </tr>
</table>

<div class="flightsTable">
<div class="header">Flights</div>
<table>
    <tr>
        <th>&nbsp;</th>
        <th>&nbsp;</th>
        <th colspan="2">Departure</th>
        <th colspan="2">Arrival</th>
        <th colspan="2">Flown</th>
        <th colspan="2">Aircraft</th>
        <th>&nbsp;</th>
    </tr>
    <tr>
        <th>Date</th>
        <th>State</th>
        <th>From</th>
        <th>Time</th>
        <th>To</th>
        <th>Time</th>
        <th>Time</th>
        <th>Dist</th>
        <th>Type</th>
        <th>Reg No</th>
        <th>Actions</th>
    </tr>

<%

    if (movements != null && !movements.isEmpty()) {

        DateMidnight currDate = null;

        for (Movement movement : movements) {
            Report report = WebCache.getReport(movement.getDepReportId());
            DateTime depDt = report != null ? report.getReportDt() : null;

            DateMidnight eachDate = null;
            if (depDt != null) {
                eachDate = depDt.toDateMidnight();
                if (eachDate.equals(currDate)) {
                    eachDate = null;
                } else {
                    currDate = eachDate;
                }
            }

%>
    <tr>
        <td><%
            if (eachDate != null) {
                %><a href="pilot-daily-log.jsp?pilot=<%=pilotNumber%>&day=<%=Web.urlDf.print(eachDate)%>"
                     title="See position reports for the date"><%=Web.df.print(eachDate)%></a><%
            } else {
                %>&nbsp;<%
            } %></td>
        <%=MovementToHtml.print(movement, MovementToHtml.PilotFlights)%>
    </tr>
<%

        }
    } else { %>
    <tr>
        <td colspan="9">No flights tracked</td>
    </tr>
<%
    }

%>
</table>
</div>

<%

    connx.close();


%>

</body>

</html>
