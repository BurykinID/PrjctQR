package com.qr.app.backend.repository;

import com.qr.app.backend.entity.Mark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MarkRepository extends JpaRepository<Mark, Long> {

    Mark findByCis(String cis);

    @Query("select mark from mark mark where mark.date < :date")
    List<Mark> findByDate(@Param("date") long date);

}
