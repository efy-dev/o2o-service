package com.efeiyi.ec.website.order.service;

import com.efeiyi.ec.model.PurchaseOrder;
import com.efeiyi.ec.model.PurchaseOrderPaymentDetails;

/**
 * Created by Administrator on 2015/8/3.
 */
public interface PaymentManager {

    PurchaseOrderPaymentDetails initPurchaseOrderPayment(PurchaseOrder purchaseOrder) throws Exception ;

    PurchaseOrderPaymentDetails initPurchaseOrderPayment(PurchaseOrder purchaseOrder, String balance, String couponId) throws Exception ;

}
