package com.ozner.cup.Device.ROWaterPurifier.view;

/**
 * Created by taoran on 2017/6/2.
 * 充值订单信息
 */

public class RechargeDatas {


    //private String orderId,productName,orderDtlId,productId,limitTimes,buyQuantity,actualQuantity,orginOrderCode;
    private String orderId;
    private String productName;
    private String orderDtlId;
    private String productId;
    private int limitTimes;//水卡月数
    private int isRecord;//1,已使用 0 没有使用
    private int actualQuantity;//已使用的卡数
    private int buyQuantity;//未使用的卡数
    private int orginOrderCode;//水机关联字段
    private String type;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getOrderDtlId() {
        return orderDtlId;
    }

    public void setOrderDtlId(String orderDtlId) {
        this.orderDtlId = orderDtlId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getLimitTimes() {
        return limitTimes;
    }

    public void setLimitTimes(int limitTimes) {
        this.limitTimes = limitTimes;
    }

    public int getIsRecord() {
        return isRecord;
    }

    public void setIsRecord(int isRecord) {
        this.isRecord = isRecord;
    }

    public int getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(int actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    public int getBuyQuantity() {
        return buyQuantity;
    }

    public void setBuyQuantity(int buyQuantity) {
        this.buyQuantity = buyQuantity;
    }

    public int getOrginOrderCode() {
        return orginOrderCode;
    }

    public void setOrginOrderCode(int orginOrderCode) {
        this.orginOrderCode = orginOrderCode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
