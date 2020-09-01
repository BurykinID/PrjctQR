package com.qr.app.backend.Json.get;

public class MarkJson {

    private String cis;
    private String numberBox;
    private String barcode;

    public MarkJson () {
    }

    public MarkJson (String cis, String numberBox, String barcode) {
        this.cis = cis;
        this.numberBox = numberBox;
        this.barcode = barcode;
    }

    public String getCis () {
        return cis;
    }

    public void setCis (String cis) {
        this.cis = cis;
    }

    public String getNumberBox () {
        return numberBox;
    }

    public void setNumberBox (String numberBox) {
        this.numberBox = numberBox;
    }

    public String getBarcode () {
        return barcode;
    }

    public void setBarcode (String barcode) {
        this.barcode = barcode;
    }

}
