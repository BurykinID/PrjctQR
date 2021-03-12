package com.qr.app.backend.entity.forSession.temporarytable.box;

import com.qr.app.backend.entity.AbstractEntity;
import lombok.*;

import javax.persistence.Entity;

@Entity
@Getter
@Setter
@Data
public class BoxMark extends AbstractEntity {

    private String cis;
    private String numberBox;
    private String macAddress;

    public BoxMark() {
        this.cis = "";
        this.numberBox = "";
        this.macAddress = "";
    }

}
