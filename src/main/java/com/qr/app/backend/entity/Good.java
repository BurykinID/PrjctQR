package com.qr.app.backend.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Setter
@Getter
@Data
@NoArgsConstructor
public class Good extends AbstractEntity {

    @Column(unique = true)
    private String barcode;
    private String name;
    private String article;
    private String color;
    private String size;

}
