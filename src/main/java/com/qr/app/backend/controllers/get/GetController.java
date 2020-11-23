package com.qr.app.backend.controllers.get;

import com.qr.app.backend.Json.get.*;
import com.qr.app.backend.entity.HierarchyOfBoxes;
import com.qr.app.backend.entity.Mark;
import com.qr.app.backend.entity.db.StateDB;
import com.qr.app.backend.repository.HierarchyOfBoxesRepository;
import com.qr.app.backend.repository.MarkRepository;
import com.qr.app.backend.repository.db.StateDBRepository;
import com.qr.app.backend.repository.db.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@RestController()
public class GetController {

    private final MarkRepository markRepository;
    private final TransactionRepository transactionRepository;
    private final StateDBRepository stateDBRepository;
    private final HierarchyOfBoxesRepository hierarchyOfBoxesRepository;

    public GetController (MarkRepository markRepository, TransactionRepository transactionRepository, StateDBRepository stateDBRepository, HierarchyOfBoxesRepository hierarchyOfBoxesRepository) {
        this.markRepository = markRepository;
        this.transactionRepository = transactionRepository;
        this.stateDBRepository = stateDBRepository;
        this.hierarchyOfBoxesRepository = hierarchyOfBoxesRepository;
    }

    @GetMapping("/get")
    public JsonReturn checkMarkWithPartQueryInUrl (@RequestParam("cis") String cis,
                                                   @RequestParam("numberBox") String numberBox) {

        return checkMark(cis, numberBox);

    }

    @GetMapping("/get/ping")
    public ResponseEntity ping() {
        return new ResponseEntity("OK", HttpStatus.OK);
    }

    // отбор записей по дате
    @GetMapping("/get/choise")
    public MarkAndContainerJson getSample(@RequestParam("dateFrom") String dateFrom) {

        List<MarkJson> markJsons = new LinkedList<>();
        List<HierarchyOfBox> containerJson = new LinkedList<>();
        MarkAndContainerJson answer = new MarkAndContainerJson();

        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateFrom);
        } catch (ParseException e) {
            return new MarkAndContainerJson();
        }

        List<Mark> marks = markRepository.findByDate(date.getTime());
        if (marks.size() > 0) {
            for (Mark mark: marks) {
                date = new Date(mark.getDate());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                markJsons.add(new MarkJson(mark.getCis(), mark.getNumberBox(), simpleDateFormat.format(date)));
            }
        }

        List<HierarchyOfBoxes> container = hierarchyOfBoxesRepository.findByDate(date.getTime());
        if (container.size() > 0) {
            for (HierarchyOfBoxes hierarchy : container) {
                date = new Date(hierarchy.getDate());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                containerJson.add(new HierarchyOfBox(hierarchy.getNumberBox(), hierarchy.getNumberContainer(), simpleDateFormat.format(date)));
            }
        }

        answer.setContainerJson(containerJson);
        answer.setMarkJson(markJsons);

        return answer;

    }

    public JsonReturn checkMark (String cis, String numberBox) {
        Mark mark = markRepository.findByCis(cis).orElse(new Mark());
        MarkJsonCheck markJson = new MarkJsonCheck();
        if (!mark.getCis().isEmpty()) {
            markJson.setCis(mark.getCis());
            markJson.setNumberBox(mark.getNumberBox());
            markJson.setBarcode(mark.getBarcode());
            if (mark.getNumberBox().equals(numberBox)) {
                return new JsonReturn("Ошибка, марка уже добавлена в этот короб", markJson);
            }
            else if (!mark.getNumberBox().equals(numberBox) && !mark.getNumberBox().equals("")) {
                String numberBoxInBase = mark.getNumberBox();
                mark.setNumberBox(numberBox);
                return new JsonReturn("Ошибка, марка уже добавлена в другой короб: " + numberBoxInBase, markJson);
            }
            else {
                mark.setNumberBox(numberBox);
                markJson.setNumberBox(numberBox);
                mark.setDate(new Date().getTime());
                markRepository.save(mark);
                return new JsonReturn("Ок", markJson);
            }
        }
        else {
            markJson.setCis(cis);
            markJson.setNumberBox(numberBox);
            markJson.setBarcode("");
            return new JsonReturn("Не найдена такая марка", markJson);
        }
    }

    @GetMapping("/get/state")
    public ResponseEntity getStateDB() {

        List<StateDB> states = stateDBRepository.findAllSortByIdDesc();
        if (states.size() > 0) {

            if (states.get(0).isLock()) {
                if (transactionRepository.findAll().size() > 0) {
                    return new ResponseEntity("База ещё не заблокирована!", HttpStatus.OK);
                }
                else {
                    return new ResponseEntity("База заблокирована", HttpStatus.OK);
                }
            }
            else {
                return new ResponseEntity("База разблокирована", HttpStatus.OK);
            }

        }

        return new ResponseEntity("База разблокирована", HttpStatus.OK);


    }

}
