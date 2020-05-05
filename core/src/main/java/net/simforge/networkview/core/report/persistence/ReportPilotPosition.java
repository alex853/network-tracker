package net.simforge.networkview.core.report.persistence;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "report_pilot_position")
public class ReportPilotPosition implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pk_report_pilot_position_id")
    @SequenceGenerator(name = "pk_report_pilot_position_id", sequenceName = "report_pilot_position_id_seq", allocationSize = 1)
    private Long id;
    @Version
    private Integer version;

    @ManyToOne
    @JoinColumn(name = "report_id")
    private Report report;
    @Column(name = "pilot_number")
    private Integer pilotNumber;
    private String callsign;
    private Double latitude;
    private Double longitude;
    private Integer altitude;
    private Integer groundspeed;
    private Integer heading;
    @Column(name = "qnh_mb")
    private Integer qnhMb;
    @Column(name = "on_ground")
    private Boolean onGround;
    @Column(name = "fp_aircraft")
    private String fpAircraft;
    @Column(name = "fp_origin")
    private String fpOrigin;
    @Column(name = "fp_destination")
    private String fpDestination;
    @ManyToOne
    @JoinColumn(name = "fp_remarks_id")
    private ReportPilotFpRemarks fpRemarks;
    @Column(name = "parsed_reg_no")
    private String parsedRegNo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
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

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getAltitude() {
        return altitude;
    }

    public void setAltitude(Integer altitude) {
        this.altitude = altitude;
    }

    public Integer getGroundspeed() {
        return groundspeed;
    }

    public void setGroundspeed(Integer groundspeed) {
        this.groundspeed = groundspeed;
    }

    public Integer getHeading() {
        return heading;
    }

    public void setHeading(Integer heading) {
        this.heading = heading;
    }

    public Integer getQnhMb() {
        return qnhMb;
    }

    public void setQnhMb(Integer qnhMb) {
        this.qnhMb = qnhMb;
    }

    public Boolean getOnGround() {
        return onGround;
    }

    public void setOnGround(Boolean onGround) {
        this.onGround = onGround;
    }

    public String getFpAircraft() {
        return fpAircraft;
    }

    public void setFpAircraft(String fpAircraft) {
        this.fpAircraft = fpAircraft;
    }

    public String getFpOrigin() {
        return fpOrigin;
    }

    public void setFpOrigin(String fpOrigin) {
        this.fpOrigin = fpOrigin;
    }

    public String getFpDestination() {
        return fpDestination;
    }

    public void setFpDestination(String fpDestination) {
        this.fpDestination = fpDestination;
    }

    public ReportPilotFpRemarks getFpRemarks() {
        return fpRemarks;
    }

    public void setFpRemarks(ReportPilotFpRemarks fpRemarks) {
        this.fpRemarks = fpRemarks;
    }

    public String getParsedRegNo() {
        return parsedRegNo;
    }

    public void setParsedRegNo(String parsedRegNo) {
        this.parsedRegNo = parsedRegNo;
    }
}
