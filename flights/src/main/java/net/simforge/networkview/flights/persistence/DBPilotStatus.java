package net.simforge.networkview.flights.persistence;

import net.simforge.commons.hibernate.Auditable;
import net.simforge.commons.hibernate.BaseEntity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "PilotStatus")
@Table(name = "flt_pilot_status")
public class DBPilotStatus implements BaseEntity, Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pk_flt_pilot_status_id")
    @SequenceGenerator(name = "pk_flt_pilot_status_id", sequenceName = "flt_pilot_status_id_seq", allocationSize = 1)
    private Integer id;
    @Version
    private Integer version;

    @SuppressWarnings("unused")
    @Column(name = "create_dt")
    private LocalDateTime createDt;
    @SuppressWarnings("unused")
    @Column(name = "modify_dt")
    private LocalDateTime modifyDt;

    @Column(name = "pilot_number")
    private Integer pilotNumber;

    @Column(name = "last_processed_report_id")
    private Long lastProcessedReportId;
    @Column(name = "last_processed_dt")
    private LocalDateTime lastProcessedDt;

    @ManyToOne
    @JoinColumn(name = "curr_flight_id")
    private DBFlight currFlight;

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

    public Long getLastProcessedReportId() {
        return lastProcessedReportId;
    }

    public void setLastProcessedReportId(Long lastProcessedReportId) {
        this.lastProcessedReportId = lastProcessedReportId;
    }

    public LocalDateTime getLastProcessedDt() {
        return lastProcessedDt;
    }

    public void setLastProcessedDt(LocalDateTime lastProcessedDt) {
        this.lastProcessedDt = lastProcessedDt;
    }

    public DBFlight getCurrFlight() {
        return currFlight;
    }

    public void setCurrFlight(DBFlight currFlight) {
        this.currFlight = currFlight;
    }
}
