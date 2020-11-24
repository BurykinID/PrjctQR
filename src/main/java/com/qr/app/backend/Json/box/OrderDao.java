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


}
