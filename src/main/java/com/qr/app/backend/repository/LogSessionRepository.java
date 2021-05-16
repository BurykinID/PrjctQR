package com.qr.app.backend.repository;

import com.qr.app.backend.Json.BoxOperator;
import com.qr.app.backend.entity.forSession.LogSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LogSessionRepository extends JpaRepository<LogSession, Long> {

    @Query("select log from LogSession log left join Operator o " +
            "on o.macAddress = log.macAddress where log.phase like '%собран%' and log.timeEvent >= :date group by o.name")
    List<LogSession> findAssembledBoxAndContainersByDate(@Param("date") long date);

}
