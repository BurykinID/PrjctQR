package com.qr.app.backend.controllers;

import com.qr.app.backend.entity.Sound;
import com.qr.app.backend.repository.sound.SoundRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
public class SoundController {

    private final SoundRepository soundRepository;

    public SoundController (SoundRepository soundRepository) {
        this.soundRepository = soundRepository;
    }

    @PostMapping ("/post/insertSound")
    public ResponseEntity<String> insertSound(@RequestBody List<MultipartFile> files) {
        try {
            for (MultipartFile file : files) {
                Sound sound = soundRepository.findByFilename(file.getOriginalFilename());
                if (sound!=null) {
                    if (sound.getFilename().isEmpty()) {
                        sound.setFilename(file.getOriginalFilename());
                    }
                }
                else {
                    sound = new Sound();
                    sound.setFilename(file.getOriginalFilename());
                }


                sound.setSound(file.getBytes());
                soundRepository.save(sound);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>("Файлы были сохранены в базу", HttpStatus.OK);
    }

}
