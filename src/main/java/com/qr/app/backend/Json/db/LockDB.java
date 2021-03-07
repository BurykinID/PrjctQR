package com.qr.app.backend.Json.db;


import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
public class LockDB {

    String lock;
    String message;

    public LockDB (String lock, String message) {
        this.lock = lock;
        this.message = message;
    }
}
