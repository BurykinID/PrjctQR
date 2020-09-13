package com.qr.app.backend.controllers.get;

import com.qr.app.backend.Json.DateFormat;
import com.qr.app.backend.Json.get.JsonReturn;
import com.qr.app.backend.Json.get.MarkJson;
import com.qr.app.backend.Json.get.MarkJsonCheck;
import com.qr.app.backend.entity.Mark;
import com.qr.app.backend.entity.db.StateDB;
import com.qr.app.backend.repository.MarkRepository;
import com.qr.app.backend.repository.db.StateDBRepository;
import com.qr.app.backend.repository.db.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    public GetController (MarkRepository markRepository, TransactionRepository transactionRepository, StateDBRepository stateDBRepository) {
        this.markRepository = markRepository;
        this.transactionRepository = transactionRepository;
        this.stateDBRepository = stateDBRepository;
    }


    @GetMapping("/get")
    public JsonReturn checkMarkWithPartQueryInUrl (@RequestParam("cis") String cis,
                                                   @RequestParam("numberBox") String numberBox) {

        return checkMark(cis, numberBox);

    }

    // отбор записей по дате
    @GetMapping("/get/choise")
    public List<MarkJson> getSample(@RequestBody DateFormat dateFrom) {

        List<MarkJson> markJsons = new LinkedList<>();

        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateFrom.getDateFrom());
        } catch (ParseException e) {
            markJsons.add(new MarkJson("Неверный формат даты", "Неверный формат даты", "Неверный формат даты"));
            return markJsons;
        }

        List<Mark> marks = markRepository.findByDate(date.getTime());
        if (marks.size() > 0) {
            for (Mark mark: marks) {

                date = new Date(mark.getDate());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                markJsons.add(new MarkJson(mark.getCis(), mark.getNumberBox(), simpleDateFormat.format(date)));

            }
            return markJsons;
        }

        return markJsons;

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
