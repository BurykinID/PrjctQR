package com.qr.app.backend.entity.forSession;

import com.qr.app.backend.entity.AbstractEntity;
import lombok.*;

import javax.persistence.Entity;

@Entity
@Getter
@Setter
@Data
@NoArgsConstructor
public class MarkView extends AbstractEntity {

    private String barcode;
    private String article;
    private String size;
    private String color;
    private int countNow;
    private int countNeed;
    private String macAddress;

}
