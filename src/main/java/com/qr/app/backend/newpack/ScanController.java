package com.qr.app.backend.newpack;

import com.qr.app.backend.TypeQR;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ScanController {

    public static TypeQR checkTypeReadQr(String readQr) {
        if ((readQr.length() == 18 && readQr.charAt(0) == '2') ||
            (readQr.length() == 20 && readQr.charAt(0) == '0' && readQr.charAt(2) == '2')) {
            return TypeQR.Container;
        }
        else if (readQr.length() > 31) {
            return TypeQR.Mark;
        }
        else if (readQr.length() == 18 ||
                 readQr.length() == 20) {
            return TypeQR.Box;
        }
        return TypeQR.IllegalMark;
    }


}