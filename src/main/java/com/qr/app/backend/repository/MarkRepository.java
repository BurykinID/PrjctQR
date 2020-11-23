package com.qr.app.backend.repository;

import com.qr.app.backend.entity.Mark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MarkRepository extends JpaRepository<Mark, Long> {

    Optional<Mark> findByCis(String cis);

    @Query("select mark from mark mark where mark.date >= :date and (mark.numberBox is not null and mark.numberBox <> '')")
    List<Mark> findByDate(@Param("date") long date);

}
