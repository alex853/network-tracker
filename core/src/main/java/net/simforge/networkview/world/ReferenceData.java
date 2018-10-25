package net.simforge.networkview.world;

public class ReferenceData {
    public static double getMaxEndurance(String aircraftType) {
        // todo AK implement kind of database

        // 6.0 is constant value which definitely covers most part of fleet - A320/B737 aircraft
        // that usually have 4-5 hours max range with max payload
        // this constant will be replaced when the method will be implemented

        return 6.0;
    }
}
