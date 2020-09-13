package com.qr.app.backend.repository.sound;

import com.qr.app.backend.entity.Sound;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SoundRepository extends JpaRepository<Sound, Long> {

    Sound findByFilename(String filename);

}
