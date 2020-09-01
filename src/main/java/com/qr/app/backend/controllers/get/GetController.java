package com.qr.app.backend.controllers.get;

import com.qr.app.backend.Json.Cis;
import com.qr.app.backend.Json.NumberBox;
import com.qr.app.backend.Json.get.JsonReturn;
import com.qr.app.backend.Json.get.MarkJson;
import com.qr.app.backend.entity.Mark;
import com.qr.app.backend.repository.MarkRepository;
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

    public GetController (MarkRepository markRepository) {
        this.markRepository = markRepository;
    }


    @GetMapping("/get")
    public JsonReturn checkMarkWithPartQueryInUrl (@RequestParam("cis") String cis,
                                                   @RequestParam("numberBox") String numberBox) {

        return checkMark(cis, numberBox);

    }

    // отбор записей по дате
    @GetMapping("/get/choise")
    public List<MarkJson> getSample(@RequestBody String dateFrom) {

        List<MarkJson> markJsons = new LinkedList<>();

        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateFrom);
        } catch (ParseException e) {
            markJsons.add(new MarkJson("Неверный формат даты", "Неверный формат даты", "Неверный формат даты"));
            return markJsons;
        }

        List<Mark> marks = markRepository.findByDate(date.getTime());
        if (marks.size() > 0) {
            for (Mark mark: marks) {
                markJsons.add(new MarkJson(mark.getCis(), mark.getNumberBox(), mark.getBarcode()));
            }
            return markJsons;
        }

        return markJsons;

    }

    public JsonReturn checkMark (String cis, String numberBox) {
        Mark mark = markRepository.findByCis(cis);
        MarkJson markJson = new MarkJson();
        if (mark != null) {
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

}
