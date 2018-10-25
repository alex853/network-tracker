<%@ page import="tracker.Stats" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="forge.commons.db.DB" %>
<%@ page import="forge.commons.persistence.Persistence" %>
<%@ page import="forge.Aircraft" %>
<%@ page import="entities.Movement" %>
<%@ page import="java.util.List" %>
<%@ page import="org.joda.time.DateMidnight" %>
<%@ page import="entities.Report" %>
<%@ page import="org.joda.time.DateTime" %>
<%@ page import="org.joda.time.format.DateTimeFormat" %>
<%@ page import="org.joda.time.format.DateTimeFormatter" %>
<%@ page import="web.Links" %>
<%

    Stats.track_noExc("Aircraft", request);

    String aircraftIdStr = request.getParameter("aircraftId");
    int aircraftId = Integer.parseInt(aircraftIdStr);

    Connection connx = DB.getConnection();

    Aircraft aircraft = Persistence.load(connx, Aircraft.class, aircraftId);
    List<Movement> movements = Persistence.loadWhere(connx, Movement.class, "aircraft_id = " + aircraftId + " order by id desc");

    DateTimeFormatter df = DateTimeFormat.forPattern("yyyy/MM/dd");
    DateTimeFormatter tf = DateTimeFormat.forPattern("HH:mm");
    DateTimeFormatter urlDf = DateTimeFormat.forPattern("yyyyMMdd");

%>
Type: <%=aircraft.getType()%><br>
ICAO: <%=aircraft.getIcao()%><br>
<br>
Movements
<table>
    <tr>
        <th>Date</th>
        <th>State</th>
        <th>Dep time</th>
        <th>Dep from</th>
        <th>Arr time</th>
        <th>Arr to</th>
        <th>Pilot #</th>
        <th>Aircraft</th>
        <th>Reg No</th>
    </tr>

<%

    DateMidnight currDate = null;

    for (Movement movement : movements) {

        Report report = Persistence.loadSingleWhere(connx, Report.class, "id = " + movement.getDepReportId());
        DateTime depDt = report.getReportDt();

        report = Persistence.loadSingleWhere(connx, Report.class, "id = " + movement.getArrReportId());
        DateTime arrDt = report != null ? report.getReportDt() : null;

        DateMidnight eachDate = depDt.toDateMidnight();
        if (eachDate.equals(currDate)) {
            eachDate = null;
        } else {
            currDate = eachDate;
        }

%>
    <tr>
        <td><%=eachDate != null ? df.print(eachDate) : ""%></td>
        <td><%=movement.getState()%></td>
        <td><%=tf.print(depDt)%></td>
        <td><%=Links.airport(movement.getDepIcao(), "----")%></td>
        <td><%=arrDt != null ? tf.print(arrDt) : "--:--"%></td>
        <td><%=Links.airport(movement.getArrIcao(), "----")%></td>
        <td><%=Links.pilot(movement.getPilotId())%></td>
        <td><%=movement.getAircraftType() != null ? movement.getAircraftType() : "n/a"%></td>
        <td><%=movement.getAircraftRegNo() != null ? movement.getAircraftRegNo() : "n/a"%></td>
    </tr>
    <%

    }

%>



</table>

<%

    connx.close();


%>
