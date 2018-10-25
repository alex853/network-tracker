package entities;

import net.simforge.commons.persistence.Column;
import net.simforge.commons.persistence.BaseEntity;
import net.simforge.commons.persistence.Table;
import net.simforge.commons.persistence.Cut;
import org.joda.time.DateTime;

@Table(name = "tracking_event")
public class TrackingEvent extends BaseEntity {
    @Column
    private DateTime dt;

    @Column
    private int pilotNumber;

    @Column
    private EventType eventType;

    @Column
    @Cut(size = 10)
    private String callsign;

    @Column
    private PositionState positionState;

    @Column
    @Cut(size = 4)
    private String positionIcao;

    @Column
    private double latitude;

    @Column
    private double longitude;

    @Column
    private int altitudeMsl;

    @Column
    private int groundspeed;

    @Column
    private int heading;

    @Column
    @Cut(size = 20)
    private String fpAircraft;

    @Column
    @Cut(size = 4)
    private String fpDep;

    @Column
    @Cut(size = 4)
    private String fpDest;

    @Column
    @Cut(size = 10)
    private String parsedRegNo;

    public enum EventType {
        Online,
        Offline,
        Takeoff,
        Landing,
        PosRep
    }

    public enum PositionState {
        InAirport,
        Flying
    }

    public DateTime getDt() {
        return dt;
    }

    public void setDt(DateTime dt) {
        this.dt = dt;
    }

    public int getPilotNumber() {
        return pilotNumber;
    }

    public void setPilotNumber(int pilotNumber) {
        this.pilotNumber = pilotNumber;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getCallsign() {
        return callsign;
    }

    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }

    public PositionState getPositionState() {
        return positionState;
    }

    public void setPositionState(PositionState positionState) {
        this.positionState = positionState;
    }

    public String getPositionIcao() {
        return positionIcao;
    }

    public void setPositionIcao(String positionIcao) {
        this.positionIcao = positionIcao;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getAltitudeMsl() {
        return altitudeMsl;
    }

    public void setAltitudeMsl(int altitudeMsl) {
        this.altitudeMsl = altitudeMsl;
    }

    public int getGroundspeed() {
        return groundspeed;
    }

    public void setGroundspeed(int groundspeed) {
        this.groundspeed = groundspeed;
    }

    public int getHeading() {
        return heading;
    }

    public void setHeading(int heading) {
        this.heading = heading;
    }

    public String getFpAircraft() {
        return fpAircraft;
    }

    public void setFpAircraft(String fpAircraft) {
        this.fpAircraft = fpAircraft;
    }

    public String getFpDep() {
        return fpDep;
    }

    public void setFpDep(String fpDep) {
        this.fpDep = fpDep;
    }

    public String getFpDest() {
        return fpDest;
    }

    public void setFpDest(String fpDest) {
        this.fpDest = fpDest;
    }

    public String getParsedRegNo() {
        return parsedRegNo;
    }

    public void setParsedRegNo(String parsedRegNo) {
        this.parsedRegNo = parsedRegNo;
    }
}
