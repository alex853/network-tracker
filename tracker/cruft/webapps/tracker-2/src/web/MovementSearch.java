package web;

import entities.Movement;

import java.sql.Connection;
import java.sql.SQLException;

import forge.commons.db.DB;
import net.simforge.commons.persistence.Persistence;

public class MovementSearch {
    private Movement previous;
    private Movement current;
    private Movement next;

    public MovementSearch(Movement previous, Movement current, Movement next) {
        this.previous = previous;
        this.current = current;
        this.next = next;
    }

    public static MovementSearch search(int pilotId, int reportId) throws SQLException {
        Connection connx = DB.getConnection();
        Movement current = Persistence.loadSingleWhere(
                connx,
                Movement.class,
                String.format("pilot_id = %s and state in (%s) and dep_report_id <= %s and %s <= arr_report_id",
                        String.valueOf(pilotId),
                        Movement.State.Done.ordinal() + ", " + Movement.State.Disconnected.ordinal() + ", " + Movement.State.Terminated.ordinal(),
                        String.valueOf(reportId),
                        String.valueOf(reportId))
                );

        if (current == null) {
            current = Persistence.loadSingleWhere(
                    connx,
                    Movement.class,
                    String.format("pilot_id = %s and state = %s and dep_report_id <= %s and arr_report_id is null",
                            String.valueOf(pilotId),
                            Movement.State.InProgress.ordinal(),
                            String.valueOf(reportId))
            );
        }

        Movement previous = Persistence.loadSingleWhere(
                connx,
                Movement.class,
                String.format("pilot_id = %s and state in (%s) and arr_report_id < %s order by id desc limit 1",
                        String.valueOf(pilotId),
                        Movement.State.Done.ordinal() + ", " + Movement.State.Disconnected.ordinal() + ", " + Movement.State.Terminated.ordinal(),
                        String.valueOf(current != null ? current.getDepReportId() : reportId))
                );

        Movement next = null;
        if (!(current != null && Movement.State.InProgress.equals(current.getState()))) {
            next = Persistence.loadSingleWhere(
                    connx,
                    Movement.class,
                    String.format("pilot_id = %s and state in (%s) and %s < dep_report_id order by id asc limit 1",
                            String.valueOf(pilotId),
                            Movement.State.Done.ordinal() + ", " + Movement.State.Disconnected.ordinal() + ", " + Movement.State.Terminated.ordinal() + ", " + Movement.State.InProgress.ordinal(),
                            String.valueOf(current != null ? current.getArrReportId() : reportId))
                    );
        }

        connx.close();

        return new MovementSearch(previous, current, next);
    }

    public Movement getCurrent() {
        return current;
    }

    public boolean isActual(int reportId) {
        if (current != null) {
            if (Movement.State.InProgress.equals(current.getState())) {
                return current.getDepReportId() <= reportId;
            } else {
                return current.getDepReportId() <= reportId && reportId <= current.getArrReportId();
            }
        }
        boolean limitedByPrevious = previous != null && previous.getArrReportId() >= reportId;
        boolean limitedByNext = next != null && next.getDepReportId() <= reportId;
        return !(limitedByPrevious || limitedByNext);
    }

    public String toString() {
        return (previous != null ? previous.getId() : "-") + " / " +
                (current != null ? current.getId() : "-") + " / " +
                (next != null ? next.getId() : "-");
    }
}
