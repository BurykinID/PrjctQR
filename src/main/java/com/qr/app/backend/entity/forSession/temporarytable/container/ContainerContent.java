package com.qr.app.backend.entity.forSession.temporarytable.container;

import com.qr.app.backend.entity.AbstractEntity;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;

@Entity
@Getter
@Setter
@Data
@NoArgsConstructor
public class ContainerContent extends AbstractEntity {

    private String numberVariantBox;
    private int countNow;
    private int countNeed;
    private String macAddress;
    private String numberContainer;

}
