package com.qr.app.backend.repository;

import com.qr.app.backend.entity.forSession.LogSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogSessionRepository extends JpaRepository<LogSession, Long> {



}
