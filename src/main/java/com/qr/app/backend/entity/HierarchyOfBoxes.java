package com.qr.app.backend.entity;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;

@Entity
@Data
@Getter
@Setter
public class HierarchyOfBoxes extends AbstractEntity {

    private String numberContainer;
    private String numberBox;
    private long date;

    public HierarchyOfBoxes (String numberContainer, String numberBox, long time) {
        this.numberContainer = numberContainer;
        this.numberBox = numberBox;
        this.date = time;
    }

    public HierarchyOfBoxes() {
        this.numberContainer = "";
        this.numberBox = "";
        this.date = 0;
    }
}
