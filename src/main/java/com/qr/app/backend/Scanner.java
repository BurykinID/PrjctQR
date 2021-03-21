package com.qr.app.backend;

import com.qr.app.backend.entity.forSession.LvlEvent;
import org.atmosphere.interceptor.AtmosphereResourceStateRecovery;

public class Scanner {

    public String buildQrCode (StringBuilder sb) {
        String data = sb.toString();
        StringBuilder bufferCode = new StringBuilder();
        if (data.length() <= 255) {
            int symbol = sb.indexOf("\r\n");
            if (symbol != -1){
                bufferCode.append(data.substring(0, symbol));
            }
            else {
                bufferCode.append(data);
            }
        }

        return bufferCode.toString();

    }

}
