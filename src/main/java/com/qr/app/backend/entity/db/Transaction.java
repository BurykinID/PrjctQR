package com.qr.app.backend.entity.db;

import com.qr.app.backend.entity.AbstractEntity;
import lombok.*;

import javax.persistence.Entity;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Data
public class Transaction extends AbstractEntity {

    private String session;

    public Transaction (String session) {
        this.session = session;
    }

}
