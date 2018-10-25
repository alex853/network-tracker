package net.simforge.tracker.webapp.dto;

import net.simforge.commons.misc.Geo;

public class PilotPositionDto {
    private int pilotNumber;
    private String callsign;
    private String status;
    private double latitude;
    private double longitude;
    private int heading;
    private int groundspeed;
    private String altitude;
    private String type;
    private String regNo;
    private String fpOrigin;
    private Geo.Coords fpOriginCoords;
    private String fpDestination;
    private Geo.Coords fpDestinationCoords;

    public int getPilotNumber() {
        return pilotNumber;
    }

    public void setPilotNumber(int pilotNumber) {
        this.pilotNumber = pilotNumber;
    }

    public String getCallsign() {
        return callsign;
    }

    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public int getHeading() {
        return heading;
    }

    public void setHeading(int heading) {
        this.heading = heading;
    }

    public int getGroundspeed() {
        return groundspeed;
    }

    public void setGroundspeed(int groundspeed) {
        this.groundspeed = groundspeed;
    }

    public String getAltitude() {
        return altitude;
    }

    public void setAltitude(String altitude) {
        this.altitude = altitude;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getFpOrigin() {
        return fpOrigin;
    }

    public void setFpOrigin(String fpOrigin) {
        this.fpOrigin = fpOrigin;
    }

    public Geo.Coords getFpOriginCoords() {
        return fpOriginCoords;
    }

    public void setFpOriginCoords(Geo.Coords fpOriginCoords) {
        this.fpOriginCoords = fpOriginCoords;
    }

    public String getFpDestination() {
        return fpDestination;
    }

    public void setFpDestination(String fpDestination) {
        this.fpDestination = fpDestination;
    }

    public Geo.Coords getFpDestinationCoords() {
        return fpDestinationCoords;
    }

    public void setFpDestinationCoords(Geo.Coords fpDestinationCoords) {
        this.fpDestinationCoords = fpDestinationCoords;
    }
}
