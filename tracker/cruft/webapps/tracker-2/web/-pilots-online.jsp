<%@ page import="tracker.Stats" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="forge.commons.db.DB" %>
<%@ page import="core.DBOps" %>
<%@ page import="forge.commons.persistence.Persistence" %>
<%@ page import="entities.Pilot" %>
<%@ page import="java.util.List" %>
<%@ page import="entities.Report" %>
<%@ page import="web.Links" %>
<%@ page import="entities.Movement" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="web.Web" %>
<%@ page import="org.joda.time.DateTime" %>
<%

    Stats.track_noExc("PilotsOnline", request);

    Report report = DBOps.findLastProcessedReport();

    Connection connx = DB.getConnection();
    List<Pilot> pilots = Persistence.loadWhere(connx, Pilot.class, "report_id >= " + (report.getId()-6));
    List<Movement> movements =
            Persistence.loadByQuery(
                    connx,
                    Movement.class,
                    "select m.* from %tn% m inner join pilot p on p.movement_id = m.id where p.report_id >= " + (report.getId()-6));
    Map<Integer, Movement> id2movement = new HashMap<Integer, Movement>();
    for (Movement movement : movements) {
        id2movement.put(movement.getId(), movement);
    }
    connx.close();

%>

<html>

<head>

    <title>Pilots currently online</title>

    <LINK href="style.css" rel="stylesheet" type="text/css">

    <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
    <META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">
    <META HTTP-EQUIV="REFRESH" CONTENT="30">

    <%@ include file="uservoice.jsp"%>

</head>

<body>

<%=report.getReportDt()%>

<div class="divtable">
<div class="header">Pilots online and current flights</div>
<table>
    <tr>
        <th>Pilot #</th>
        <th>Pilot state</th>
        <th>Location /<br>
            Dep from
        </th>
        <th>Dep time</th>
        <th>Arr to</th>
        <th>Arr time</th>
        <th>Aircraft</th>
        <th>Reg No</th>
    </tr>

<%

    for (Pilot pilot : pilots) {

        Movement movement = id2movement.get(pilot.getMovementId());

        %>
    <tr>
        <td><%=Links.pilot(pilot.getPilotNumber())%></td>
        <td><%=pilot.getState()%></td>
        <%
            if (movement != null) {

                Report aReport = Persistence.load(connx, Report.class, movement.getDepReportId());
                DateTime depDt = aReport != null ? aReport.getReportDt() : null;

                aReport = Persistence.load(connx, Report.class, movement.getArrReportId());
                DateTime arrDt = aReport != null ? aReport.getReportDt() : null;

        %>
        <td><%=Links.airport(movement.getDepIcao())%></td>
        <td><%=depDt != null ? Web.tf.print(depDt) : ""%></td>
        <td><%=Links.airport(movement.getArrIcao())%></td>
        <td><%=arrDt != null ? Web.tf.print(arrDt) : ""%></td>
        <td><%=movement.getAircraftType() != null ? movement.getAircraftType() : ""%></td>
        <td><%=movement.getAircraftRegNo() != null ? movement.getAircraftRegNo() : ""%></td>
        <%

            } else {

        %>
        <td><%=Links.airport(pilot.getIcao())%></td>
        <%

            } 

        %>
    </tr>
    <%

    }

%>

</table>
</div>

</body>

</html>
