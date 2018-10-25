<%@ page import="tracker.Stats" %>
<%@ page import="tracker.TrackerData" %>
<%@ page import="world.Airport" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="forge.commons.db.DB" %>
<%@ page import="forge.commons.persistence.Persistence" %>
<%@ page import="entities.Pilot" %>
<%@ page import="java.util.List" %>
<%@ page import="entities.Movement" %>
<%@ page import="org.joda.time.DateMidnight" %>
<%@ page import="org.joda.time.DateTime" %>
<%@ page import="entities.Report" %>
<%@ page import="web.Links" %>
<%@ page import="web.Web" %>
<%@ page import="web.WebCache" %>
<%@ page import="web.MovementToHtml" %>
<%

    String icao = request.getParameter("icao");
    icao = icao.toUpperCase();

    Stats.track_noExc("Airport", request, icao);

    Airport airport = TrackerData.airports.getByIcao(icao);

    Connection connx = DB.getConnection();
//    List<Pilot> pilots = Persistence.loadWhere(connx, Pilot.class, "state in (" + Pilot.State.Offline.ordinal() + ", " + Pilot.State.InAirport.ordinal() + ") and icao = '" + icao + "' order by pilot_number");
    List<Movement> movements =
            Persistence.loadWhere(
                    connx,
                    Movement.class,
                    "(dep_icao = '" + icao + "'" +
                    " or arr_icao = '" + icao + "'" +
                    " or planned_arr_icao = '" + icao + "')" +
                    " and state <> " + Movement.State.Jump.ordinal() +
                    " order by dep_report_id desc limit 100");
    connx.close();

%>
<html>

<head>

    <title>Airport <%=icao%> / Pilots and last flights</title>

    <LINK href="style.css" rel="stylesheet" type="text/css">

    <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
    <META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">
    <META HTTP-EQUIV="REFRESH" CONTENT="120">

    <%@ include file="uservoice.jsp"%>

</head>

<body>

<table class="maininfo">
    <tr>
        <td class="label">ICAO</td>
        <td class="value"><%=airport.getIcao()%></td>
    </tr>
    <tr>
        <td class="label">&nbsp;</td>
        <td class="value"><a href="airport-timeline.jsp?icao=<%=icao%>">See <%=icao%> timeline</a></td>
    </tr>
</table>

<div class="divtable">
<div class="header">Pilots in airport</div>
<table>
    <tr>
        <th>Pilot #</th>
        <th>Status</th>
    </tr>

<!-- todo -->
</table>
</div>

<div class="flightsTable">
<div class="header">Flights</div>
<table>
    <tr>
        <th>&nbsp;</th>
        <th>&nbsp;</th>
        <th colspan="2">Departure</th>
        <th colspan="2">Arrival</th>
        <th colspan="2">Flown</th>
        <th>&nbsp;</th>
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
        <th>Pilot #</th>
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
                %><%=Web.df.print(eachDate)%><%
            } else {
                %>&nbsp;<%
            } %></td>
        <%=MovementToHtml.print(movement, MovementToHtml.AirportFlights)%>
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

</body>
</html>
