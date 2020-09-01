package com.qr.app.backend.repository;

import com.qr.app.backend.entity.forSession.LogSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventSessionRepository extends JpaRepository<LogSession, Long> {



}
