package com.qr.app.backend.Json.get;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Data
@NoArgsConstructor
public class JsonReturn {

    private String resultRequest;
    private MarkJsonCheck mark;

    public JsonReturn(String resultRequest, MarkJsonCheck mark) {
        this.resultRequest = resultRequest;
        this.mark = mark;
    }

}
