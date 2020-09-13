package com.qr.app.backend.repository.db;

import com.qr.app.backend.entity.db.StateDB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StateDBRepository extends JpaRepository<StateDB, Long> {

    @Query("select state from StateDB state order by state.id desc")
    List<StateDB> findAllSortByIdDesc();

}
