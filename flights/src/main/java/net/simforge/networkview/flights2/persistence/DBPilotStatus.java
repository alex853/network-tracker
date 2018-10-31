package net.simforge.networkview.flights2.persistence;

public class DBPilotStatus {
    private int pilotNumber;
    private String currReport;
    private DBFlight currFlight;

    public int getPilotNumber() {
        return pilotNumber;
    }

    public void setPilotNumber(int pilotNumber) {
        this.pilotNumber = pilotNumber;
    }
}
