package com.qr.app.backend.repository.order;

import com.qr.app.backend.entity.order.Box;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BoxRepository extends JpaRepository<Box, Long> {

    @Query("select box from Box box where box.numberBox = :numberBox")
    Optional<Box> findByNumberBox(@Param("numberBox") String numberBox);

}
