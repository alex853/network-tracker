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
<%@ page import="java.util.Collections" %>
<%@ page import="web.*" %>
<%@ page import="core.MovementExtra" %>
<%

    String icao = request.getParameter("icao");
    icao = icao.toUpperCase();

    Stats.track_noExc("AirportTimeline", request, icao);

    Airport airport = TrackerData.airports.getByIcao(icao);

    Connection connx = DB.getConnection();
    List<Movement> movements =
            Persistence.loadWhere(
                    connx,
                    Movement.class,
                    "(dep_icao = '" + icao + "' and state <> " + Movement.State.Jump.ordinal() + ")" +
                    " or (arr_icao = '" + icao + "' and state = " + Movement.State.Done.ordinal() + ")" +
                    " or (planned_arr_icao = '" + icao + "' and state = " + Movement.State.InProgress.ordinal() + ")" +
                    " order by dep_report_id desc limit 100");
    connx.close();

    for (Movement movement : movements) {
        MovementExtraCalculator.calculate(movement);
    }

    Collections.sort(movements, new AirportTimelineMovementComparator(icao));
    Collections.reverse(movements);

%>
<html>

<head>

    <title>Airport <%=icao%> / Timeline</title>

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
        <td class="value"><a href="airport.jsp?icao=<%=icao%>">See <%=icao%> flights</a></td>
    </tr>
</table>

<!-- todo callsign with link to movement page -->
<!-- todo flown time/dist -->
<!-- todo date of flight -->
<!-- todo now line -->

<div class="flightsTable">
<div class="header">Flights</div>
<table>
    <tr>
        <th colspan="4">Inbound flights</th>
        <th><%=icao%></th>
        <th colspan="4">Outbound flights</th>
    </tr>
    <tr>
        <th colspan="2">&nbsp;</th>
        <th colspan="2">Departure</th>
        <th>Timeline</th>
        <th colspan="2">Arrival</th>
        <th colspan="2">&nbsp;</th>
    </tr>
    <tr>
        <th>Callsign</th>
        <th>State</th>
        <th>Time</th>
        <th>From</th>
        <th>&nbsp;</th>
        <th>To</th>
        <th>Time</th>
        <th>State</th>
        <th>Callsign</th>
    </tr>

<%

    if (movements != null && !movements.isEmpty()) {

        for (Movement movement : movements) {

            if (!movement.getExtra().isOutbound(icao)) {

%>
    <tr>
        <%=MovementToHtml.print(movement, MovementToHtml.AirportTimelineInboundFlights)%>
        <td colspan="4" class="no-borders">&nbsp;</td>
    </tr>
<%

            } else {

%>
    <tr>
        <td colspan="4" class="no-borders">&nbsp;</td>
        <%=MovementToHtml.print(movement, MovementToHtml.AirportTimelineOutboundFlights)%>
    </tr>
<%

            }

        }
    } else { %>
    <tr>
        <td colspan="7">No flights tracked</td>
    </tr>
<%

    }

    %>
</table>
</div>

</body>
</html>
