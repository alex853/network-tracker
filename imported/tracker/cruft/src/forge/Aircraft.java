package forge;

import net.simforge.commons.persistence.BaseEntity;
import net.simforge.commons.persistence.Table;
import net.simforge.commons.persistence.Column;

@Table(name = "aircraft")
public class Aircraft extends BaseEntity {
    @Column
    private String type;

    @Column
    private String regNo;

    @Column
    private String icao;

    @Column
    private int movementId;

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

    public String getIcao() {
        return icao;
    }

    public void setIcao(String icao) {
        this.icao = icao;
    }

    public int getMovementId() {
        return movementId;
    }

    public void setMovementId(int movementId) {
        this.movementId = movementId;
    }
}
