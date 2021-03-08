package com.qr.app.backend.service;

import com.qr.app.backend.repository.sound.SoundRepository;
import com.qr.app.backend.entity.Sound;
import org.springframework.stereotype.Service;

@Service
public class SoundService {

    private final SoundRepository soundRepository;

    public SoundService(SoundRepository soundRepository) {
        this.soundRepository = soundRepository;
    }

    public Sound getSound(String nameSound) {
        return soundRepository.findByFilename(nameSound).orElse(new Sound());
    }

}
