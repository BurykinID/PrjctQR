package com.qr.app.backend.controllers.post;

import com.qr.app.backend.Json.box.OrderDao;
import com.qr.app.backend.Json.box.dao.BoxDao;
import com.qr.app.backend.Json.box.dao.DescriptionBoxDao;
import com.qr.app.backend.Json.box.dao.VariantBoxDao;
import com.qr.app.backend.Json.container.HierarchyOfBoxesJson;
import com.qr.app.backend.Json.container.OrderContainerDao;
import com.qr.app.backend.Json.container.dao.ContainerDao;
import com.qr.app.backend.Json.container.dao.DescriptionContainerDao;
import com.qr.app.backend.Json.container.dao.VariantContainerDao;
import com.qr.app.backend.Json.db.LockDB;
import com.qr.app.backend.Json.get.MarkJson;
import com.qr.app.backend.entity.Good;
import com.qr.app.backend.entity.HierarchyOfBoxes;
import com.qr.app.backend.entity.Mark;
import com.qr.app.backend.entity.Sound;
import com.qr.app.backend.entity.db.StateDB;
import com.qr.app.backend.entity.order.box.Box;
import com.qr.app.backend.entity.order.box.DescriptionBox;
import com.qr.app.backend.entity.order.box.Order;
import com.qr.app.backend.entity.order.box.VariantBox;
import com.qr.app.backend.entity.order.container.Container;
import com.qr.app.backend.entity.order.container.DescriptionContainer;
import com.qr.app.backend.entity.order.container.OrderContainer;
import com.qr.app.backend.entity.order.container.VariantContainer;
import com.qr.app.backend.repository.GoodRepository;
import com.qr.app.backend.repository.HierarchyOfBoxesRepository;
import com.qr.app.backend.repository.MarkRepository;
import com.qr.app.backend.repository.db.StateDBRepository;
import com.qr.app.backend.repository.order.box.BoxRepository;
import com.qr.app.backend.repository.order.box.DescriptionBoxRepository;
import com.qr.app.backend.repository.order.box.OrderRepository;
import com.qr.app.backend.repository.order.box.VariantsBoxRepository;
import com.qr.app.backend.repository.order.container.ContainerRepository;
import com.qr.app.backend.repository.order.container.DescriptionContainerRepository;
import com.qr.app.backend.repository.order.container.OrderContainerRepository;
import com.qr.app.backend.repository.order.container.VariantsContainerRepository;
import com.qr.app.backend.repository.sound.SoundRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@RestController
public class PostController {

    private final MarkRepository markRepository;
    private final BoxRepository boxRepository;
    private final DescriptionBoxRepository descriptionBoxRepository;
    private final VariantsBoxRepository variantsBoxRepository;
    private final GoodRepository goodRepository;
    private final OrderRepository orderRepository;
    private final StateDBRepository stateDBRepository;
    private final SoundRepository soundRepository;
    private final ContainerRepository containerRepository;
    private final OrderContainerRepository orderContainerRepository;
    private final VariantsContainerRepository variantsContainerRepository;
    private final DescriptionContainerRepository descriptionContainerRepository;
    private final HierarchyOfBoxesRepository hierarchyOfBoxesRepository;

    public PostController (MarkRepository markRepository, BoxRepository boxRepository, DescriptionBoxRepository descriptionBoxRepository, VariantsBoxRepository variantsBoxRepository, GoodRepository goodRepository, OrderRepository orderRepository, StateDBRepository stateDBRepository, SoundRepository soundRepository, ContainerRepository containerRepository, OrderContainerRepository orderContainerRepository, VariantsContainerRepository variantsContainerRepository, DescriptionContainerRepository descriptionContainerRepository, HierarchyOfBoxesRepository hierarchyOfBoxesRepository) {
        this.markRepository = markRepository;
        this.boxRepository = boxRepository;
        this.descriptionBoxRepository = descriptionBoxRepository;
        this.variantsBoxRepository = variantsBoxRepository;
        this.goodRepository = goodRepository;
        this.orderRepository = orderRepository;
        this.stateDBRepository = stateDBRepository;
        this.soundRepository = soundRepository;
        this.containerRepository = containerRepository;
        this.orderContainerRepository = orderContainerRepository;
        this.variantsContainerRepository = variantsContainerRepository;
        this.descriptionContainerRepository = descriptionContainerRepository;
        this.hierarchyOfBoxesRepository = hierarchyOfBoxesRepository;
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
        variantsBoxRepository.saveAll(variantBoxes);
        descriptionBoxRepository.saveAll(descriptionBoxes);
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
        List<Mark> marksForDB = new LinkedList<>();
        for ( Mark mark: marks) {
            Mark oneMark = markRepository.findByCis(mark.getCis()).orElse(new Mark());
            if (oneMark.getCis().isEmpty()) {
                oneMark.setCis(mark.getCis());
            }
            oneMark.setNumberBox(mark.getNumberBox());
            oneMark.setDate(mark.getDate());
            oneMark.setBarcode(mark.getBarcode());
            oneMark.setNumberOrder(mark.getNumberOrder());
            marksForDB.add(oneMark);
        }
        markRepository.saveAll(marksForDB);
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
            Sound sound = soundRepository.findByFilename(file.getOriginalFilename());
            if (sound.getFilename().isEmpty()) {
                sound.setFilename(file.getOriginalFilename());
            }
            sound.setSound(file.getBytes());
            soundRepository.save(sound);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ResponseEntity("Файлы были сохранены в базу", HttpStatus.OK);

    }

    @PostMapping("post/addContainers")
    public ResponseEntity addContainer(@RequestBody List<OrderContainerDao> containers) {
        int countMarksBeforeInsert = (int) orderContainerRepository.count();
        for (OrderContainerDao orderDao : containers) {
            OrderContainer orderContainer = setOrderContainer(orderDao);
            List<VariantContainer> variantContainerList = setVariantContainer(orderDao, orderContainer);
            setDescriptionContainer(orderDao, variantContainerList);
            setContainer(orderDao, variantContainerList);
        }
        // количество записей в таблице, после добавления заказа
        int countMarksAfterInsert = (int) orderContainerRepository.count();
        // количество записей, которые добавлены в таблицу
        int countInsertInTable = countMarksAfterInsert - countMarksBeforeInsert;
        return new ResponseEntity("Добавлено записей: " + countInsertInTable, HttpStatus.OK);
    }

    @PostMapping("/post/addManySound")
    public ResponseEntity addManySound(@RequestBody List<MultipartFile> files) {
        try {
            for (MultipartFile file : files) {
                Sound sound = soundRepository.findByFilename(file.getOriginalFilename());
                if (sound.getFilename().isEmpty()) {
                    sound.setFilename(file.getOriginalFilename());
                }
                sound.setSound(file.getBytes());
                soundRepository.save(sound);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity("Файлы были сохранены в базу", HttpStatus.OK);
    }

    @PostMapping ("/post/addHierarchy")
    public ResponseEntity addHierarсhyContainers(@RequestBody List<HierarchyOfBoxesJson> hierarchyList) {
        ArrayList<HierarchyOfBoxes> hierarchyOfBoxes = new ArrayList<>();

        for (HierarchyOfBoxesJson json : hierarchyList) {
            Date date = null;
            try {
                date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(json.getDate());
            } catch (ParseException e) {
                return new ResponseEntity("Некорректная дата" + json.getDate(), HttpStatus.BAD_REQUEST);
            }
            HierarchyOfBoxes hierarchyOfBox = hierarchyOfBoxesRepository.findByNumberContainerAndNumberBox(json.getNumberContainer(), json.getNumberBox()).orElse(new HierarchyOfBoxes());
            if (hierarchyOfBox.getNumberContainer().isEmpty()) {
                hierarchyOfBox.setNumberContainer(json.getNumberContainer());
                hierarchyOfBox.setNumberBox(json.getNumberBox());
            }
            hierarchyOfBox.setDate(date.getTime());
            hierarchyOfBoxes.add(hierarchyOfBox);
        }

        hierarchyOfBoxesRepository.saveAll(hierarchyOfBoxes);
        return new ResponseEntity("Ok", HttpStatus.OK);
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

    private OrderContainer setOrderContainer(OrderContainerDao dao) {
        OrderContainer order = orderContainerRepository.findByNumber(dao.getNumber()).orElse(new OrderContainer());
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dao.getDate());
            order.setDate(date.getTime());
        } catch (ParseException e) {
            System.out.println("Ошибка формата даты: number " + dao.getNumber() + ". date " + dao.getDate());
        }
        if (order.getNumber().isEmpty()) {
            order.setNumber(dao.getNumber());
        }
        order.setNumber(dao.getNumber());
        if (dao.getStatus() != null && !dao.getStatus().equals(""))
            order.setStatus(dao.getStatus());

        orderContainerRepository.save(order);
        return order;
    }

    private List<VariantContainer> setVariantContainer(OrderContainerDao orderDao, OrderContainer order) {
        List<VariantContainer> list = new LinkedList<>();
        for (VariantContainerDao dao : orderDao.getVariantContainers()) {
            VariantContainer box = variantsContainerRepository.findByNumberVariant(dao.getNumberVariant()).orElse(new VariantContainer());
            if (box.getNumberVariant().isEmpty()) {
                box.setNumberVariant(dao.getNumberVariant());
            }
            box.setOrderContainer(order);
            box.setCountBox(dao.getCountBox());
            box.setCountInBox(dao.getCountInBox());
            list.add(box);
        }
        variantsContainerRepository.saveAll(list);
        return list;
    }

    private List<DescriptionContainer> setDescriptionContainer (OrderContainerDao orderDao, List<VariantContainer> boxList) {
        List<DescriptionContainer> descriptionBoxList = new LinkedList<>();
        for (DescriptionContainerDao dao : orderDao.getDescriptionContainers()) {
            DescriptionContainer box = descriptionContainerRepository.findByNumberVariantBoxAndVariantContainer(dao.getNumberVariantBox(), variantsContainerRepository.findByNumberVariant(dao.getNumberVariant()).orElse(new VariantContainer()))
                            .orElse(new DescriptionContainer());
            if (box.getNumberVariantBox().isEmpty()) {
                box.setNumberVariantBox(dao.getNumberVariantBox());
            }

            box.setCount(dao.getCount());
            box.setNumberLine(dao.getNumberLine());
            for (VariantContainer variantBox : boxList) {
                if (variantBox.getNumberVariant().equals(dao.getNumberVariant())) {
                    box.setVariantContainer(variantBox);
                }
            }
            descriptionBoxList.add(box);
        }

        return descriptionBoxList;

    }

    private List<Container> setContainer (OrderContainerDao orderDao, List<VariantContainer> variantContainerList) {
        List<Container> containerList = new LinkedList<>();
        for (ContainerDao dao : orderDao.getContainers()) {
            Container box = new Container();
            box.setNumberContainer(dao.getNumberContainer());
            box.setStatus(dao.getStatus());
            for (VariantContainer variantBox : variantContainerList) {
                if (variantBox.getNumberVariant().equals(dao.getNumberVariant())) {
                    box.setVariantContainer(variantBox);
                }
            }
            containerList.add(box);
        }
        return containerList;
    }

}
