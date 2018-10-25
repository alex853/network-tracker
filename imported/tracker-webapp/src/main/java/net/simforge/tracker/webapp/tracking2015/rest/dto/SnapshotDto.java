package net.simforge.tracker.webapp.tracking2015.rest.dto;

public class SnapshotDto {
    private String filename;

    public SnapshotDto(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }
}
