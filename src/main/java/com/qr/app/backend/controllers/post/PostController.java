package com.qr.app.backend.controllers.post;

import com.qr.app.backend.Json.container.OrderDao;
import com.qr.app.backend.Json.container.dao.BoxDao;
import com.qr.app.backend.Json.container.dao.DescriptionBoxDao;
import com.qr.app.backend.Json.container.dao.VariantBoxDao;
import com.qr.app.backend.Json.db.LockDB;
import com.qr.app.backend.entity.Good;
import com.qr.app.backend.entity.Mark;
import com.qr.app.backend.entity.Sound;
import com.qr.app.backend.entity.db.StateDB;
import com.qr.app.backend.entity.order.Box;
import com.qr.app.backend.entity.order.DescriptionBox;
import com.qr.app.backend.entity.order.Order;
import com.qr.app.backend.entity.order.VariantBox;
import com.qr.app.backend.repository.GoodRepository;
import com.qr.app.backend.repository.MarkRepository;
import com.qr.app.backend.repository.db.StateDBRepository;
import com.qr.app.backend.repository.order.BoxRepository;
import com.qr.app.backend.repository.order.DescriptionBoxRepostitory;
import com.qr.app.backend.repository.order.OrderRepository;
import com.qr.app.backend.repository.order.VariantsBoxRepostiorty;
import com.qr.app.backend.repository.sound.SoundRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@RestController
public class PostController {

    private final MarkRepository markRepository;
    private final BoxRepository boxRepository;
    private final DescriptionBoxRepostitory descriptionBoxRepostitory;
    private final VariantsBoxRepostiorty variantsBoxRepostiorty;
    private final GoodRepository goodRepository;
    private final OrderRepository orderRepository;
    private final StateDBRepository stateDBRepository;
    private final SoundRepository soundRepository;

    public PostController (MarkRepository markRepository, BoxRepository boxRepository, DescriptionBoxRepostitory descriptionBoxRepostitory, VariantsBoxRepostiorty variantsBoxRepostiorty, GoodRepository goodRepository, OrderRepository orderRepository, StateDBRepository stateDBRepository, SoundRepository soundRepository) {
        this.markRepository = markRepository;
        this.boxRepository = boxRepository;
        this.descriptionBoxRepostitory = descriptionBoxRepostitory;
        this.variantsBoxRepostiorty = variantsBoxRepostiorty;
        this.goodRepository = goodRepository;
        this.orderRepository = orderRepository;
        this.stateDBRepository = stateDBRepository;
        this.soundRepository = soundRepository;
    }

    @PostMapping("/post/manyOrders")
    public ResponseEntity insertManyObject(@RequestBody List<OrderDao> orderDaoList) {

        long countMarksBeforeInsert = orderRepository.count();

        List<Order> orderList = new LinkedList<>();
        List<VariantBox> variantBoxes = new LinkedList<>();
        List<DescriptionBox> descriptionBoxes = new LinkedList<>();
        List<Box> boxList = new LinkedList<>();

        for (OrderDao orderDao : orderDaoList) {

            Order order = setOrder(orderDao);

            orderList.add(order);
            List<VariantBox> variantBoxList = setVariantBox(orderDao, order);
            variantBoxes.addAll(variantBoxList);

            descriptionBoxes.addAll(setDescriptionBox(orderDao, variantBoxList));
            boxList.addAll(setBox(orderDao, variantBoxList));

        }

        orderRepository.saveAll(orderList);
        variantsBoxRepostiorty.saveAll(variantBoxes);
        descriptionBoxRepostitory.saveAll(descriptionBoxes);
        boxRepository.saveAll(boxList);

        // количество записей в таблице, после добавления заказа
        long countMarksAfterInsert = orderRepository.count();
        // количество записей, которые добавлены в таблицу
        long countInsertInTable = countMarksAfterInsert - countMarksBeforeInsert;

        if (countInsertInTable == orderDaoList.size()){
            return new ResponseEntity("Добавлено записей: " + orderDaoList.size(), HttpStatus.OK);
        }
        else
            return new ResponseEntity("Добавлено записей: " + countInsertInTable, HttpStatus.BAD_REQUEST);

    }

    @PostMapping("/post/manyGoods")
    public ResponseEntity insertManyGoods(@RequestBody List<Good> goods) {
        // количество записей в таблице, до добавления товаров
        long countGoodsBeforeInsert = goodRepository.count();
        goodRepository.saveAll(goods);
        // количество записей в таблице, после добавления товаров
        long countGoodsAfterInsert = goodRepository.count();
        // количество записей, которые добавлены в таблицу
        long countInsertInTable = countGoodsAfterInsert - countGoodsBeforeInsert;

        if (countInsertInTable == goods.size())
            return new ResponseEntity("Добавлено записей: " + goods.size(), HttpStatus.OK);
        else
            return new ResponseEntity("Добавлено записей: " + countInsertInTable, HttpStatus.BAD_REQUEST);

    }

    @PostMapping("/post/oneGood")
    public ResponseEntity insertOneGood(@RequestBody Good good) {

        long countGoodBeforeInsert = goodRepository.count();
        goodRepository.save(good);
        // количество записей в таблице, после добавления товаров
        long countGoodAfterInsert = goodRepository.count();
        // количество записей, которые добавлены в таблицу
        long countInsertInTable = countGoodAfterInsert - countGoodBeforeInsert;

        if (countInsertInTable == 1)
            return new ResponseEntity("Добавлено записей: 1", HttpStatus.OK);
        else
            return new ResponseEntity("Добавлено записей: " + countInsertInTable, HttpStatus.BAD_REQUEST);

    }

    @PostMapping("/post/oneMark")
    public ResponseEntity insertOneMark(@RequestBody Mark mark) {

        long countMarksBeforeInsert = markRepository.count();
        markRepository.save(mark);
        // количество записей в таблице, после добавления марок
        long countMarksAfterInsert = markRepository.count();
        // количество записей, которые добавлены в таблицу
        long countInsertInTable = countMarksAfterInsert - countMarksBeforeInsert;

        if (countInsertInTable == 1)
            return new ResponseEntity("Добавлено записей: 1", HttpStatus.OK);
        else
            return new ResponseEntity("Добавлено записей: " + countInsertInTable, HttpStatus.BAD_REQUEST);

    }

    @PostMapping("/post/manyMarks")
    public ResponseEntity insertManyMark(@RequestBody List<Mark> marks) {

        long countMarksBeforeInsert = markRepository.count();
        markRepository.saveAll(marks);
        // количество записей в таблице, после добавления описания
        long countMarksAfterInsert = markRepository.count();
        // количество записей, которые добавлены в таблицу
        long countInsertInTable = countMarksAfterInsert - countMarksBeforeInsert;

        if (countInsertInTable == marks.size())
            return new ResponseEntity("Добавлено записей: " + marks.size(), HttpStatus.OK);
        else
            return new ResponseEntity("Добавлено записей: " + countInsertInTable, HttpStatus.BAD_REQUEST);

    }

    @PostMapping("/post/lockDB")
    public ResponseEntity lockDB (@RequestBody LockDB lock) {

        Date date = new Date();

        boolean lockBool = Boolean.parseBoolean(lock.getLock());

        stateDBRepository.save(new StateDB(lockBool, lock.getMessage(), date.getTime()));

        if (lockBool) {
            return new ResponseEntity("Блокировка установлена", HttpStatus.OK);
        }
        else {
            return new ResponseEntity("Блокировка снята", HttpStatus.OK);
        }

    }

    @PostMapping("/post/addsound")
    public ResponseEntity addSound(@RequestBody MultipartFile file) {

        try {
            soundRepository.save(new Sound(file.getOriginalFilename(), file.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ResponseEntity("Файлы были сохранены в базу", HttpStatus.OK);

    }

    @PostMapping("/post/addManySound")
    public ResponseEntity addManySound(@RequestBody List<MultipartFile> files) {

        try {
            for (MultipartFile file : files) {
                soundRepository.save(new Sound(file.getOriginalFilename(), file.getBytes()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ResponseEntity("Файлы были сохранены в базу", HttpStatus.OK);

    }

    private List<Box> setBox (OrderDao orderDao, List<VariantBox> variantBoxList) {

        List<Box> boxList = new LinkedList<>();

        for (BoxDao dao : orderDao.getBoxes()) {
            Box box = new Box();
            box.setNumberBox(dao.getNumberBox());
            box.setStatus(dao.getStatus());

            for (VariantBox variantBox : variantBoxList) {
                if (variantBox.getNumberVariant().equals(dao.getNumberVariant())) {
                    box.setVariantBox(variantBox);
                }
            }

            boxList.add(box);
        }

        return boxList;

    }

    private List<DescriptionBox> setDescriptionBox (OrderDao orderDao, List<VariantBox> boxList) {

        List<DescriptionBox> descriptionBoxList = new LinkedList<>();

        for (DescriptionBoxDao dao : orderDao.getDescriptionBoxes()) {
            DescriptionBox box = new DescriptionBox();
            box.setBarcode(dao.getBarcode());
            box.setCount(dao.getCount());
            box.setNumberLine(dao.getNumberLine());
            for (VariantBox variantBox : boxList) {
                if (variantBox.getNumberVariant().equals(dao.getNumberVariant())) {
                    box.setVariantBox(variantBox);
                }
            }

            descriptionBoxList.add(box);
        }

        return descriptionBoxList;

    }

    private List<VariantBox> setVariantBox (OrderDao orderDao, Order order) {

        List<VariantBox> list = new LinkedList<>();

        for (VariantBoxDao dao : orderDao.getVariantBoxes()) {
            VariantBox box = new VariantBox();
            box.setNumberVariant(dao.getNumberVariant());
            box.setCountBox(dao.getCountBox());
            box.setCountInBox(dao.getCountInBox());
            box.setOrder(order);
            list.add(box);
        }

        return list;

    }

    private Order setOrder (OrderDao orderDao) {

        Order order = new Order();
        order.setNumber(orderDao.getNumber());

        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(orderDao.getDate());
            order.setDate(date.getTime());
        } catch (ParseException e) {
            System.out.println("Ошибка формата даты: number " + orderDao.getNumber() + ". date " + orderDao.getDate());
        }

        if (orderDao.getStatus() != null && !orderDao.getStatus().equals(""))
            order.setStatus(orderDao.getStatus());

        return order;

    }

}
