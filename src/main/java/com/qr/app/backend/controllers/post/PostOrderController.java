package com.qr.app.backend.controllers.post;

import com.qr.app.backend.Json.box.OrderDao;
import com.qr.app.backend.Json.box.dao.BoxDao;
import com.qr.app.backend.Json.box.dao.DescriptionBoxDao;
import com.qr.app.backend.Json.box.dao.VariantBoxDao;
import com.qr.app.backend.entity.order.box.Box;
import com.qr.app.backend.entity.order.box.DescriptionBox;
import com.qr.app.backend.entity.order.box.Order;
import com.qr.app.backend.entity.order.box.VariantBox;
import com.qr.app.backend.repository.order.box.BoxRepository;
import com.qr.app.backend.repository.order.box.DescriptionBoxRepository;
import com.qr.app.backend.repository.order.box.OrderRepository;
import com.qr.app.backend.repository.order.box.VariantsBoxRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

@RestController
public class PostOrderController {

    private final BoxRepository boxRepository;
    private final DescriptionBoxRepository descriptionBoxRepository;
    private final VariantsBoxRepository variantsBoxRepository;
    private final OrderRepository orderRepository;

    public PostOrderController (BoxRepository boxRepository, DescriptionBoxRepository descriptionBoxRepository, VariantsBoxRepository variantsBoxRepository, OrderRepository orderRepository) {
        this.boxRepository = boxRepository;
        this.descriptionBoxRepository = descriptionBoxRepository;
        this.variantsBoxRepository = variantsBoxRepository;
        this.orderRepository = orderRepository;
    }

    @PostMapping("/post/insertOrders")
    public ResponseEntity<String> insertOrders(@RequestBody List<OrderDao> orderDaoList) {
        long countOrdersBeforeInsert = orderRepository.count();
        List<Order> orderList = new LinkedList<>();
        List<VariantBox> variantBoxes = new LinkedList<>();
        List<DescriptionBox> descriptionBoxes = new LinkedList<>();
        List<Box> boxList = new LinkedList<>();
        for (OrderDao orderDao : orderDaoList) {
            try {
                Order order = new Order(orderDao);
                orderList.add(order);

                List<VariantBox> variantBoxList = variantListFormation(orderDao, order);
                variantBoxes.addAll(variantBoxList);

                List<DescriptionBox> descriptionBoxList = descriptionBoxFormation(orderDao, variantBoxList);
                descriptionBoxes.addAll(descriptionBoxList);

                List<Box> boxes = boxFormation(orderDao, variantBoxList);
                boxList.addAll(boxes);

            } catch (ParseException e) {
                return new ResponseEntity<>("Incorrect data format.", HttpStatus.BAD_REQUEST);
            }
        }
        saveOrderInfo(orderList, variantBoxes, descriptionBoxes, boxList);
        long countInsertInTable = orderRepository.count() - countOrdersBeforeInsert;
        return new ResponseEntity<>("Добавлено записей: " + countInsertInTable, HttpStatus.OK);
    }

    @PostMapping("/post/updateOrders")
    public ResponseEntity<String> updateOrders(@RequestBody List<OrderDao> orderDaoList) {
        long countOrdersBeforeInsert = orderRepository.count();
        List<VariantBox> variantBoxes = new LinkedList<>();
        List<DescriptionBox> descriptionBoxes = new LinkedList<>();
        List<Box> boxList = new LinkedList<>();
        for (OrderDao orderDao : orderDaoList) {
            try {
                Order order = orderRepository.findByNumber(orderDao.getNumber()).orElse(new Order());
                if (!order.getNumber().isEmpty()) {
                    order.updateOrder(orderDao.getDate(), orderDao.getStatus());
                }
                else {
                    order = new Order(orderDao);
                }
                orderRepository.save(order);

                List<VariantBox> variantBoxesForDelete = variantsBoxRepository.findByOrderNumber(order.getNumber());

                for (VariantBox varBox: variantBoxesForDelete)
                    boxRepository.deleteByVariantBox(varBox);

                for (VariantBox varBox: variantBoxesForDelete)
                    descriptionBoxRepository.deleteByNumberVariant(varBox);

                variantsBoxRepository.deleteByOrderNumber(order.getNumber());
                List<VariantBox> variantBoxList = variantListFormation(orderDao, order);
                variantBoxes.addAll(variantBoxList);

                descriptionBoxes.addAll(descriptionBoxFormation(orderDao, variantBoxList));

                boxList.addAll(boxFormation(orderDao, variantBoxList));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        variantsBoxRepository.saveAll(variantBoxes);
        descriptionBoxRepository.saveAll(descriptionBoxes);
        boxRepository.saveAll(boxList);
        long countInsertInTable = orderRepository.count() - countOrdersBeforeInsert;
        return new ResponseEntity<>("Обновлено записей: " + countInsertInTable, HttpStatus.OK);
    }

    public void saveOrderInfo (List<Order> orderList,
                               List<VariantBox> variantBoxes,
                               List<DescriptionBox> descriptionBoxes,
                               List<Box> boxList) {
        orderRepository.saveAll(orderList);
        variantsBoxRepository.saveAll(variantBoxes);
        descriptionBoxRepository.saveAll(descriptionBoxes);
        boxRepository.saveAll(boxList);
    }

    public List<VariantBox> variantListFormation(OrderDao orderDao, Order order) {
        List<VariantBox> variantBoxList = new LinkedList<>();
        for (VariantBoxDao dao : orderDao.getVariantBoxes()) {
            VariantBox box = new VariantBox(dao, order);
            variantBoxList.add(box);
        }
        return variantBoxList;
    }
    
    public List<DescriptionBox> descriptionBoxFormation(OrderDao orderDao, List<VariantBox> variantBoxList) {
        List<DescriptionBox> descriptionBoxList = new LinkedList<>();
        for (DescriptionBoxDao dao : orderDao.getDescriptionBoxes()) {
            VariantBox variantBoxForDescription = new VariantBox().selectVariantBoxForDescriptionBox(variantBoxList, dao);
            DescriptionBox box = new DescriptionBox(dao.getBarcode(), dao.getNumberLine(), dao.getCount(), variantBoxForDescription);
            descriptionBoxList.add(box);
        }
        return descriptionBoxList;
    }

    public List<Box> boxFormation(OrderDao orderDao, List<VariantBox> variantBoxList) {
        List<Box> boxes = new LinkedList<>();
        for (BoxDao dao : orderDao.getBoxes()) {
            VariantBox variantBoxForBox = new VariantBox().selectVariantBoxForBox(variantBoxList, dao);
            Box box = new Box(dao.getNumberBox(), dao.getStatus(), variantBoxForBox);
            boxes.add(box);
        }
        return boxes;
    }

}