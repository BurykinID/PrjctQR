package com.qr.app.backend.entity;

import lombok.*;

import javax.persistence.*;

@Entity(name = "mark")
@Getter
@Setter
@Data
public class Mark {

    @Id
    @NonNull
    @Column(unique = true)
    private String cis;
    @NonNull
    private String barcode;
    private String numberBox;
    private String numberOrder;
    private long date;

    public Mark() {
        cis = "";
        barcode = "";
        numberBox = "";
        numberOrder = "";
        date = 0;
    }

    public Mark(String cis, String barcode, String numberBox, String numberOrder, long date) {
        this.cis = cis;
        this.barcode = barcode;
        this.numberBox = numberBox;
        this.numberOrder = numberOrder;
        this.date = date;
    }

    public void updateMark(String barcode, String numberBox, String numberOrder, long date) {
        this.barcode = barcode;
        this.numberBox = numberBox;
        this.numberOrder = numberOrder;
        this.date = date;
    }


}
