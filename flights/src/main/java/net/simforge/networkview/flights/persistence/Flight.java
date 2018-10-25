package flights.persistence;

import net.simforge.commons.hibernate.Auditable;
import net.simforge.commons.hibernate.BaseEntity;
import net.simforge.tracker.Network;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "flight")
public class Flight implements BaseEntity, Auditable/*, EventLog.Loggable*/ {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pk_flight_id")
    @SequenceGenerator(name = "pk_flight_id", sequenceName = "flight_id_seq", allocationSize = 1)
    private Integer id;
    @Version
    private Integer version;

    @SuppressWarnings("unused")
    @Column(name = "create_dt")
    private LocalDateTime createDt;
    @SuppressWarnings("unused")
    @Column(name = "modify_dt")
    private LocalDateTime modifyDt;

    private Integer network;
    @Column(name = "pilot_number")
    private Integer pilotNumber;

    private String callsign;
    @Column(name = "aircraft_type")
    private String aircraftType;
    @Column(name = "reg_no")
    private String regNo;
    @Column(name = "planned_origin")
    private String plannedOrigin;
    @Column(name = "planned_destination")
    private String plannedDestination;

    private Integer status;

    @Column(name = "first_seen_report_id")
    private Integer firstSeenReportId;
    @Column(name = "first_seen_dt")
    private LocalDateTime firstSeenDt;

    @Column(name = "last_seen_report_id")
    private Integer lastSeenReportId;
    @Column(name = "last_seen_dt")
    private LocalDateTime lastSeenDt;

    @Column(name = "departure_report_id")
    private Integer departureReportId;
    @Column(name = "departure_dt")
    private LocalDateTime departureDt;
    @Column(name = "departure_latitude")
    private Double departureLatitude;
    @Column(name = "departure_longitude")
    private Double departureLongitude;
    @Column(name = "origin_type")
    private Integer originType; // ICAO, InAir, Other
    @Column(name = "origin_icao")
    private String originIcao;

    @Column(name = "arrival_report_id")
    private Integer arrivalReportId;
    @Column(name = "arrival_dt")
    private LocalDateTime arrivalDt;
    @Column(name = "arrival_latitude")
    private Double arrivalLatitude;
    @Column(name = "arrival_longitude")
    private Double arrivalLongitude;
    @Column(name = "destination_type")
    private Integer destinationType; // ICAO, InAir, Other
    @Column(name = "destination_icao")
    private String destinationIcao;

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

    public Network getNetwork() {
        switch (network) { // todo AK
            case 1: return Network.VATSIM;
            case 2: return Network.IVAO;
            default: return null;
        }
    }

    public void setNetwork(Network network) {
        if (network != null) {
            this.network = network.getCode();
        } else {
            this.network = null;
        }
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

    public String getPlannedOrigin() {
        return plannedOrigin;
    }

    public void setPlannedOrigin(String plannedOrigin) {
        this.plannedOrigin = plannedOrigin;
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

    public Integer getFirstSeenReportId() {
        return firstSeenReportId;
    }

    public void setFirstSeenReportId(Integer firstSeenReportId) {
        this.firstSeenReportId = firstSeenReportId;
    }

    public LocalDateTime getFirstSeenDt() {
        return firstSeenDt;
    }

    public void setFirstSeenDt(LocalDateTime firstSeenDt) {
        this.firstSeenDt = firstSeenDt;
    }

    public Integer getLastSeenReportId() {
        return lastSeenReportId;
    }

    public void setLastSeenReportId(Integer lastSeenReportId) {
        this.lastSeenReportId = lastSeenReportId;
    }

    public LocalDateTime getLastSeenDt() {
        return lastSeenDt;
    }

    public void setLastSeenDt(LocalDateTime lastSeenDt) {
        this.lastSeenDt = lastSeenDt;
    }

    public Integer getDepartureReportId() {
        return departureReportId;
    }

    public void setDepartureReportId(Integer departureReportId) {
        this.departureReportId = departureReportId;
    }

    public LocalDateTime getDepartureDt() {
        return departureDt;
    }

    public void setDepartureDt(LocalDateTime departureDt) {
        this.departureDt = departureDt;
    }

    public Double getDepartureLatitude() {
        return departureLatitude;
    }

    public void setDepartureLatitude(Double departureLatitude) {
        this.departureLatitude = departureLatitude;
    }

    public Double getDepartureLongitude() {
        return departureLongitude;
    }

    public void setDepartureLongitude(Double departureLongitude) {
        this.departureLongitude = departureLongitude;
    }

    public Integer getOriginType() {
        return originType;
    }

    public void setOriginType(Integer originType) {
        this.originType = originType;
    }

    public String getOriginIcao() {
        return originIcao;
    }

    public void setOriginIcao(String originIcao) {
        this.originIcao = originIcao;
    }

    public Integer getArrivalReportId() {
        return arrivalReportId;
    }

    public void setArrivalReportId(Integer arrivalReportId) {
        this.arrivalReportId = arrivalReportId;
    }

    public LocalDateTime getArrivalDt() {
        return arrivalDt;
    }

    public void setArrivalDt(LocalDateTime arrivalDt) {
        this.arrivalDt = arrivalDt;
    }

    public Double getArrivalLatitude() {
        return arrivalLatitude;
    }

    public void setArrivalLatitude(Double arrivalLatitude) {
        this.arrivalLatitude = arrivalLatitude;
    }

    public Double getArrivalLongitude() {
        return arrivalLongitude;
    }

    public void setArrivalLongitude(Double arrivalLongitude) {
        this.arrivalLongitude = arrivalLongitude;
    }

    public Integer getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(Integer destinationType) {
        this.destinationType = destinationType;
    }

    public String getDestinationIcao() {
        return destinationIcao;
    }

    public void setDestinationIcao(String destinationIcao) {
        this.destinationIcao = destinationIcao;
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
