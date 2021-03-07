package com.qr.app;

import com.qr.app.backend.Json.box.OrderDao;
import com.qr.app.backend.Json.box.dao.BoxDao;
import com.qr.app.backend.Json.box.dao.DescriptionBoxDao;
import com.qr.app.backend.Json.box.dao.VariantBoxDao;
import com.qr.app.backend.Json.container.OrderContainerDao;
import com.qr.app.backend.Json.container.dao.ContainerDao;
import com.qr.app.backend.Json.container.dao.DescriptionContainerDao;
import com.qr.app.backend.Json.container.dao.VariantContainerDao;
import com.qr.app.backend.Json.db.LockDB;
import com.qr.app.backend.controllers.post.*;
import com.qr.app.backend.entity.Good;
import com.qr.app.backend.entity.Mark;
import com.qr.app.backend.repository.GoodRepository;
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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.LinkedList;
import java.util.List;

/*@RunWith (SpringRunner.class)
@SpringBootTest
public class PostController {

    @Autowired
    private MarkRepository markRepository;
    @Autowired
    private GoodRepository goodRepository;
    @Autowired
    private StateDBRepository stateDBRepository;
    @Autowired
    private OrderContainerRepository orderContainerRepository;
    @Autowired
    private VariantsContainerRepository variantsContainerRepository;
    @Autowired
    private DescriptionContainerRepository descriptionContainerRepository;
    @Autowired
    private ContainerRepository containerRepository;
    @Autowired
    private BoxRepository boxRepository;
    @Autowired
    private DescriptionBoxRepository descriptionBoxRepository;
    @Autowired
    private VariantsBoxRepository variantsBoxRepository;
    @Autowired
    private OrderRepository orderRepository;

    @Test
    public void insertMark() {

        int initSize = markRepository.findAll().size();

        PostMarkController postMarkController = new PostMarkController(markRepository);
        List<Mark> marks = new LinkedList<>();
        marks.add(new Mark("010290000076453821__\"\">)FFA4L!xe","2900000764538","0","",0));
        marks.add(new Mark("010290000076453821__\'c:yR2Mi9pK,", "2900000764538","0","756fec77-1888-41f2-bd6d-b6e9424a40cb", 0));
        marks.add(new Mark("010290000076453821__+E<iNUmIThS","2900000764538","0","",0));
        marks.add(new Mark("010290000076453821__6tCkOfP+O_M","2900000764538","0","",0));
        marks.add(new Mark("010290000076453821__8qDGLHjn6AQ","2900000764538","0","",0));
        postMarkController.insertMark(marks);

        Assert.assertEquals(markRepository.findAll().size()-initSize, 0);

    }

    @Test
    public void updateMark() {
        int initSize = markRepository.findAll().size();

        PostMarkController postMarkController = new PostMarkController(markRepository);
        List<Mark> marks = new LinkedList<>();
        marks.add(new Mark("010290000076453821__\"\">)FFA4L!xe","2900000764538","1","",0));
        marks.add(new Mark("010290000076453821__\'c:yR2Mi9pK,", "2900000764538","1","756fec77-1888-41f2-bd6d-b6e9424a40cb", 0));
        marks.add(new Mark("010290000076453821__+E<iNUmIThS","2900000764538","1","",0));
        marks.add(new Mark("010290000076453821__6tCkOfP+O_M","2900000764538","1","",0));
        marks.add(new Mark("010290000076453821__8qDGLHjn6AQ","2900000764538","1","",0));
        postMarkController.insertMark(marks);

        Assert.assertEquals(markRepository.findAll().size()-initSize, 0);
    }

    @Test
    public void insertGoods() {
        List<Good> goods = new LinkedList<>();
        for(int i = 0; i < 100; i++) {
            goods.add(new Good("123123"+i, "123123", "Синий", "Тест", String.valueOf(i*35)));
        }
        PostGoodController postGoodController = new PostGoodController(goodRepository);
        postGoodController.insertGoods(goods);
        Assert.assertEquals(goods.size(), goodRepository.findAll().size());
    }

    @Test
    public void updateGoods() {
        int initSize = goodRepository.findAll().size();

        List<Good> goods = new LinkedList<>();
        for(int i = 0; i < 100; i++) {
            goods.add(new Good("123123"+i, "123123", "Синий", "Тест", String.valueOf(i*35)));
        }
        PostGoodController postGoodController = new PostGoodController(goodRepository);
        postGoodController.updateGoods(goods);
        Assert.assertEquals(goodRepository.findAll().size()-initSize, 0);
    }

    @Test
    public void insertContainer() {
        List<OrderContainerDao> orderContainerDao = new LinkedList<>();

        for (int i = 0; i < 100; i++) {

            List<VariantContainerDao> variantContainerDao = new LinkedList<>();
            List<DescriptionContainerDao> descriptionContainerDao = new LinkedList<>();
            List<ContainerDao> containerDao = new LinkedList<>();

            for (int j = 0; j < 100;j++) {
                VariantContainerDao variant = new VariantContainerDao();
                variantContainerDao.add(variant);
            }

            for (int j = 0; j < 100; j++) {
                DescriptionContainerDao description = new DescriptionContainerDao();
                descriptionContainerDao.add(description);
            }

            for (int j = 0; j < 100; j++) {
                ContainerDao container = new ContainerDao();
                containerDao.add(container);
            }

            OrderContainerDao order = new OrderContainerDao("1"+i, "0", "", variantContainerDao, descriptionContainerDao, containerDao);
            orderContainerDao.add(order);
        }

        PostContainerController postController = new PostContainerController(orderContainerRepository, variantsContainerRepository, descriptionContainerRepository, containerRepository);
        postController.insertContainers(orderContainerDao);
        Assert.assertEquals(orderContainerRepository.findAll().size(), 0);
    }
*//*
    @Test
    public void updateContainer() {
        List<OrderContainerDao> orderContainerDao = new LinkedList<>();

        for (int i = 0; i < 100; i++) {

            List<VariantContainerDao> variantContainerDao = new LinkedList<>();
            List<DescriptionContainerDao> descriptionContainerDao = new LinkedList<>();
            List<ContainerDao> containerDao = new LinkedList<>();

            for (int j = 0; j < 100;j++) {
                VariantContainerDao variant = new VariantContainerDao();
                variantContainerDao.add(variant);
            }

            for (int j = 0; j < 100; j++) {
                DescriptionContainerDao description = new DescriptionContainerDao();
                descriptionContainerDao.add(description);
            }

            for (int j = 0; j < 100; j++) {
                ContainerDao container = new ContainerDao();
                containerDao.add(container);
            }

            OrderContainerDao order = new OrderContainerDao("1"+i, "2019-01-07 10:00:00", "", variantContainerDao, descriptionContainerDao, containerDao);
            orderContainerDao.add(order);
        }

        PostContainerController postController = new PostContainerController(orderContainerRepository, variantsContainerRepository, descriptionContainerRepository, containerRepository);
        postController.updateContainers(orderContainerDao);
        Assert.assertEquals(orderContainerRepository.findAll().size(), 0);
    }*//*

    @Test
    public void insertOrder() {

        List<OrderDao> orderDaoList = new LinkedList<>();

        for (int i = 0; i < 100; i++) {

            List<VariantBoxDao> variantBoxes = new LinkedList<>();
            List<DescriptionBoxDao> descriptionBoxes = new LinkedList<>();
            List<BoxDao> boxes = new LinkedList<>();

            for (int j = 0; j < 100; j++) {
                VariantBoxDao variant = new VariantBoxDao("1"+j, 0, 0);
                variantBoxes.add(variant);
            }

            for (int j = 0; j < 100; j++) {
                DescriptionBoxDao description = new DescriptionBoxDao("1"+j, 1+j, "1"+j, 1+j);
                descriptionBoxes.add(description);
            }

            for (int j = 0; j < 100; j++) {
                BoxDao box = new BoxDao("1"+j, "1"+j, "");
                boxes.add(box);
            }

            OrderDao orderDao = new OrderDao("1"+i, "0", "", variantBoxes, descriptionBoxes, boxes);
            orderDaoList.add(orderDao);

        }

        PostOrderController post = new PostOrderController(boxRepository, descriptionBoxRepository, variantsBoxRepository, orderRepository);
        post.insertOrders(orderDaoList);
        Assert.assertEquals(orderRepository.findAll().size(), 0);
    }
*//*
    @Test
    public void updateOrder() {
        List<OrderDao> orderDaoList = new LinkedList<>();

        for (int i = 0; i < 100; i++) {

            List<VariantBoxDao> variantBoxes = new LinkedList<>();
            List<DescriptionBoxDao> descriptionBoxes = new LinkedList<>();
            List<BoxDao> boxes = new LinkedList<>();

            for (int j = 0; j < 100; j++) {
                VariantBoxDao variant = new VariantBoxDao("1"+j, 0, 0);
                variantBoxes.add(variant);
            }

            for (int j = 0; j < 100; j++) {
                DescriptionBoxDao description = new DescriptionBoxDao("1"+j, 1+j, "1"+j, 1+j);
                descriptionBoxes.add(description);
            }

            for (int j = 0; j < 100; j++) {
                BoxDao box = new BoxDao("1"+j, "1"+j, "");
                boxes.add(box);
            }

            OrderDao orderDao = new OrderDao("1"+i, "2019-01-07 10:00:00", "", variantBoxes, descriptionBoxes, boxes);
            orderDaoList.add(orderDao);

        }

        PostOrderController post = new PostOrderController(boxRepository, descriptionBoxRepository, variantsBoxRepository, orderRepository);
        post.updateOrders(orderDaoList);
        Assert.assertEquals(orderRepository.findAll().size(), 0);
    }*//*

    @Test
    public void lockDB() {
        LockDB lockDB = new LockDB("true", "Блокировка установлена");
        PostDBController postDBController = new PostDBController(stateDBRepository);
        Assert.assertEquals(postDBController.lockDB(lockDB).getStatusCodeValue(), 200);
    }

}*/
