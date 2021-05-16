package com.qr.app.backend.entity;

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
public class Operator extends AbstractEntity {

    private String name;
    private String macAddress;

}
