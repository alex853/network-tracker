<%@ page import="tracker.TrackerData" %>
<%@ page import="java.util.List" %>
<%@ page import="tracker.PilotData" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="web.FLCalc" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="tracker.Stats" %>
<%@ page import="entities.Report" %>
<%@ page import="org.joda.time.format.DateTimeFormat" %>
<%@ page import="org.joda.time.format.DateTimeFormatter" %>
<%

    Stats.track_noExc("PilotPositionLog", request);

    String pilotStr = request.getParameter("pilot");
    int pilotNumber = Integer.parseInt(pilotStr);

    TrackerData trackerData = TrackerData.getOrLoad(getServletConfig().getServletContext());

%>

<html>

<head>

    <title>Pilot #<%=pilotNumber%> / Position log</title>

    <LINK href="style.css" rel="stylesheet" type="text/css">

    <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
    <META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">
    <META HTTP-EQUIV="REFRESH" CONTENT="120">

</head>

<body>

<table class="maininfo">
    <tr>
        <td class="label">Pilot #</td>
        <td class="value"><%=pilotNumber%></td>
    </tr>
</table>

<div class="divtable">
<div class="header">Position log</div>
<table cellspacing="3">
    <tr>
        <th>Report</th>
        <th>Rep Alt</th>
        <th>Rep QNH</th>
        <th colspan="6">Nearest Airport</th>
        <th>Alt Mode</th>
        <th>Act Alt</th>
        <th>Act FL</th>
        <th>On Ground</th>
        <th colspan="4">Segment</th>
    </tr>
    <tr>
        <th colspan="3"></th>
        <th>ICAO</th>
        <th>Dist</th>
        <th>Alt</th>
        <th>Alt-r<br>Policy</th>
        <th>TA</th>
        <th>TL</th>
        <th colspan="4"></th>
        <th>Type</th>
        <th>Distance,<br>nm</th>
        <th>Time,<br>secs</th>
        <th>Speed,<br>knots</th>
    </tr>
    <%

        DateTimeFormatter repDtFormat = DateTimeFormat.forPattern("HH:mm:ss");
        DecimalFormat altitudeFormat = new DecimalFormat("###,##0");
        DecimalFormat distanceFormat = new DecimalFormat("0.0");

        List<Report> reports = trackerData.getReports();
        PilotData pilotData = trackerData.getPilots().get(pilotNumber);
        List<PilotData.Segment> segments = new ArrayList<PilotData.Segment>(pilotData.getSegments());

        if (pilotData != null) {

            PilotData.Segment currSegment = null;
            boolean currSegmentShown = false;
            for (Iterator<Report> it = reports.iterator(); it.hasNext(); ) {
                Report report = it.next();

                if (segments.size() > 0) {
                    PilotData.Segment segment = segments.get(0);
                    if (segment.getP1().getPosition().getReportId() == report.getId()) {
                        currSegment = segment;
                        currSegmentShown = false;
                        segments.remove(0);
                    }
                } else {
                    currSegment = null;
                    currSegmentShown = false;
                }

                PilotData.Position position = pilotData.getPositionByReportId(report.getId());

    %>
    <tr>
        <td><%=repDtFormat.print(report.getReportDt())%></td>

        <%
                if (position != null) {

        %>
        <td><%=altitudeFormat.format(position.getPosition().getAltitude())%></td>
        <td><%=position.getPosition().getQnhMb()%></td>
        <%

                    if (position.getNearestAirport() != null) {
        %>
        <td><%=position.getNearestAirport().getAirport().getIcao()%></td>
        <td><%=distanceFormat.format(position.getNearestAirport().getDistance())%></td>
        <td><%=altitudeFormat.format(position.getNearestAirport().getAirport().getAltitude())%></td>
        <td><%=position.getNearestAirportAltimeterPolicy()%></td>
        <td><%=altitudeFormat.format(position.getNearestAirportTA())%></td>
        <td>FL<%=FLCalc.altToFL(position.getNearestAirportTL())%></td>
        <%
                    } else {
        %>
        <td>n/a</td>
        <td colspan="5"></td>
        <%
                }

        %>
        <td><%=position.getAltimeterMode()%></td>
        <td><%=altitudeFormat.format(position.getActualAltitude())%></td>
        <td><%=position.getActualFL()%></td>
        <td><%=position.isOnGround()%></td>
        <%

            } else {

        %>
        <td>n/a</td>
        <td colspan="11"></td>
        <%
            }

            if (currSegment != null) {

                if (!currSegmentShown) {

        %>
        <td><%=currSegment.getType()%></td>
        <td><%=distanceFormat.format(currSegment.getDistance())%></td>
        <td><%=currSegment.getDurationInSecs()%></td>
        <td><%=distanceFormat.format(currSegment.getSpeed())%></td>
        <%

                    currSegmentShown = true;
                } else {
                    %>
        <td>--||--</td>
        <td colspan="3"></td>
        <%
                }
            } else if (it.hasNext()) { // we do not print anything after last report row

                %>
        <td>n/a</td>
        <td colspan="3"></td>
        <%
            } else {
                %>
        <td colspan="4"></td>
        <%
            }
        %>
    </tr>
    <%


        }

    %>
</table>
</div>
<%
    }
%>

</body>

</html>
