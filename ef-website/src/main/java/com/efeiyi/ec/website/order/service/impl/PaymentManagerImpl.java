package com.efeiyi.ec.website.order.service.impl;

import com.efeiyi.ec.model.Coupon;
import com.efeiyi.ec.model.PurchaseOrder;
import com.efeiyi.ec.model.PurchaseOrderPayment;
import com.efeiyi.ec.model.PurchaseOrderPaymentDetails;
import com.efeiyi.ec.website.order.service.PaymentManager;
import com.ming800.core.base.service.BaseManager;
import com.ming800.core.p.service.AutoSerialManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Administrator on 2015/8/3.
 */
@Service
public class PaymentManagerImpl implements PaymentManager {

    @Autowired
    private BaseManager baseManager;

    @Autowired
    private AutoSerialManager autoSerialManager;



    @Override
    public PurchaseOrderPaymentDetails initPurchaseOrderPayment(PurchaseOrder purchaseOrder) throws Exception {
        PurchaseOrderPayment purchaseOrderPayment = new PurchaseOrderPayment();
        purchaseOrderPayment.setStatus("1");
        purchaseOrderPayment.setCreateDateTime(new Date());
        purchaseOrderPayment.setPaymentAmount(purchaseOrder.getTotal());
        purchaseOrderPayment.setPurchaseOrder(purchaseOrder);
        purchaseOrderPayment.setPayWay(purchaseOrder.getPayWay());
        purchaseOrderPayment.setSerial(autoSerialManager.nextSerial("payment"));
        purchaseOrderPayment.setUser(purchaseOrder.getUser());
        baseManager.saveOrUpdate(PurchaseOrderPayment.class.getName(), purchaseOrderPayment);
        PurchaseOrderPaymentDetails purchaseOrderPaymentDetails = new PurchaseOrderPaymentDetails();
        purchaseOrderPaymentDetails.setMoney(purchaseOrder.getTotal());
        purchaseOrderPaymentDetails.setPayWay(purchaseOrder.getPayWay());
        purchaseOrderPaymentDetails.setPurchaseOrderPayment(purchaseOrderPayment);
        baseManager.saveOrUpdate(PurchaseOrderPaymentDetails.class.getName(), purchaseOrderPaymentDetails);
        return purchaseOrderPaymentDetails;
    }

    //初始化订单支付，包括支付记录，支付记录详情等
    @Override
    public PurchaseOrderPaymentDetails initPurchaseOrderPayment(PurchaseOrder purchaseOrder, String balance, String couponId) throws Exception {
        Float balance1 = Float.parseFloat(balance);
        PurchaseOrderPayment purchaseOrderPayment = new PurchaseOrderPayment();
        purchaseOrderPayment.setStatus("1");
        purchaseOrderPayment.setCreateDateTime(new Date());
        purchaseOrderPayment.setPaymentAmount(purchaseOrder.getTotal());
        purchaseOrderPayment.setPurchaseOrder(purchaseOrder);
        //purchaseOrderPayment.setPayWay(purchaseOrder.getPayWay());
        purchaseOrderPayment.setSerial(autoSerialManager.nextSerial("payment"));
        purchaseOrderPayment.setUser(purchaseOrder.getUser());
        baseManager.saveOrUpdate(PurchaseOrderPayment.class.getName(), purchaseOrderPayment);
        //支付详情
        Coupon coupon = null;
        PurchaseOrderPaymentDetails purchaseOrderPaymentDetails = new PurchaseOrderPaymentDetails();
        if (null != couponId && !"".equals(couponId)) {
            if (balance1 > 0) {
                purchaseOrderPaymentDetails.setMoney((purchaseOrder.getTotal().subtract(new BigDecimal(coupon.getCouponBatch().getPrice()))).subtract(new BigDecimal(balance)));
            } else {
                purchaseOrderPaymentDetails.setMoney(purchaseOrder.getTotal().subtract(new BigDecimal(coupon.getCouponBatch().getPrice())));
            }
        } else {
            if (balance1 > 0) {
                purchaseOrderPaymentDetails.setMoney(purchaseOrder.getTotal().subtract(new BigDecimal(balance)));
            } else {
                purchaseOrderPaymentDetails.setMoney(purchaseOrder.getTotal());
            }
        }
        purchaseOrderPaymentDetails.setPayWay(purchaseOrder.getPayWay()
        );
        purchaseOrderPaymentDetails.setPurchaseOrderPayment(purchaseOrderPayment);
        baseManager.saveOrUpdate(PurchaseOrderPaymentDetails.class.getName(), purchaseOrderPaymentDetails);
        return purchaseOrderPaymentDetails;
    }
}
