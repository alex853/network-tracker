package tracker;

import forge.commons.db.DB;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;

public class Stats {

    public static void track_noExc(String page, HttpServletRequest request) {
        track_noExc(page, request, null, null, null);
    }

    public static void track_noExc(String page, HttpServletRequest request, String p1) {
        track_noExc(page, request, p1, null, null);
    }

    public static void track_noExc(String page, HttpServletRequest request, String p1, String p2) {
        track_noExc(page, request, p1, p2, null);
    }

    public static void track_noExc(String page, HttpServletRequest request, String p1, String p2, String p3) {
        try {
            Connection connx = DB.getConnection();
            try {
                Statement st = connx.createStatement();
                st.executeUpdate(
                        "insert into web_stats(dt, page, addr, name, param1, param2, param3) " +
                                "values(now(), " +
                                "'" + page + "', " +
                                "'" + request.getRemoteAddr() + "', " +
                                "'" + request.getRemoteHost() + "', " +
                                paramToSql(p1) + ", " +
                                paramToSql(p2) + ", " +
                                paramToSql(p3) + ")");
                st.close();
                connx.commit();
            } finally {
                connx.close();
            }
        } catch (SQLException e) {
            System.err.println("Stats exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String paramToSql(String p) {
        return p != null ? "'" + p + "'" : "null";
    }
}
