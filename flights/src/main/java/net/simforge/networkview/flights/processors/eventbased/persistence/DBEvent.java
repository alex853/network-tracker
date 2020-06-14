package net.simforge.networkview.flights.processors.eventbased.persistence;

import net.simforge.commons.hibernate.Auditable;
import net.simforge.commons.hibernate.BaseEntity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "Event")
@Table(name = "flt_event")
public class DBEvent implements BaseEntity, Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pk_flt_event_id")
    @SequenceGenerator(name = "pk_flt_event_id", sequenceName = "flt_event_id_seq", allocationSize = 1)
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

    private Integer type;

    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "dt")
    private LocalDateTime dt;

    @ManyToOne
    @JoinColumn(name = "flight_id")
    private DBFlight flight;

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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public LocalDateTime getDt() {
        return dt;
    }

    public void setDt(LocalDateTime dt) {
        this.dt = dt;
    }

    public DBFlight getFlight() {
        return flight;
    }

    public void setFlight(DBFlight flight) {
        this.flight = flight;
    }
}
