package com.eiadmreh.uploadimagetest;

public class Tool {
    private String tName;
    private long tCode;
    private double tPrice;
    private String imageUrl;

    public Tool() {
    }

    public Tool(String tName, long tCode, double tPrice,String imageUrl) {
        this.tName = tName;
        this.tCode = tCode;
        this.tPrice = tPrice;
        this.imageUrl=imageUrl;
    }

    public String gettName() {
        return tName;
    }

    public void settName(String tName) {
        this.tName = tName;
    }

    public long gettCode() {
        return tCode;
    }

    public void settCode(long tCode) {
        this.tCode = tCode;
    }

    public double gettPrice() {
        return tPrice;
    }

    public void settPrice(double tPrice) {
        this.tPrice = tPrice;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public String toString() {
        return "Tool{" +
                "tName='" + tName + '\'' +
                ", tCode=" + tCode +
                ", tPrice=" + tPrice +
                '}';
    }
}
