package net.simforge.ot.db;

import entities.Report;
import entities.ReportFpRemarks;
import entities.ReportPilotPosition;
import forge.commons.BM;
import forge.commons.db.CP;
import net.simforge.commons.persistence.BaseEntity;
import net.simforge.commons.persistence.Persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class DBOps {

    private static DBOps _live;
    private static DBOps _archive;

    public static DBOps live() {
        if (_live == null) {
            _live = new DBOps("live", new CP());
        }
        return _live;
    }

    public static DBOps archive() {
        if (_archive == null) {
            _archive = new DBOps("archive", new CP("archive"));
        }
        return _archive;
    }

    //*** Base DBOps code **********************************************************************************************

    private String name;
    private CP cp;

    public DBOps(String name, CP cp) {
        this.name = name;
        this.cp = cp;
    }

    public Connection getConnection() throws SQLException {
        return cp.getConnection();
    }

    //*** Report functions *********************************************************************************************

    public Report loadReport(int reportId) throws SQLException {
        BM.start("DBOps.loadReport/" + name);
        Connection connx = getConnection();
        try {
            return Persistence.load(connx, Report.class, reportId);
        } finally {
            connx.close();
            BM.stop();
        }
    }

    public Report loadReport(String report) throws SQLException {
        BM.start("DBOps.loadReport/" + name);
        Connection connx = getConnection();
        try {
            return Persistence.loadSingleWhere(connx,
                    Report.class,
                    String.format("report = '%s'", report));
        } finally {
            connx.close();
            BM.stop();
        }
    }

    public Report loadFirstReport(Boolean parsed) throws SQLException {
        BM.start("DBOps.loadFirstReport/" + name);
        Connection connx = getConnection();
        try {
            String sql = (parsed != null ? parsedCondition(parsed) : "1 = 1")
                    + " order by report limit 1";
            return Persistence.loadSingleWhere(connx, Report.class, sql);
        } finally {
            connx.close();
            BM.stop();
        }
    }

    public Report loadNextReport(int reportId, Boolean parsed) throws SQLException {
        BM.start("DBOps.loadNextReport/" + name);
        Connection connx = getConnection();
        try {
            String sql = String.format("id > %s %s order by report limit 1",
                    reportId,
                    parsedAndCondition(parsed));
            return Persistence.loadSingleWhere(connx, Report.class, sql);
        } finally {
            connx.close();
            BM.stop();
        }
    }

    public Report loadPreviousReport(int reportId, Boolean parsed) throws SQLException {
        BM.start("DBOps.loadPreviousReport/" + name);
        Connection connx = getConnection();
        try {
            String sql = String.format("id < %s %s order by report desc limit 1",
                    reportId,
                    parsedAndCondition(parsed));
            return Persistence.loadSingleWhere(connx, Report.class, sql);
        } finally {
            connx.close();
            BM.stop();
        }
    }


    private String parsedAndCondition(Boolean parsed) {
        return parsed != null ? " and " + parsedCondition(parsed) : "";
    }

    private String parsedCondition(Boolean parsed) {
        return String.format("parsed = '%s'", parsed ? "Y" : "N");
    }

    //*** Pilot positions functions ************************************************************************************

    public List<ReportPilotPosition> loadPilotPositions(Report report) throws SQLException {
        BM.start("DBOps.loadPilotPositions/" + name);
        Connection connx = getConnection();
        try {
            return Persistence.loadWhere(connx,
                    ReportPilotPosition.class,
                    String.format("report_id = %s", report.getId()));
        } finally {
            connx.close();
            BM.stop();
        }
    }

    public ReportPilotPosition loadPilotPosition(int reportId, int pilotNumber) throws SQLException {
        BM.start("DBOps.loadPilotPosition/" + name);
        Connection connx = getConnection();
        try {
            return Persistence.loadSingleWhere(connx,
                    ReportPilotPosition.class,
                    String.format("report_id = %s and pilot_number = %s", reportId, pilotNumber));
        } finally {
            connx.close();
            BM.stop();
        }
    }

    public ReportPilotPosition loadLastPilotPosition(int pilotNumber) throws SQLException {
        BM.start("DBOps.loadLastPilotPosition/" + name);
        Connection connx = getConnection();
        try {
            return Persistence.loadSingleWhere(connx,
                    ReportPilotPosition.class,
                    String.format("pilot_number = %s order by report_id desc limit 1", pilotNumber));
        } finally {
            connx.close();
            BM.stop();
        }
    }

    public ReportPilotPosition loadPreviousPilotPosition(ReportPilotPosition currentPosition) throws SQLException {
        BM.start("DBOps.loadPreviousPilotPosition/" + name);
        Connection connx = getConnection();
        try {
            return Persistence.loadSingleWhere(connx,
                    ReportPilotPosition.class,
                    String.format("report_id < %s and pilot_number = %s order by report_id desc limit 1",
                            currentPosition.getReportId(),
                            currentPosition.getPilotNumber()));
        } finally {
            connx.close();
            BM.stop();
        }
    }

    //*** FP Remarks functions *****************************************************************************************

    public ReportFpRemarks findFpRemarks(String fpRemarksStr) throws SQLException {
        BM.start("DBOps.findFpRemarks/" + name);
        Connection connx = getConnection();
        try {
            return Persistence.loadSingleWhere(connx,
                    ReportFpRemarks.class,
                    "fp_remarks = '" + fpRemarksStr.replace("'", "''") + "'");
        } finally {
            connx.close();
            BM.stop();
        }
    }

    public ReportFpRemarks loadFpRemarks(int fpRemarksId) throws SQLException {
        BM.start("DBOps.loadFpRemarks/" + name);
        try {
            return loadSingleWhere(ReportFpRemarks.class, "id = " + fpRemarksId);
        } finally {
            BM.stop();
        }
    }

    //*** Persistence functions ****************************************************************************************

    public <T extends BaseEntity> T create(T entity) throws SQLException {
        BM.start("DBOps.create/" + name);
        Connection connx = getConnection();
        try {
            return Persistence.create(connx, entity);
        } finally {
            connx.commit();
            connx.close();
            BM.stop();
        }
    }

    public <T extends BaseEntity> T loadSingleWhere(Class<T> clazz, String where) throws SQLException {
        BM.start("DBOps.loadSingleWhere/" + name);
        Connection connx = getConnection();
        try {
            return Persistence.loadSingleWhere(connx, clazz, where);
        } finally {
            connx.close();
            BM.stop();
        }
    }
}
