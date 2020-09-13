package com.qr.app.backend.Json.db;


import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
public class LockDB {

    String lock;
    String message;

}
