package com.qr.app.backend.service;

import com.qr.app.backend.entity.forSession.LogSession;
import com.qr.app.backend.entity.forSession.LvlEvent;
import com.qr.app.backend.repository.LogSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class LogService {

    @Autowired
    private static LogSessionRepository logSessionRepository;

    public void saveLog (String bufferCode, String descriptionEvent, LvlEvent lvlEvent, String macAddress) {
        LogSession event = new LogSession(new Date().getTime(), bufferCode, descriptionEvent, lvlEvent, macAddress);
        logSessionRepository.save(event);
    }

}
