package com.qr.app.backend.service;

import com.qr.app.backend.entity.forSession.LogSession;
import com.qr.app.backend.entity.forSession.LvlEvent;
import com.qr.app.backend.repository.LogSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
public class LogService {

    private LogSessionRepository logSessionRepository;

    public LogService(LogSessionRepository logSessionRepository) {
        this.logSessionRepository = logSessionRepository;
    }

    public void saveLog (String bufferCode, String descriptionEvent, LvlEvent lvlEvent, String macAddress) {
        LogSession event = new LogSession(new Date().getTime(), bufferCode, descriptionEvent, lvlEvent, macAddress);
        logSessionRepository.save(event);
    }


}
