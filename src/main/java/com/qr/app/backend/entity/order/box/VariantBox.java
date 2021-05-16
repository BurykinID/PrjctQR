package com.qr.app.backend.entity.order.box;

import com.qr.app.backend.Json.box.dao.BoxDao;
import com.qr.app.backend.Json.box.dao.DescriptionBoxDao;
import com.qr.app.backend.Json.box.dao.VariantBoxDao;
import com.qr.app.backend.entity.AbstractEntity;
import lombok.*;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

@Entity
@Getter
@Setter
@Data
public class VariantBox extends AbstractEntity {

    @Column(unique = true)
    private String numberVariant;
    private int countInBox;
    private int countBox;

    @OneToMany(mappedBy = "variantBox", orphanRemoval = true)
    private List<DescriptionBox> descriptionBoxes;

    @OneToMany(mappedBy = "variantBox", orphanRemoval = true)
    private List<Box> boxes;

    @ManyToOne
    @JoinColumn(name = "order_number")
    private Order order;

    public VariantBox(VariantBoxDao variantBoxDao, Order order) {
        this.numberVariant = variantBoxDao.getNumberVariant();
        this.countBox = variantBoxDao.getCountBox();
        this.countInBox = variantBoxDao.getCountInBox();
        this.order = order;
    }

    public VariantBox() {
        this.numberVariant = "";
        this.countInBox = 0;
        this.countBox = 0;
        this.descriptionBoxes = new LinkedList<>();
        this.boxes = new LinkedList<>();
        this.order = new Order();
    }

    public VariantBox selectVariantBoxForDescriptionBox (List<VariantBox> variantBoxList, DescriptionBoxDao dao) {
        for (VariantBox variantBox : variantBoxList) {
            if (variantBox.getNumberVariant().equals(dao.getNumberVariant())) {
                return variantBox;
            }
        }
        return new VariantBox();
    }

    public VariantBox selectVariantBoxForBox (List<VariantBox> variantBoxList, BoxDao dao) {
        for (VariantBox variantBox : variantBoxList) {
            if (variantBox.getNumberVariant().equals(dao.getNumberVariant())) {
                return variantBox;
            }
        }
        return new VariantBox();
    }

}
