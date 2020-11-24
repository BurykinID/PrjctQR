package com.qr.app.backend.entity.order.container;

import com.qr.app.backend.Json.box.dao.BoxDao;
import com.qr.app.backend.Json.container.dao.ContainerDao;
import com.qr.app.backend.Json.container.dao.DescriptionContainerDao;
import com.qr.app.backend.Json.container.dao.VariantContainerDao;
import com.qr.app.backend.entity.AbstractEntity;
import com.qr.app.backend.entity.order.box.Order;
import com.qr.app.backend.entity.order.box.VariantBox;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

@Entity
@Getter
@Setter
@Data
public class VariantContainer extends AbstractEntity {

    @Column (unique = true)
    private String numberVariant;
    private int countInBox;
    private int countBox;

    @OneToMany (mappedBy = "variantContainer")
    private List<DescriptionContainer> descriptionContainers;

    @OneToMany(mappedBy = "variantContainer")
    private List<Container> containers;

    @ManyToOne
    @JoinColumn(name = "order_container")
    private OrderContainer orderContainer;

    public VariantContainer() {
        this.numberVariant = "";
        this.countBox = 0;
        this.countInBox = 0;
        this.descriptionContainers = new LinkedList<>();
        this.containers = new LinkedList<>();
        this.orderContainer = new OrderContainer();
    }

    public VariantContainer(VariantContainerDao dao, OrderContainer orderContainer) {
        this.numberVariant = dao.getNumberVariant();
        this.countInBox = dao.getCountInBox();
        this.countBox = dao.getCountBox();
        this.orderContainer = orderContainer;
    }

    public VariantContainer selectVariantContainerForDescriptionContainer(List<VariantContainer> variantContainerList, DescriptionContainerDao containerDao) {
        for (VariantContainer variantContainer : variantContainerList) {
            if (variantContainer.getNumberVariant().equals(containerDao.getNumberVariant())) {
                return variantContainer;
            }
        }
        return new VariantContainer();
    }

    public VariantContainer selectVariantContainerForContainer (List<VariantContainer> variantContainerList, ContainerDao dao) {
        for (VariantContainer variantContainer : variantContainerList) {
            if (variantContainer.getNumberVariant().equals(dao.getNumberVariant())) {
                return variantContainer;
            }
        }
        return new VariantContainer();
    }

}
