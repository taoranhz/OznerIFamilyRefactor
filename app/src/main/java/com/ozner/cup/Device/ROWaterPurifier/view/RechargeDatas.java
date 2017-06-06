package com.ozner.cup.Device.ROWaterPurifier.view;

/**
 * Created by taoran on 2017/6/2.
 * 充值订单信息
 */

public class RechargeDatas {
    private String orderId;
    private String productName;
    private String orderDtlId;
    private String productId;
    private int isRecord;//1,已使用 0 没有使用
    private int useCount;//已使用的卡数
    private int noUseCount;//未使用的卡数
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getNoUseCount() {
        return noUseCount;
    }

    public void setNoUseCount(int noUseCount) {
        this.noUseCount = noUseCount;
    }

    public int getUseCount() {

        return useCount;
    }

    public void setUseCount(int useCount) {
        this.useCount = useCount;
    }
    //    private List<Integer>


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

    public int getIsRecord() {
        return isRecord;
    }

    public void setIsRecord(int isRecord) {
        this.isRecord = isRecord;
    }


}
