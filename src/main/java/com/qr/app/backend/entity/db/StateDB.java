package com.qr.app.backend.entity.db;

import com.qr.app.backend.entity.AbstractEntity;
import lombok.*;

import javax.persistence.Entity;

@Entity
@Getter
@Setter
@Data
@NoArgsConstructor
public class StateDB extends AbstractEntity {

    private boolean lock;
    private String description;
    private long timeStartBlock;

    public StateDB(boolean lock, String description, long timeStartBlock) {
        this.lock = lock;
        this.description = description;
        this.timeStartBlock = timeStartBlock;
    }

}
