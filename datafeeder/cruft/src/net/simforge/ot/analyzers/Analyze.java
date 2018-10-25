package net.simforge.ot.analyzers;

import core.DBOps;
import core.PilotPosition;
import core.UpdateStamp;

import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.sql.SQLException;
import java.sql.Connection;

import forge.commons.db.DB;
import net.simforge.commons.persistence.Persistence;
import forge.commons.Misc;
import forge.commons.TimeMS;
import forge.commons.Geo;
import forge.commons.BM;
import net.simforge.commons.logging.LogHelper;
import net.simforge.ot.tools.ParsingLogics;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;
import entities.Report;
import entities.ReportPilotPosition;
import entities.Movement;
import entities.Pilot;

public class Analyze {
    @SuppressWarnings({"UnusedDeclaration"})
    private static int timeZoneOffset = 4 * 3600 * 1000;

    private static Logger logMain = LogHelper.getLogger("analyze");
    private static Logger logDetails = LogHelper.getLogger("analyze-details");
    private static Connection connx;

    private static final String ARG_RESET = "reset";

    public static void main(String[] args) {
        BM.init("Analyze");

        boolean reset = false;

        for (String arg : args) {
            if (arg.startsWith(ARG_RESET)) {
                reset = Boolean.parseBoolean(arg.substring(ARG_RESET.length()));
            }
        }

        if (reset) {
            logMain.log(Level.INFO, "Resetting data");
            try {
                resetData();
            } catch (SQLException e) {
                logMain.log(Level.SEVERE, "Exception during processing", e);
                return;
            }
        }

        //noinspection InfiniteLoopStatement
        while (true) {
            boolean processedAnything = false;
            try {
                processedAnything = process();
            } catch (SQLException e) {
                logMain.log(Level.SEVERE, "Exception during processing", e);
            }

            sleep(processedAnything);

            BM.logPeriodically(true);
        }
    }

    private static void sleep(boolean processedAnything) {
        BM.start();
        Misc.sleep(processedAnything ? 10 : 30000);
        BM.stop();
    }

    private static boolean process() throws SQLException {
        BM.start();

        Report nextReport = DBOps.findFirstUnprocessedReport();
        if (nextReport == null) {
            BM.stop();
            return false;
        }

        logMain.info("Report: "  + Misc.yMdHms.print(nextReport.getReportDt()) + " REP:" + nextReport.getId());
        logDetails.info("Report: "  + Misc.yMdHms.print(nextReport.getReportDt()) + " REP:" + nextReport.getId());

        List<ReportPilotPosition> nextPilotPositions = net.simforge.ot.db.DBOps.live().loadPilotPositions(nextReport);
        logMain.info("    loaded");

        connx = DB.getConnection();

        Report lastReport = DBOps.findLastProcessedReport();
        List<Pilot> lastPilots;
        if (lastReport != null) {
            lastPilots = Persistence.loadWhere(connx, Pilot.class, "report_id = " + lastReport.getId());
        } else {
            lastPilots = new ArrayList<Pilot>();
        }
        Map<Integer, Pilot> lastPilotsMap = new HashMap<Integer, Pilot>();
        for (Pilot pilot : lastPilots) {
            lastPilotsMap.put(pilot.getPilotNumber(), pilot);
        }

        for (ReportPilotPosition nextPilotPosition : nextPilotPositions) {
            int pilotNumber = nextPilotPosition.getPilotNumber();
            lastPilotsMap.remove(pilotNumber);

            Pilot pilot = Persistence.loadSingleWhere(connx, Pilot.class, "pilot_number = " + pilotNumber);
            if (pilot == null) {
                pilot = new Pilot();
                pilot.setPilotNumber(pilotNumber);
                pilot.setState(Pilot.State.Offline);
                PilotPosition nextPP = new PilotPosition(nextPilotPosition);
                if (nextPP.isInNearestAirport()) {
                    pilot.setIcao(nextPP.getNearestAirport().getAirport().getIcao());
                }
                pilot = Persistence.create(connx, pilot);
                logDetails(pilot, "created");
            }

            if (pilot.getReportId() < nextReport.getId()) {
                processReportPosition(pilot, nextReport, nextPilotPosition);
            } else if (pilot.getReportId() > nextReport.getId()) {
                // todo warn
            }
        }

        List<Pilot> list = new ArrayList<Pilot>(lastPilotsMap.values());
        for (Pilot pilotWentOffline : list) {
            markPilotWentOffline(pilotWentOffline, nextReport);
        }

        terminateDisconnectedFlights(nextReport);

        nextReport.setProcessed("Y");
        Persistence.update(connx, nextReport);

        connx.commit();
        connx.close();

        logMain.info("    done");

        BM.stop();
        
        return true;
    }

    private static void terminateDisconnectedFlights(Report nextReport) throws SQLException {
        BM.start();

        MutableDateTime mdt = new MutableDateTime(nextReport.getReportDt());
        mdt.addHours(-6);
        String update = UpdateStamp.toUpdate(mdt.toDateTime());
        Report report = Persistence.loadSingleWhere(connx, Report.class, "report < '" + update + "' order by id desc limit 1");
        if (report == null) {
            BM.stop();
            return;
        }
        List<Movement> movements = Persistence.loadWhere(
                connx,
                Movement.class,
                "state = " + Movement.State.Disconnected.ordinal() + " and state_report_id <= " + report.getId());
        for (Movement movement : movements) {
            movement.setArrReportId(movement.getStateReportId());

            movement.setState(Movement.State.Terminated);
            movement.setStateReportId(nextReport.getId());
            updateMovement(movement);

            Pilot pilot = Persistence.load(connx, Pilot.class, movement.getPilotId());
            pilot.setMovementId(0);
            updatePilot(pilot);
        }

        BM.stop();
    }

    private static void markPilotWentOffline(Pilot pilot, Report nextReport) throws SQLException {
        BM.start();

        if (pilot.getState() != Pilot.State.Offline) {
            pilot.setState(Pilot.State.Offline);
            pilot.setReportId(nextReport.getId());
            pilot.setReport(nextReport.getReport());
            updatePilot(pilot);

            if (pilot.hasActiveMovement()) {
                Movement movement = Persistence.load(connx, Movement.class, pilot.getMovementId());
                if (movement == null || movement.getState() != Movement.State.InProgress) {
                    throw new IllegalStateException();
                }

                movement.setState(Movement.State.Disconnected);
                movement.setStateReportId(nextReport.getId());
                updateMovement(movement);
            }
        }

        BM.stop();
    }

    private static void processReportPosition(Pilot currPilot, Report nextReport, ReportPilotPosition nextReportPilotPosition) throws SQLException {
        BM.start();

        Pilot nextPilot = currPilot.clone();

        Pilot.State currState = currPilot.getState();
        Pilot.State nextState = Pilot.State.get(nextReportPilotPosition);
        PilotPosition nextPP = new PilotPosition(nextReportPilotPosition);

        nextPilot.setState(nextState);
        if (nextState == Pilot.State.InAirport) {
            String nextIcao = nextPP.getNearestAirport() != null ? nextPP.getNearestAirport().getAirport().getIcao() : null;
            nextPilot.setIcao(nextIcao);
        } else if (nextState == Pilot.State.Flying) {
            nextPilot.setIcao(null);
        }

        nextPilot.setReportId(nextReportPilotPosition.getReportId());
        nextPilot.setReport(nextReport.getReport());

        if (currState == Pilot.State.InAirport) {
            if (nextState == Pilot.State.Flying) {
                logDetails(nextPilot, "InAirport->Flying");
                _takeoff(nextPilot, nextPilot.getReportId(), currPilot.getIcao());
            } else if (nextState == Pilot.State.InAirport) {
                _checkForJump(currPilot, nextPilot);
            }
        } else if (currState == Pilot.State.Flying) {
            if (nextState == Pilot.State.InAirport) {
                logDetails(nextPilot, "Flying->InAirport");
                _landing(nextPilot, nextPilot.getReportId(), nextPilot.getIcao());
            } else if (nextState == Pilot.State.Flying) {
                if (nextPilot.hasActiveMovement()) {
                    _calcFlownDistance(nextPilot, nextReportPilotPosition, currPilot);
                }
            }
        } else if (currState == Pilot.State.Offline) {
            if (nextState == Pilot.State.InAirport) {
                logDetails(nextPilot, "Offline->InAirport");
                if (nextPilot.hasActiveMovement()) {
                    if (isContinued(nextPilot.getId(), nextReport.getReportDt(), nextReportPilotPosition)) {
                        _landing(nextPilot, nextPilot.getReportId(), nextPilot.getIcao());
                    } else {
                        _landing(nextPilot, currPilot.getReportId(), null);
                    }
                } else {
                    _checkForJump(currPilot, nextPilot);
                }
            } else if (nextState == Pilot.State.Flying) {
                logDetails(nextPilot, "Offline->Flying");
                if (!nextPilot.hasActiveMovement()) {
                    _takeoff(nextPilot, nextPilot.getReportId(), null);
                } else {
                    if (!isContinued(nextPilot.getId(), nextReport.getReportDt(), nextReportPilotPosition)) {
                        _landing(nextPilot, currPilot.getReportId(), null);
                        _takeoff(nextPilot, nextPilot.getReportId(), null);
                    } else {
                        _movementStillInProgress(nextPilot.getId(), nextReport.getId());
                    }
                }
            }
        }

        updatePilot(nextPilot);
        BM.stop();
    }

    private static void _calcFlownDistance(Pilot nextPilot, ReportPilotPosition nextReportPilotPosition, Pilot currPilot) throws SQLException {
        ReportPilotPosition currReportPilotPosition = Persistence.loadSingleWhere(connx, ReportPilotPosition.class, "report_id = " + currPilot.getReportId() + " and pilot_number = " + currPilot.getPilotNumber());

        if (currReportPilotPosition != null) {
            double dist = Geo.distanceNM(
                    currReportPilotPosition.getLatitude(), currReportPilotPosition.getLongitude(),
                    nextReportPilotPosition.getLatitude(), nextReportPilotPosition.getLongitude());
            Movement movement = Persistence.load(connx, Movement.class, nextPilot.getMovementId());
            movement.setFlownDistance(movement.getFlownDistance() + dist);
            updateMovement(movement);
        }
    }

    private static void updateMovement(Movement movement) throws SQLException {
        BM.start();
        Persistence.update(connx, movement);
        BM.stop();
    }

    private static void updatePilot(Pilot pilot) throws SQLException {
        BM.start();
        Persistence.update(connx, pilot);
        BM.stop();
    }

    private static void logDetails(Pilot pilot, String msg) {
        logDetails.info("        #" + pilot.getPilotNumber() + ": " + msg);
    }

    private static void _checkForJump(Pilot currPilot, Pilot nextPilot) throws SQLException {
        BM.start();

        String currIcao = currPilot.getIcao();
        String nextIcao = nextPilot.getIcao();

        if (currIcao != null && nextIcao != null && !currIcao.equals(nextIcao)) {
            logDetails(nextPilot, "jump");
            
            Movement lastMovement = Persistence.loadSingleWhere(connx, Movement.class, "pilot_id = " + nextPilot.getId() + " order by pilot_id, int_order desc limit 1");
            int nextOrder = 10;
            if (lastMovement != null) {
                nextOrder = lastMovement.getIntOrder() + 10;
            }

            Movement movement = new Movement();

            movement.setPilotId(nextPilot.getId());
            movement.setIntOrder(nextOrder);
            movement.setState(Movement.State.Jump);

            movement.setDepIcao(currIcao);
            movement.setArrIcao(nextIcao);

            Persistence.create(connx, movement);
        }

        BM.stop();
    }

    private static boolean isContinued(int pilotId, DateTime nextReportDt, ReportPilotPosition nextReportPilotPosition) throws SQLException {
        BM.start();

        Pilot pilot = Persistence.load(connx, Pilot.class, pilotId);

        Movement movement = Persistence.load(connx, Movement.class, pilot.getMovementId());

        Report depReport = Persistence.load(connx, Report.class, movement.getDepReportId());
        ReportPilotPosition depPosition = Persistence.loadSingleWhere(connx, ReportPilotPosition.class, "pilot_number = " + pilot.getPilotNumber() + " and report_id = " + movement.getDepReportId());
        if (depPosition == null) {
            throw new IllegalStateException();
        }

        double distance = Geo.distanceNM(
                    depPosition.getLatitude(), depPosition.getLongitude(),
                    nextReportPilotPosition.getLatitude(), nextReportPilotPosition.getLongitude());

        double elapsed =
                (nextReportDt.getMillis() - depReport.getReportDt().getMillis())
                        / (double) TimeMS.HOUR;

        double Vcruise = 440;
        double Vmin = Vcruise * 0.5;
        double Vmax = Vcruise * 1.3;
        double Dmin = elapsed * Vmin;
        double Dmax = elapsed * Vmax;

        BM.stop();

        return Dmin < distance && distance < Dmax;
    }

    private static void _takeoff(Pilot nextPilot, int takeoffReportId, String takeoffIcao) throws SQLException {
        BM.start();

        logDetails(nextPilot, "takeoff " + takeoffIcao);

        Movement lastMovement = Persistence.loadSingleWhere(connx, Movement.class, "pilot_id = " + nextPilot.getId() + " order by pilot_id, int_order desc limit 1");
        int nextOrder = 10;
        if (lastMovement != null) {
            nextOrder = lastMovement.getIntOrder() + 10;
        }

        ReportPilotPosition takeoffPosition = Persistence.loadSingleWhere(connx, ReportPilotPosition.class, "pilot_number = " + nextPilot.getPilotNumber() + " and report_id = " + takeoffReportId);

        Movement movement = new Movement();

        movement.setPilotId(nextPilot.getId());
        movement.setIntOrder(nextOrder);
        movement.setState(Movement.State.InProgress);
        movement.setStateReportId(takeoffReportId);

        movement.setCallsign(takeoffPosition.getCallsign());

        movement.setDepReportId(takeoffReportId);
        movement.setDepIcao(takeoffIcao);
        movement.setPlannedDepIcao(takeoffPosition.getFpDep());

        movement.setPlannedArrIcao(takeoffPosition.getFpDest());
        movement.setFlownDistance(0);

        movement.setAircraftType(ParsingLogics.parseAircraftType(takeoffPosition.getFpAircraft()));
        movement.setAircraftRegNo(takeoffPosition.getParsedRegNo());

        if (movement.getAircraftType() != null) {
            if (movement.getAircraftType().trim().length() == 0) {
                movement.setAircraftType(null);
            }
        }

        if (movement.getAircraftType() != null && movement.getAircraftType().length() > 4) {
            movement.setAircraftType(movement.getAircraftType().substring(0, 4));
        }

        movement = Persistence.create(connx, movement);

        nextPilot.setMovementId(movement.getId());

        BM.stop();
    }

    private static void _landing(Pilot nextPilot, int landingReportId, String landingIcao) throws SQLException {
        BM.start();

        logDetails(nextPilot, "landing " + landingIcao);

        Movement movement = Persistence.load(connx, Movement.class, nextPilot.getMovementId());
        if (movement == null || !(movement.getState() == Movement.State.InProgress || movement.getState() == Movement.State.Disconnected)) {
            throw new IllegalStateException();
        }

        movement.setState(Movement.State.Done);
        movement.setStateReportId(landingReportId);

        movement.setArrReportId(landingReportId);
        movement.setArrIcao(landingIcao);

        updateMovement(movement);

        nextPilot.setMovementId(0);

        BM.stop();
    }

    private static void _movementStillInProgress(int pilotId, int reportId) throws SQLException {
        BM.start();

        Pilot pilot = Persistence.load(connx, Pilot.class, pilotId);
        Movement movement = Persistence.load(connx, Movement.class, pilot.getMovementId());
        if (movement == null || movement.getState() != Movement.State.Disconnected) {
            throw new IllegalStateException();
        }

        movement.setState(Movement.State.InProgress);
        movement.setStateReportId(reportId);
        updateMovement(movement);

        BM.stop();
    }


    private static void resetData() throws SQLException {
        Connection connx = DB.getConnection();
        Statement st = connx.createStatement();
        st.executeUpdate("update report set processed = 'N'");
        st.executeUpdate("truncate table pilot cascade");
        st.executeUpdate("truncate table movement cascade");
        connx.commit();
        connx.close();
    }
}