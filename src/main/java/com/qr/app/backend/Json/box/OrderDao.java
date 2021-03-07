package com.qr.app.backend.Json.box;

import com.qr.app.backend.Json.box.dao.BoxDao;
import com.qr.app.backend.Json.box.dao.DescriptionBoxDao;
import com.qr.app.backend.Json.box.dao.VariantBoxDao;
import lombok.*;

import java.util.List;

@Getter
@Setter
public class OrderDao {

    private String number;
    private String date;
    private String status;
    private List<VariantBoxDao> variantBoxes;
    private List<DescriptionBoxDao> descriptionBoxes;
    private List<BoxDao> boxes;

    public OrderDao () {
    }

    public OrderDao (String number, String date, String status, List<VariantBoxDao> variantBoxes, List<DescriptionBoxDao> descriptionBoxes, List<BoxDao> boxes) {
        this.number = number;
        this.date = date;
        this.status = status;
        this.variantBoxes = variantBoxes;
        this.descriptionBoxes = descriptionBoxes;
        this.boxes = boxes;
    }
}
