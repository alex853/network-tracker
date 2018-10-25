<%@ page import="forge.commons.db.DB" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.Statement" %>
<%@ page import="java.sql.ResultSet" %>

<table>
    <tr>
        <th>Date</th>
        <th>Page</th>
        <th>Addr</th>
        <th>Name</th>
        <th>Count</th>
    </tr>
<%

    long started = System.currentTimeMillis();

    String sql =
            "select cast(dt as date) as date, page, addr, name, count(*) as cnt " +
            "from web_stats " +
            "group by cast(dt as date), page, addr, name " +
            "order by cast(dt as date), page, addr, name";

    Connection connx = DB.getConnection();
    Statement st = connx.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {

    %>

    <tr>
        <td><%=rs.getDate(1)%></td>
        <td><%=rs.getString(2)%></td>
        <td><%=rs.getString(3)%></td>
        <td><%=rs.getString(4)%></td>
        <td><%=rs.getInt(5)%></td>
    </tr>

    <%

        }

    rs.close();
    st.close();
    connx.close();

%>

</table>
<br>
<br>
Elapsed: <%=(System.currentTimeMillis() - started)%>
