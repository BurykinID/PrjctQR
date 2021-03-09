package com.qr.app.backend;

import com.qr.app.backend.service.SoundService;
import com.qr.app.backend.sound.Sound;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Player {

    @Autowired
    private static SoundService soundService;

    public Player() {}

    public static void playSound (String nameSound) {
        com.qr.app.backend.entity.Sound forPlay = soundService.getSound(nameSound);

        if (!forPlay.getFilename().equals("Ок.wav")) {
            File file = null;
            try {
                file = new File(new File(".").getAbsolutePath(), forPlay.getFilename());
            }
            catch (NullPointerException e) {
                e.printStackTrace();
            }
            Sound sound = null;
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                OutputStream outStream = new FileOutputStream(file);
                outStream.write(forPlay.getSound());
                outStream.close();
                sound = new Sound(file);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (sound != null) {
                sound.play();
                //sound.join();
            }
            sound.close();
            file.delete();
        }
    }

}
