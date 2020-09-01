package com.qr.app.backend.Json.container;

import com.qr.app.backend.Json.container.dao.BoxDao;
import com.qr.app.backend.Json.container.dao.DescriptionBoxDao;
import com.qr.app.backend.Json.container.dao.VariantBoxDao;
import lombok.*;

import java.util.List;

@Getter
@Setter
public class OrderDao {

    private int number;
    private String date;
    private String status;
    private List<VariantBoxDao> variantBoxes;
    private List<DescriptionBoxDao> descriptionBoxes;
    private List<BoxDao> boxes;


}
