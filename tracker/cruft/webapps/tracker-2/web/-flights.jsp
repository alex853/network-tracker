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
<%@ page import="web.Web" %>
<%@ page import="org.joda.time.DateTime" %>
<%@ page import="org.joda.time.format.DateTimeFormat" %>
<%@ page import="org.joda.time.DateTimeZone" %>
<%

    Stats.track_noExc("Flights", request);

    Report report = DBOps.findLastProcessedReport();

    Connection connx = DB.getConnection();
    List<Movement> movements =
            Persistence.loadByQuery(
                    connx,
                    Movement.class,
                    "select * from %tn% "
                        + "where state_report_id >= " + (report.getId()-6) + " "
                        + "or (state != " + Movement.State.Terminated.ordinal() + " and state != " + Movement.State.Done.ordinal() + ") "
                        + "order by state, dep_report_id desc");

%>

<html>

<head>

    <title>Flights</title>

    <LINK href="style.css" rel="stylesheet" type="text/css">

    <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
    <META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">
    <META HTTP-EQUIV="REFRESH" CONTENT="30">

    <%@ include file="uservoice.jsp"%>

</head>

<body>

<div class="divtable">
<div class="header">Flights <%=report.getReportDt()%></div>
<table>
    <tr>
        <th>Pilot #</th>
        <th>State</th>
        <th>Dep from</th>
        <th>Dep time</th>
        <th>Arr to</th>
        <th>Arr time</th>
        <th>Duration</th>
        <th>Aircraft</th>
        <th>Reg No</th>
    </tr>

<%

    for (Movement movement : movements) {
        if (movement.getState() == Movement.State.Jump) {
            continue;
        }

        Report aReport = Persistence.load(connx, Report.class, movement.getDepReportId());
        DateTime depDt = aReport != null ? aReport.getReportDt() : null;

        aReport = Persistence.load(connx, Report.class, movement.getArrReportId());
        DateTime arrDt = aReport != null ? aReport.getReportDt() : null;

        long duration;
        if (movement.getState() == Movement.State.InProgress || movement.getState() == Movement.State.Disconnected) {
            duration = report.getReportDt().getMillis() - depDt.getMillis();
            duration = duration < 0 ? 0 : duration;
        } else {
            duration = arrDt.getMillis() - depDt.getMillis(); 
        }

        %>
    <tr>
        <td><%=Links.pilot(movement.getPilotId())%></td>
        <td><%=movement.getState()%></td>
        <td><%=Links.airport(movement.getDepIcao())%></td>
        <td><%=depDt != null ? Web.tf.print(depDt) : ""%></td>
        <td><%=Links.airport(movement.getArrIcao())%></td>
        <td><%=arrDt != null ? Web.tf.print(arrDt) : ""%></td>
        <td><%=Web.tf.withZone(DateTimeZone.UTC).print(duration)%></td>
        <td><%=movement.getAircraftType() != null ? movement.getAircraftType() : ""%></td>
        <td><%=movement.getAircraftRegNo() != null ? movement.getAircraftRegNo() : ""%></td>
    </tr>
    <%

    }

    connx.close();

%>

</table>
</div>

</body>

</html>
