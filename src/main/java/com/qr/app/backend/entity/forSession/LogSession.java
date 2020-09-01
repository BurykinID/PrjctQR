package com.qr.app.backend.entity.forSession;

import com.qr.app.backend.entity.AbstractEntity;
import lombok.*;

import javax.persistence.Entity;

@Entity
@Getter
@Setter
@Data
@NoArgsConstructor
public class LogSession extends AbstractEntity {

    private long timeEvent;
    private String barcode;
    private String phase;
    private LvlEvent lvlEvent;
    private String macAddress;

    public LogSession(long timeEvent,
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
