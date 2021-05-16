package com.qr.app.backend.entity.forSession;

import com.qr.app.backend.entity.AbstractEntity;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Getter
@Setter
@Data
@NoArgsConstructor
public class LogSession extends AbstractEntity {

    @Temporal(TemporalType.TIMESTAMP)
    private Date timeEvent;
    private String barcode;
    private String phase;
    private LvlEvent lvlEvent;
    private String macAddress;

    public LogSession(Date timeEvent,
                      String barcode,
                      String phase,
                      LvlEvent lvlEvent,
                      String macAddress) {
        this.timeEvent = timeEvent;
        this.barcode = barcode;
        this.phase = phase;
        this.lvlEvent = lvlEvent;
        this.macAddress = macAddress;
    }
    
}
