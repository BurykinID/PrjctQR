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
public class ContainerBox extends AbstractEntity {

    private String numberBox;
    private String numberContainer;
    private String macAddress;

    public ContainerBox() {
        this.numberBox = "";
        this.numberContainer = "";
        this.macAddress = "";
    }

}
