package com.qr.app.backend.Json.container;

import com.qr.app.backend.Json.container.dao.ContainerDao;
import com.qr.app.backend.Json.container.dao.DescriptionContainerDao;
import com.qr.app.backend.Json.container.dao.VariantContainerDao;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Data
@NoArgsConstructor
public class OrderContainerDao {

    private String number;
    private String date;
    private String status;
    private List<VariantContainerDao> variantContainers;
    private List<DescriptionContainerDao> descriptionContainers;
    private List<ContainerDao> containers;

    public OrderContainerDao (String number, String date, String status, List<VariantContainerDao> variantContainers, List<DescriptionContainerDao> descriptionContainers, List<ContainerDao> containers) {
        this.number = number;
        this.date = date;
        this.status = status;
        this.variantContainers = variantContainers;
        this.descriptionContainers = descriptionContainers;
        this.containers = containers;
    }
}
