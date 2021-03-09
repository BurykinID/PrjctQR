package com.qr.app.backend.service;

import com.qr.app.backend.BuilderContainer;
import com.qr.app.backend.entity.db.Transaction;
import com.qr.app.backend.entity.forSession.LvlEvent;
import com.qr.app.backend.repository.db.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Service;

import static com.qr.app.backend.service.LogService.saveLog;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public void closeTransaction (BuilderContainer currentContainer) {
        transactionRepository.deleteAll(transactionRepository.findBySessions(currentContainer.getMacAddress()));
        saveLog("", "Транзакция закрыта", LvlEvent.CRITICAL, currentContainer.getMacAddress());
    }

    public void openTransaction(BuilderContainer macAddress) {
        transactionRepository.save(new Transaction(macAddress.getMacAddress()));
    }

}
