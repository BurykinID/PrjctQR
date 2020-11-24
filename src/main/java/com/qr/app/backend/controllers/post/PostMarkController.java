package com.qr.app.backend.controllers.post;

import com.qr.app.backend.entity.Mark;
import com.qr.app.backend.repository.MarkRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PostMarkController {

    private final MarkRepository markRepository;

    public PostMarkController (MarkRepository markRepository) {
        this.markRepository = markRepository;
    }

    @PostMapping ("/post/insertMarks")
    public ResponseEntity<String> insertMark(@RequestBody List<Mark> marks) {
        long countMarksBeforeInsert = markRepository.count();
        markRepository.saveAll(marks);
        long countInsertInTable = markRepository.count() - countMarksBeforeInsert;
        return new ResponseEntity<>("Добавлено записей: " + countInsertInTable, HttpStatus.OK);
    }
    @PostMapping("/post/updateMarks")
    public ResponseEntity<String> updateMark(@RequestBody List<Mark> marks) {
        long countMarksBeforeInsert = markRepository.count();
        for ( Mark mark: marks) {
            Mark changesMark = markRepository.findByCis(mark.getCis()).orElse(new Mark());
            if (!changesMark.getCis().isEmpty())
                changesMark.updateMark(mark.getBarcode(), mark.getNumberBox(), mark.getNumberOrder(), mark.getDate());
            else
                changesMark = new Mark(mark.getCis(), mark.getBarcode(), mark.getNumberBox(), mark.getNumberOrder(), mark.getDate());
            markRepository.save(changesMark);
        }
        long countInsertInTable = markRepository.count() - countMarksBeforeInsert;
        return new ResponseEntity<>("Добавлено записей: " + countInsertInTable, HttpStatus.OK);
    }

}
