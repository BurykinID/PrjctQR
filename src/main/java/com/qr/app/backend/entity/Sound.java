package com.qr.app.backend.entity;

import lombok.*;

import javax.persistence.Entity;
import java.io.File;

@Entity
@Getter
@Setter
@Data
@NoArgsConstructor
public class Sound extends AbstractEntity {

    private String filename;
    private byte[] sound;

    public Sound(String filename, byte[] file) {
        this.filename = filename;
        this.sound = file;
    }

}
