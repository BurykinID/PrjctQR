package com.qr.app.backend.controllers.post;

import com.qr.app.backend.entity.Good;
import com.qr.app.backend.repository.GoodRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;

@RestController
public class PostGoodController {

    private final GoodRepository goodRepository;

    public PostGoodController (GoodRepository goodRepository) {
        this.goodRepository = goodRepository;
    }

    @PostMapping ("/post/insertGoods")
    public ResponseEntity<String> insertGoods(@RequestBody List<Good> goods) {
        long countGoodsBeforeInsert = goodRepository.count();
        goodRepository.saveAll(goods);
        long countInsertInTable = goodRepository.count() - countGoodsBeforeInsert;
        return new ResponseEntity<>("Добавлено записей: " + countInsertInTable, HttpStatus.OK);
    }
    @PostMapping("/post/updateGoods")
    public ResponseEntity<String> updateGoods(@RequestBody List<Good> goods) {
        long countGoodsBeforeInsert = goodRepository.count();
        List<Good> goodList = new LinkedList<>();
        for (Good good : goods) {
            Good changesGood = goodRepository.findByBarcode(good.getBarcode()).orElse(new Good());
            if (!changesGood.getBarcode().isEmpty())
                changesGood.updateGood(good.getName(), good.getArticle(), good.getColor(), good.getSize());
            else
                changesGood = new Good(good.getBarcode(), good.getName(), good.getArticle(), good.getColor(), good.getSize());
            goodList.add(changesGood);
        }
        goodRepository.saveAll(goodList);
        long countInsertInTable = goodRepository.count() - countGoodsBeforeInsert;
        return new ResponseEntity<>("Добавлено записей: " + countInsertInTable, HttpStatus.OK);
    }

}
