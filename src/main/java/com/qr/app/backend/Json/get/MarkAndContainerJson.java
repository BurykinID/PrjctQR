package com.qr.app.backend.Json.get;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Data
@NoArgsConstructor
public class MarkAndContainerJson {

    List<MarkJson> markJson;
    List<HierarchyOfBox> containerJson;

}
