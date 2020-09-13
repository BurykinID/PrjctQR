package com.qr.app.backend.repository.db;

import com.qr.app.backend.entity.db.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Transaction findBySession(String session);

}
