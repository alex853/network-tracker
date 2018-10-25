package net.simforge.fr24.model1;

import net.simforge.commons.persistence.BaseEntity;
import net.simforge.commons.persistence.Column;
import net.simforge.commons.persistence.Table;
import org.joda.time.DateTime;

@Table(name = "fr24_flight")
public class Flight extends BaseEntity {

    @Column
    private DateTime dof;

    @Column
    private String flightNumber;

    @Column
    private String originIata;

    @Column
    private String destinationIata;

    @Column
    private String type;

    @Column
    private String regNumber;

    @Column
    private String callsign;

    public DateTime getDof() {
        return dof;
    }

    public void setDof(DateTime dof) {
        this.dof = dof;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getOriginIata() {
        return originIata;
    }

    public void setOriginIata(String originIata) {
        this.originIata = originIata;
    }

    public String getDestinationIata() {
        return destinationIata;
    }

    public void setDestinationIata(String destinationIata) {
        this.destinationIata = destinationIata;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRegNumber() {
        return regNumber;
    }

    public void setRegNumber(String regNumber) {
        this.regNumber = regNumber;
    }

    public String getCallsign() {
        return callsign;
    }

    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }

    @Override
    public String toString() {
        return String.format("Flight {DOF: %s, flightNumber: %s, originIata: %s, destinationIata: %s, " +
                "type: %s, regNumber: %s, callsign: %s}",
                dof,
                flightNumber,
                originIata,
                destinationIata,
                type,
                regNumber,
                callsign);
    }
}
