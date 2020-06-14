package net.simforge.networkview.flights.processors.eventbased.persistence;

import net.simforge.commons.hibernate.Auditable;
import net.simforge.commons.hibernate.BaseEntity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "Flight")
@Table(name = "flt_flight")
public class DBFlight implements BaseEntity, Auditable/*, EventLog.Loggable*/ {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pk_flt_flight_id")
    @SequenceGenerator(name = "pk_flt_flight_id", sequenceName = "flt_flight_id_seq", allocationSize = 1)
    private Integer id;
    @Version
    private Integer version;

    @SuppressWarnings("unused")
    @Column(name = "create_dt")
    private LocalDateTime createDt;
    @SuppressWarnings("unused")
    @Column(name = "modify_dt")
    private LocalDateTime modifyDt;

    //    private Integer network;
    @Column(name = "pilot_number")
    private Integer pilotNumber;

    private String callsign;
    @Column(name = "aircraft_type")
    private String aircraftType;
    @Column(name = "reg_no")
    private String regNo;
    @Column(name = "planned_departure")
    private String plannedDeparture;
    @Column(name = "planned_destination")
    private String plannedDestination;

    private Integer status;

    @Column(name = "first_seen_report_id")
    private Long firstSeenReportId;
    @Column(name = "first_seen_dt")
    private LocalDateTime firstSeenDt;
    @Column(name = "first_seen_latitude")
    private Double firstSeenLatitude;
    @Column(name = "first_seen_longitude")
    private Double firstSeenLongitude;
    @Column(name = "first_seen_icao")
    private String firstSeenIcao;

    @Column(name = "last_seen_report_id")
    private Long lastSeenReportId;
    @Column(name = "last_seen_dt")
    private LocalDateTime lastSeenDt;
    @Column(name = "last_seen_latitude")
    private Double lastSeenLatitude;
    @Column(name = "last_seen_longitude")
    private Double lastSeenLongitude;
    @Column(name = "last_seen_icao")
    private String lastSeenIcao;

    @Column(name = "takeoff_report_id")
    private Long takeoffReportId;
    @Column(name = "takeoff_dt")
    private LocalDateTime takeoffDt;
    @Column(name = "takeoff_latitude")
    private Double takeoffLatitude;
    @Column(name = "takeoff_longitude")
    private Double takeoffLongitude;
    @Column(name = "takeoff_icao")
    private String takeoffIcao;

    @Column(name = "landing_report_id")
    private Long landingReportId;
    @Column(name = "landing_dt")
    private LocalDateTime landingDt;
    @Column(name = "landing_latitude")
    private Double landingLatitude;
    @Column(name = "landing_longitude")
    private Double landingLongitude;
    @Column(name = "landing_icao")
    private String landingIcao;

    @Column(name = "distance_flown")
    private Double distanceFlown;
    @Column(name = "flight_time")
    private Double flightTime; // 1.0 means 1.0 hour

//    @Override
//    public String getEventLogCode() {
//        return "flight";
//    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public Integer getVersion() {
        return version;
    }

    @Override
    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public LocalDateTime getCreateDt() {
        return createDt;
    }

    @Override
    public LocalDateTime getModifyDt() {
        return modifyDt;
    }

    public Integer getPilotNumber() {
        return pilotNumber;
    }

    public void setPilotNumber(Integer pilotNumber) {
        this.pilotNumber = pilotNumber;
    }

    public String getCallsign() {
        return callsign;
    }

    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getAircraftType() {
        return aircraftType;
    }

    public void setAircraftType(String aircraftType) {
        this.aircraftType = aircraftType;
    }

    public String getPlannedDeparture() {
        return plannedDeparture;
    }

    public void setPlannedDeparture(String plannedDeparture) {
        this.plannedDeparture = plannedDeparture;
    }

    public String getPlannedDestination() {
        return plannedDestination;
    }

    public void setPlannedDestination(String plannedDestination) {
        this.plannedDestination = plannedDestination;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getFirstSeenReportId() {
        return firstSeenReportId;
    }

    public void setFirstSeenReportId(Long firstSeenReportId) {
        this.firstSeenReportId = firstSeenReportId;
    }

    public LocalDateTime getFirstSeenDt() {
        return firstSeenDt;
    }

    public void setFirstSeenDt(LocalDateTime firstSeenDt) {
        this.firstSeenDt = firstSeenDt;
    }

    public Double getFirstSeenLatitude() {
        return firstSeenLatitude;
    }

    public void setFirstSeenLatitude(Double firstSeenLatitude) {
        this.firstSeenLatitude = firstSeenLatitude;
    }

    public Double getFirstSeenLongitude() {
        return firstSeenLongitude;
    }

    public void setFirstSeenLongitude(Double firstSeenLongitude) {
        this.firstSeenLongitude = firstSeenLongitude;
    }

    public String getFirstSeenIcao() {
        return firstSeenIcao;
    }

    public void setFirstSeenIcao(String firstSeenIcao) {
        this.firstSeenIcao = firstSeenIcao;
    }

    public Long getLastSeenReportId() {
        return lastSeenReportId;
    }

    public void setLastSeenReportId(Long lastSeenReportId) {
        this.lastSeenReportId = lastSeenReportId;
    }

    public LocalDateTime getLastSeenDt() {
        return lastSeenDt;
    }

    public void setLastSeenDt(LocalDateTime lastSeenDt) {
        this.lastSeenDt = lastSeenDt;
    }

    public Double getLastSeenLatitude() {
        return lastSeenLatitude;
    }

    public void setLastSeenLatitude(Double lastSeenLatitude) {
        this.lastSeenLatitude = lastSeenLatitude;
    }

    public Double getLastSeenLongitude() {
        return lastSeenLongitude;
    }

    public void setLastSeenLongitude(Double lastSeenLongitude) {
        this.lastSeenLongitude = lastSeenLongitude;
    }

    public String getLastSeenIcao() {
        return lastSeenIcao;
    }

    public void setLastSeenIcao(String lastSeenIcao) {
        this.lastSeenIcao = lastSeenIcao;
    }

    public Long getTakeoffReportId() {
        return takeoffReportId;
    }

    public void setTakeoffReportId(Long takeoffReportId) {
        this.takeoffReportId = takeoffReportId;
    }

    public LocalDateTime getTakeoffDt() {
        return takeoffDt;
    }

    public void setTakeoffDt(LocalDateTime takeoffDt) {
        this.takeoffDt = takeoffDt;
    }

    public Double getTakeoffLatitude() {
        return takeoffLatitude;
    }

    public void setTakeoffLatitude(Double takeoffLatitude) {
        this.takeoffLatitude = takeoffLatitude;
    }

    public Double getTakeoffLongitude() {
        return takeoffLongitude;
    }

    public void setTakeoffLongitude(Double takeoffLongitude) {
        this.takeoffLongitude = takeoffLongitude;
    }

    public String getTakeoffIcao() {
        return takeoffIcao;
    }

    public void setTakeoffIcao(String originIcao) {
        this.takeoffIcao = originIcao;
    }

    public Long getLandingReportId() {
        return landingReportId;
    }

    public void setLandingReportId(Long landingReportId) {
        this.landingReportId = landingReportId;
    }

    public LocalDateTime getLandingDt() {
        return landingDt;
    }

    public void setLandingDt(LocalDateTime landingDt) {
        this.landingDt = landingDt;
    }

    public Double getLandingLatitude() {
        return landingLatitude;
    }

    public void setLandingLatitude(Double landingLatitude) {
        this.landingLatitude = landingLatitude;
    }

    public Double getLandingLongitude() {
        return landingLongitude;
    }

    public void setLandingLongitude(Double landingLongitude) {
        this.landingLongitude = landingLongitude;
    }

//    public Integer getLandingType() {
//        return landingType;
//    }

//    public void setLandingType(Integer landingType) {
//        this.landingType = landingType;
//    }

    public String getLandingIcao() {
        return landingIcao;
    }

    public void setLandingIcao(String landingIcao) {
        this.landingIcao = landingIcao;
    }

    public Double getDistanceFlown() {
        return distanceFlown;
    }

    public void setDistanceFlown(Double distanceFlown) {
        this.distanceFlown = distanceFlown;
    }

    public Double getFlightTime() {
        return flightTime;
    }

    public void setFlightTime(Double flightTime) {
        this.flightTime = flightTime;
    }
}
