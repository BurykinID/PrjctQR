package com.qr.app.backend.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Setter
@Getter
@Data
public class Good{

    @Id
    @Column(unique = true)
    private String barcode;
    private String name;
    private String article;
    private String color;
    private String size;

    public Good() {
        this.barcode = "";
        this.name = "";
        this.article = "";
        this.color = "";
        this.size = "";
    }

    public Good (String barcode, String name, String article, String color, String size) {
        this.barcode = barcode;
        this.name = name;
        this.article = article;
        this.color = color;
        this.size = size;
    }

    public void updateGood(String name, String article, String color, String size) {
        this.name = name;
        this.article = article;
        this.color = color;
        this.size = size;
    }

}
