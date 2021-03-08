package com.qr.app.backend.repository.sound;

import com.qr.app.backend.entity.Sound;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SoundRepository extends JpaRepository<Sound, Long> {

    Optional<Sound> findByFilename(String filename);

}
