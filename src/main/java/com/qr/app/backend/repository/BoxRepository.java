package com.qr.app.backend.repository;

import com.qr.app.backend.entity.box.Box;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoxRepository extends JpaRepository<Box, Long> {

    @Query("select box from Box box where box.numberBox = :numberBox")
    Box findByNumberBox(@Param("numberBox") String numberBox);

}
