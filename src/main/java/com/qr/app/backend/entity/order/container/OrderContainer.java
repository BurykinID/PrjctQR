package com.qr.app.backend.entity.order.container;

import com.qr.app.backend.Json.container.OrderContainerDao;
import com.qr.app.backend.entity.AbstractEntity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Entity
@Table (name = "ord_pack")
@Getter
@Setter
@Data
public class OrderContainer {

    @Id
    @Column (unique = true)
    private String number;
    private long date;
    private String status;

    @OneToMany (mappedBy = "orderContainer")
    private List<VariantContainer> variantContainers;

    public OrderContainer() {
        this.number = null;
        this.date = 0;
        this.status = "";
        this.variantContainers = new LinkedList<>();
    }

    public OrderContainer(OrderContainerDao orderContainerDao) throws ParseException {
        this.number = orderContainerDao.getNumber();
        Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(orderContainerDao.getDate());
        this.date = date.getTime();
        this.status = orderContainerDao.getStatus();
    }

    public void updateContainer(String time, String status) throws ParseException {
        Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
        this.date = date.getTime();
        this.status = status;
    }


}
