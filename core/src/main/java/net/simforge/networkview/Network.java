package net.simforge.networkview;

public enum Network {
    VATSIM(1),
    IVAO(2);

    private int code;

    private Network(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
