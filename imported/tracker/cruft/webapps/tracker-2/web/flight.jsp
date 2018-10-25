<%@ page import="tracker.Stats" %>
<%

    Stats.track_noExc("Flight", request);

%>
<html>

<head>

    <title>Flight ??? / Not implemented yet</title>

    <LINK href="style.css" rel="stylesheet" type="text/css">

    <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
    <META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">
    <META HTTP-EQUIV="REFRESH" CONTENT="120">

    <%@ include file="uservoice.jsp"%>

</head>

<body>

<table class="maininfo">
    <tr>
        <td class="label">Flight</td>
        <td class="value">???</td>
    </tr>
</table>

<div class="todo">
    Not implemented yet
</div>

</body>
</html>
