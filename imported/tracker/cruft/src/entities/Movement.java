package entities;

import net.simforge.commons.persistence.BaseEntity;
import net.simforge.commons.persistence.Column;
import net.simforge.commons.persistence.Table;
import net.simforge.commons.persistence.SetNull;
import core.MovementExtra;

@Table(name = "movement")
public class Movement extends BaseEntity {
    @Column
    private int pilotId;

    @Column
    private int intOrder;

    @Column
    private String callsign;

    @Column
    private State state;

    @Column
    @SetNull
    private int stateReportId;

    @Column
    @SetNull
    private int depReportId;

    @Column
    private String depIcao;

    @Column
    private String plannedDepIcao;

    @Column
    @SetNull
    private int arrReportId;

    @Column
    private String arrIcao;

    @Column
    private String plannedArrIcao;

    @Column
    private double flownDistance; 

    @Column
    private String aircraftType;

    @Column
    private String aircraftRegNo;

    @Column
    private int aircraftId;

    private MovementExtra extra;

    public enum State {
        InProgress,
        Done,
        Disconnected,
        Terminated,
        Jump
    }

    public int getPilotId() {
        return pilotId;
    }

    public void setPilotId(int pilotId) {
        this.pilotId = pilotId;
    }

    public int getIntOrder() {
        return intOrder;
    }

    public void setIntOrder(int intOrder) {
        this.intOrder = intOrder;
    }

    public String getCallsign() {
        return callsign;
    }

    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public int getStateReportId() {
        return stateReportId;
    }

    public void setStateReportId(int stateReportId) {
        this.stateReportId = stateReportId;
    }

    public int getDepReportId() {
        return depReportId;
    }

    public void setDepReportId(int depReportId) {
        this.depReportId = depReportId;
    }

    public String getDepIcao() {
        return depIcao;
    }

    public void setDepIcao(String depIcao) {
        this.depIcao = depIcao;
    }

    public String getPlannedDepIcao() {
        return plannedDepIcao;
    }

    public void setPlannedDepIcao(String plannedDepIcao) {
        this.plannedDepIcao = plannedDepIcao;
    }

    public int getArrReportId() {
        return arrReportId;
    }

    public void setArrReportId(int arrReportId) {
        this.arrReportId = arrReportId;
    }

    public String getArrIcao() {
        return arrIcao;
    }

    public void setArrIcao(String arrIcao) {
        this.arrIcao = arrIcao;
    }

    public String getPlannedArrIcao() {
        return plannedArrIcao;
    }

    public void setPlannedArrIcao(String plannedArrIcao) {
        this.plannedArrIcao = plannedArrIcao;
    }

    public double getFlownDistance() {
        return flownDistance;
    }

    public void setFlownDistance(double flownDistance) {
        this.flownDistance = flownDistance;
    }

    public String getAircraftType() {
        return aircraftType;
    }

    public void setAircraftType(String aircraftType) {
        this.aircraftType = aircraftType;
    }

    public String getAircraftRegNo() {
        return aircraftRegNo;
    }

    public void setAircraftRegNo(String aircraftRegNo) {
        this.aircraftRegNo = aircraftRegNo;
    }

    public int getAircraftId() {
        return aircraftId;
    }

    public void setAircraftId(int aircraftId) {
        this.aircraftId = aircraftId;
    }

    public MovementExtra getExtra() {
        return extra;
    }

    public void setExtra(MovementExtra extra) {
        this.extra = extra;
    }
}
