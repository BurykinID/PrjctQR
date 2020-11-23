package com.qr.app.backend.repository.db;

import com.qr.app.backend.entity.db.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Transaction findBySession(String session);
    @Query ("select transaction from Transaction transaction where transaction.session = :transaction")
    List<Transaction> findBySessions(@Param("transaction") String session);

}
