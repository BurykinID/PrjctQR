package com.qr.app.backend.entity;

import com.qr.app.backend.Json.container.HierarchyOfBoxesJson;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    public HierarchyOfBoxes (String numberContainer, String numberBox, String time) throws ParseException {
        this.numberContainer = numberContainer;
        this.numberBox = numberBox;
        Date newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
        this.date = newDate.getTime();
    }

    public HierarchyOfBoxes() {
        this.numberContainer = "";
        this.numberBox = "";
        this.date = 0;
    }

    public void update(HierarchyOfBoxesJson json) throws ParseException {
        this.numberBox = json.getNumberBox();
        this.numberContainer = json.getNumberContainer();
        Date newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(json.getDate());
        this.date = newDate.getTime();
    }

}
