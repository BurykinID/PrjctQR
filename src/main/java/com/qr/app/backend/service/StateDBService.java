package com.qr.app.backend.service;

import com.qr.app.backend.entity.db.StateDB;
import com.qr.app.backend.repository.db.StateDBRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StateDBService {

    @Autowired
    private static StateDBRepository stateDBRepository;

    public static StateDB getDbState() {
        List<StateDB> stateList = stateDBRepository.findAllSortByIdDesc();
        //String currentState = stateList.get(0).getDescription();
        StateDB currentState = stateList.get(0);
        return currentState != null ? currentState : new StateDB();
    }

}
