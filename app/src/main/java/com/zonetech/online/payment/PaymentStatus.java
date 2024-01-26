package com.zonetech.online.payment;

public class PaymentStatus {
    public String payStatus = "";
    public String pGtype = "";
    public String paymentID = "";
    public String txnid = "";
    public String bankRefNo = "";
    public String paymentMode = "";
    public String remarks = "";
    public int status;
    public long discount;

    @Override
    public String toString() {
        return "PaymentStatus{" +
                "payStatus='" + payStatus + '\'' +
                ", pGtype='" + pGtype + '\'' +
                ", paymentID='" + paymentID + '\'' +
                ", txnid='" + txnid + '\'' +
                ", bankRefNo='" + bankRefNo + '\'' +
                ", paymentMode='" + paymentMode + '\'' +
                ", remarks='" + remarks + '\'' +
                ", status=" + status +
                ", discount=" + discount +
                '}';
    }
}
