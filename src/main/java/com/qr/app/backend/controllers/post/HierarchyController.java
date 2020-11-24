package com.qr.app.backend.controllers.post;

import com.qr.app.backend.Json.container.HierarchyOfBoxesJson;
import com.qr.app.backend.entity.HierarchyOfBoxes;
import com.qr.app.backend.repository.HierarchyOfBoxesRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class HierarchyController {

    private final HierarchyOfBoxesRepository hierarchyOfBoxesRepository;

    public HierarchyController (HierarchyOfBoxesRepository hierarchyOfBoxesRepository) {
        this.hierarchyOfBoxesRepository = hierarchyOfBoxesRepository;
    }

    @PostMapping ("/post/insertHierarchy")
    public ResponseEntity<String> insertHierarchyContainers(@RequestBody List<HierarchyOfBoxesJson> hierarchyList) {
        long countHierarchyListBeforeInsert = hierarchyList.size();
        ArrayList<HierarchyOfBoxes> hierarchyOfBoxes = new ArrayList<>();
        for (HierarchyOfBoxesJson json : hierarchyList) {
            try {
                HierarchyOfBoxes hierarchyOfBox = new HierarchyOfBoxes(json.getNumberContainer(), json.getNumberBox(), json.getDate());
                hierarchyOfBoxes.add(hierarchyOfBox);
            } catch (ParseException e) {
                return new ResponseEntity<>("Incorrect date format " + json.getDate(), HttpStatus.BAD_REQUEST);
            }

        }
        hierarchyOfBoxesRepository.saveAll(hierarchyOfBoxes);
        long countInsertInTable = hierarchyList.size() - countHierarchyListBeforeInsert;
        return new ResponseEntity<>("Добавлено записей: " + countInsertInTable, HttpStatus.OK);
    }

    @PostMapping ("/post/updateHierarchy")
    public ResponseEntity<String> updateHierarchyContainers(@RequestBody List<HierarchyOfBoxesJson> hierarchyList) {
        long countHierarchyListBeforeInsert = hierarchyList.size();
        for (HierarchyOfBoxesJson json : hierarchyList) {
            try {
                HierarchyOfBoxes hierarchyOfBox = hierarchyOfBoxesRepository.findByNumberContainerAndNumberBox(json.getNumberContainer(), json.getNumberBox()).orElse(new HierarchyOfBoxes());
                if (hierarchyOfBox.isPersisted()) {
                    hierarchyOfBox.update(json);
                }
                else {
                    hierarchyOfBox = new HierarchyOfBoxes(json.getNumberContainer(), json.getNumberBox(), json.getDate());
                }
                hierarchyOfBoxesRepository.save(hierarchyOfBox);
            } catch (ParseException e) {
                return new ResponseEntity<>("Incorrect date format " + json.getDate(), HttpStatus.BAD_REQUEST);
            }
        }
        long countInsertInTable = hierarchyList.size() - countHierarchyListBeforeInsert;
        return new ResponseEntity<>("Добавлено записей: " + countInsertInTable, HttpStatus.OK);
    }

}
