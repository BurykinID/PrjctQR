package com.qr.app.backend.entity.forSession;

import com.qr.app.backend.entity.AbstractEntity;
import lombok.*;

import javax.persistence.Entity;

@Entity
@Getter
@Setter
@Data
@NoArgsConstructor
public class BoxMark extends AbstractEntity {

    private String cis;
    private String numberBox;
    private String macAddress;

}
