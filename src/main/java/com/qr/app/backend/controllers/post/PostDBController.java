package com.qr.app.backend.controllers.post;

import com.qr.app.backend.Json.db.LockDB;
import com.qr.app.backend.entity.db.StateDB;
import com.qr.app.backend.repository.db.StateDBRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class PostDBController {

    private final StateDBRepository stateDBRepository;

    public PostDBController (StateDBRepository stateDBRepository) {
        this.stateDBRepository = stateDBRepository;
    }

    @PostMapping ("/post/lockDB")
    public ResponseEntity<String> lockDB (@RequestBody LockDB lock) {
        Date date = new Date();
        boolean lockBool = Boolean.parseBoolean(lock.getLock());
        stateDBRepository.save(new StateDB(lockBool, lock.getMessage(), date.getTime()));
        if (lockBool)
            return new ResponseEntity<>("Блокировка установлена", HttpStatus.OK);
        else
            return new ResponseEntity<>("Блокировка снята", HttpStatus.OK);
    }

}
