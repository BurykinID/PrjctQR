package com.qr.app.backend.entity.order.box;

import com.qr.app.backend.Json.box.OrderDao;
import lombok.*;

import javax.persistence.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Entity
@Table(name = "ord")
@Getter
@Setter
@Data
@NoArgsConstructor
public class Order {

    @Id
    @Column(unique = true)
    private String number;
    private long date;
    private String status;

    @OneToMany(mappedBy = "order")
    private List<VariantBox> variantBoxes;

    public Order(OrderDao orderDao) throws ParseException {
        this.number = orderDao.getNumber();
        Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(orderDao.getDate());
        this.date = date.getTime();
        if (orderDao.getStatus() != null && !orderDao.getStatus().equals(""))
            this.status = orderDao.getStatus();
    }

    public void updateOrder(String newDate, String status) throws ParseException {
        Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(newDate);
        this.date = date.getTime();
        this.status = status;
        variantBoxes = new LinkedList<>();
    }



}
