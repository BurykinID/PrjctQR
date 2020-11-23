package com.qr.app.backend.entity.forSession.temporarytable.box;

import com.qr.app.backend.entity.AbstractEntity;
import lombok.*;

import javax.persistence.Entity;

@Entity
@Getter
@Setter
@Data
@NoArgsConstructor
public class BoxContent extends AbstractEntity {

    private String barcode;
    private String article;
    private String size;
    private String color;
    private int countNow;
    private int countNeed;
    private String macAddress;
    private String numberBox;

}
