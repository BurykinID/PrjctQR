package com.qr.app.backend.Json;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;

@Getter
@Setter
@Data
@NoArgsConstructor
public class SoundJson {

    private String filename;
    private File sound;

}
