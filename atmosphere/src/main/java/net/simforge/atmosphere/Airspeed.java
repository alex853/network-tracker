package net.simforge.atmosphere;

public class Airspeed {
    public static int iasToTas(int ias, int altitude) {
        return (int) (ias * .02 * altitude / 1000) + ias;
    }

    public static int tasToIas(int tas, int altitude) {
        return (int) (tas / (.02 * altitude / 1000 + 1));
    }
}
