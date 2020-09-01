package com.qr.app.backend.entity;

import lombok.*;

import javax.persistence.*;

@Entity(name = "mark")
@Getter
@Setter
@Data
@NoArgsConstructor
public class Mark extends AbstractEntity {

    @NonNull
    @Column(unique = true)
    private String cis;
    @NonNull
    private String barcode;
    private String numberBox;
    private String numberOrder;
    private long date;

}
