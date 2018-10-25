package entities;

import net.simforge.commons.persistence.BaseEntity;
import net.simforge.commons.persistence.Table;
import net.simforge.commons.persistence.Column;
import net.simforge.commons.persistence.SetNull;
import core.PilotPosition;

@Table(name = "pilot")
public class Pilot extends BaseEntity implements Cloneable {
    @Column
    private int pilotNumber; // vatsim or ivao id

    @Column
    @SetNull
    private int reportId;

    @Column
    private String report;

    @Column
    private State state;

    @Column
    private String icao;

    @Column
    @SetNull
    private int movementId;

    public int getPilotNumber() {
        return pilotNumber;
    }

    public void setPilotNumber(int pilotNumber) {
        this.pilotNumber = pilotNumber;
    }

    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getIcao() {
        return icao;
    }

    public void setIcao(String icao) {
        this.icao = icao;
    }

    public Pilot clone() {
        try {
            return (Pilot) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public int getMovementId() {
        return movementId;
    }

    public void setMovementId(int movementId) {
        this.movementId = movementId;
    }

    public boolean hasActiveMovement() {
        return movementId != 0;
    }

    public static enum State {
        Offline,
        InAirport,
        Flying;

        public static State get(ReportPilotPosition position) {
            if (position == null) {
                return Offline;
            }
            PilotPosition pp = new PilotPosition(position);
            return pp.isInNearestAirport() ? InAirport : Flying;
        }
    }
}
