package com.qr.app.backend.service;

import com.qr.app.backend.entity.Mark;
import com.qr.app.backend.repository.MarkRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarkService {

    private final MarkRepository markRepository;

    public MarkService (MarkRepository markRepository) {
        this.markRepository = markRepository;
    }

    public List<Mark> findAll() {
        return markRepository.findAll();
    }

    public List<Mark> findForTest() {
        return markRepository.findAll().subList(1, 100);
    }



}
