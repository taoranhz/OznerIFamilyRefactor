package com.ozner.cup.Device.ROWaterPurifier.view;

/**
 * Created by taoran on 2017/6/2.
 * 充值订单信息
 */

public class RechargeDatas {


    //private String orderId,productName,orderDtlId,productId,limitTimes,buyQuantity,actualQuantity,orginOrderCode;
    private String OrderId;
    private String ProductName;
    private String OrderDtlId;
    private String ProductId;
    private int LimitTimes;//水卡月数
    private int isRecord;//1,已使用 0 没有使用
    private int ActualQuantity;//已使用的卡数
    private int BuyQuantity;//未使用的卡数
    private String OrginOrderCode;//水机关联字段
    private String type;
    private String UCode;
    private String mac;
    private int Days;//水卡可使用天数

    public int getDays() {
        return Days;
    }

    public void setDays(int days) {
        Days = days;
    }

    public String getOrderId() {
        return OrderId;
    }

    public void setOrderId(String orderId) {
        OrderId = orderId;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public String getOrderDtlId() {
        return OrderDtlId;
    }

    public void setOrderDtlId(String orderDtlId) {
        OrderDtlId = orderDtlId;
    }

    public String getProductId() {
        return ProductId;
    }

    public void setProductId(String productId) {
        ProductId = productId;
    }

    public int getLimitTimes() {
        return LimitTimes;
    }

    public void setLimitTimes(int limitTimes) {
        LimitTimes = limitTimes;
    }

    public int getIsRecord() {
        return isRecord;
    }

    public void setIsRecord(int isRecord) {
        this.isRecord = isRecord;
    }

    public int getActualQuantity() {
        return ActualQuantity;
    }

    public void setActualQuantity(int actualQuantity) {
        ActualQuantity = actualQuantity;
    }

    public int getBuyQuantity() {
        return BuyQuantity;
    }

    public void setBuyQuantity(int buyQuantity) {
        BuyQuantity = buyQuantity;
    }

    public String getOrginOrderCode() {
        return OrginOrderCode;
    }

    public void setOrginOrderCode(String orginOrderCode) {
        OrginOrderCode = orginOrderCode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getuCode() {
        return UCode;
    }

    public void setuCode(String uCode) {
        this.UCode = uCode;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
}
