package com.qr.app.backend.controllers.post;

import com.qr.app.backend.Json.box.OrderDao;
import com.qr.app.backend.Json.container.OrderContainerDao;
import com.qr.app.backend.Json.container.dao.ContainerDao;
import com.qr.app.backend.Json.container.dao.DescriptionContainerDao;
import com.qr.app.backend.Json.container.dao.VariantContainerDao;
import com.qr.app.backend.entity.order.box.Box;
import com.qr.app.backend.entity.order.box.DescriptionBox;
import com.qr.app.backend.entity.order.box.Order;
import com.qr.app.backend.entity.order.box.VariantBox;
import com.qr.app.backend.entity.order.container.Container;
import com.qr.app.backend.entity.order.container.DescriptionContainer;
import com.qr.app.backend.entity.order.container.OrderContainer;
import com.qr.app.backend.entity.order.container.VariantContainer;
import com.qr.app.backend.repository.order.container.ContainerRepository;
import com.qr.app.backend.repository.order.container.DescriptionContainerRepository;
import com.qr.app.backend.repository.order.container.OrderContainerRepository;
import com.qr.app.backend.repository.order.container.VariantsContainerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

@RestController
public class PostContainerController {

    private final OrderContainerRepository orderContainerRepository;
    private final VariantsContainerRepository variantsContainerRepository;
    private final DescriptionContainerRepository descriptionContainerRepository;
    private final ContainerRepository containerRepository;

    public PostContainerController (OrderContainerRepository orderContainerRepository, VariantsContainerRepository variantsContainerRepository, DescriptionContainerRepository descriptionContainerRepository, ContainerRepository containerRepository) {
        this.orderContainerRepository = orderContainerRepository;
        this.variantsContainerRepository = variantsContainerRepository;
        this.descriptionContainerRepository = descriptionContainerRepository;
        this.containerRepository = containerRepository;
    }

    @PostMapping ("post/insertContainers")
    public ResponseEntity<String> insertContainers(@RequestBody List<OrderContainerDao> containers) {
        int countMarksBeforeInsert = (int) orderContainerRepository.count();
        List<VariantContainer> variantContainers = new LinkedList<>();
        List<DescriptionContainer> descriptionContainers = new LinkedList<>();
        List<OrderContainer> orderContainers = new LinkedList<>();
        List<Container> containerList = new LinkedList<>();
        for (OrderContainerDao orderDao : containers) {
            try {
                OrderContainer orderContainer = new OrderContainer(orderDao);
                orderContainers.add(orderContainer);

                List<VariantContainer> variantContainerList = variantContainerFormation(orderDao, orderContainer);
                variantContainers.addAll(variantContainerList);

                descriptionContainers.addAll(descriptionContainerFormation(orderDao, variantContainerList));

                containerList.addAll(containerFormation(orderDao, variantContainerList));
            } catch (ParseException e) {
                return new ResponseEntity<>("Incorrect date format" + orderDao.getDate(), HttpStatus.BAD_REQUEST);
            }
        }
        saveOrderInfo(orderContainers, variantContainers, descriptionContainers, containerList);
        int countInsertInTable = (int) orderContainerRepository.count() - countMarksBeforeInsert;
        return new ResponseEntity<>("Добавлено записей: " + countInsertInTable, HttpStatus.OK);
    }

    @PostMapping ("post/updateContainers")
    public ResponseEntity<String> updateContainers(@RequestBody List<OrderContainerDao> containers) {
        List<VariantContainer> variantContainers = new LinkedList<>();
        List<DescriptionContainer> descriptionContainers = new LinkedList<>();
        List<Container> containerList = new LinkedList<>();
        for (OrderContainerDao orderContainerDao : containers) {
            try {
                OrderContainer orderContainer = orderContainerRepository.findByNumber(orderContainerDao.getNumber()).orElse(new OrderContainer());
                if (orderContainer.getNumber().isEmpty()) {
                    orderContainer.updateContainer(orderContainerDao.getDate(), orderContainerDao.getStatus());
                }
                else {
                    orderContainer = new OrderContainer(orderContainerDao);
                }
                orderContainerRepository.save(orderContainer);

                List<VariantContainer> variantContainersForDelete = variantsContainerRepository.findByOrderContainer(orderContainer.getNumber());

                for (VariantContainer varContainer: variantContainersForDelete)
                    descriptionContainerRepository.deleteByNumberVariant(varContainer);

                for (VariantContainer varContainer: variantContainersForDelete)
                    containerRepository.deleteByVariantContainer(varContainer);

                variantsContainerRepository.deleteByOrderContainerNumber(orderContainer.getNumber());
                List<VariantContainer> variantContainerList = variantContainerFormation(orderContainerDao, orderContainer);
                variantContainers.addAll(variantContainerList);

                descriptionContainers.addAll(descriptionContainerFormation(orderContainerDao, variantContainerList));

                containerList.addAll(containerFormation(orderContainerDao, variantContainerList));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        variantsContainerRepository.saveAll(variantContainers);
        descriptionContainerRepository.saveAll(descriptionContainers);
        containerRepository.saveAll(containerList);
        return new ResponseEntity<>("Обновление завершено", HttpStatus.OK);
    }

    public void saveOrderInfo (List<OrderContainer> orderContainers,
                               List<VariantContainer> variantContainers,
                               List<DescriptionContainer> descriptionContainers,
                               List<Container> containerList) {
        orderContainerRepository.saveAll(orderContainers);
        variantsContainerRepository.saveAll(variantContainers);
        descriptionContainerRepository.saveAll(descriptionContainers);
        containerRepository.saveAll(containerList);
    }

    public List<VariantContainer> variantContainerFormation(OrderContainerDao orderContainerDao, OrderContainer orderContainer) {
        List<VariantContainer> variantContainerList = new LinkedList<>();
        for (VariantContainerDao dao : orderContainerDao.getVariantContainers()) {
            VariantContainer variantContainer = new VariantContainer(dao, orderContainer);
            variantContainerList.add(variantContainer);
        }
        return variantContainerList;
    }

    public List<DescriptionContainer> descriptionContainerFormation(OrderContainerDao orderContainerDao, List<VariantContainer> variantContainerList) {
            List<DescriptionContainer> descriptionBoxList = new LinkedList<>();
            for (DescriptionContainerDao dao : orderContainerDao.getDescriptionContainers()) {
                VariantContainer variantContainerForDescription = new VariantContainer().selectVariantContainerForDescriptionContainer(variantContainerList, dao);
                DescriptionContainer descriptionContainer = new DescriptionContainer(dao.getNumberLine(), dao.getNumberVariantBox(), dao.getCount(),variantContainerForDescription);
                descriptionBoxList.add(descriptionContainer);
            }
            return descriptionBoxList;
    }

    public List<Container> containerFormation (OrderContainerDao orderContainerDao, List<VariantContainer> variantContainerList) {
        List<Container> containers = new LinkedList<>();
        for (ContainerDao dao : orderContainerDao.getContainers()) {
            VariantContainer variantContainerForContainer = new VariantContainer().selectVariantContainerForContainer(variantContainerList, dao);
            Container container = new Container(dao.getNumberContainer(), dao.getStatus(), variantContainerForContainer);
            containers.add(container);
        }
        return containers;
    }

}
