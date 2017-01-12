package com.efeiyi.ec.website.order.controller;

import com.efeiyi.ec.model.PurchaseOrder;
import com.efeiyi.ec.model.PurchaseOrderPaymentDetails;
import com.efeiyi.ec.model.PurchaseOrderProduct;
import com.efeiyi.ec.website.organization.service.SmsCheckManager;
import com.ming800.core.base.service.BaseManager;
import com.ming800.core.p.PConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.util.HashMap;


@Controller
@RequestMapping({"/order"})
public class PurchaseOrderPaymentController {

    private SmsCheckManager smsCheckManager;

    private BaseManager baseManager;

    @Autowired
    public PurchaseOrderPaymentController(SmsCheckManager smsCheckManager, BaseManager baseManager) {
        this.smsCheckManager = smsCheckManager;
        this.baseManager = baseManager;
    }

    private final static String PAY_INTERFACE = "http://mall.efeiyi.com/order/payAdapter";

    /**
     * 支付适配器
     */
    @RequestMapping({"payAdapter"})
    public String payAdapter(HttpServletRequest request) throws Exception {
        String paymentType = request.getParameter("paymentType");
        String paymentDetailsId = request.getParameter("paymentDetailsId");
        String queryString = "?paymentType=" + paymentType + "&paymentDetailsId=" + paymentDetailsId + "&callback=http://" + PConst.LOCALHOST + "/order/payResultAdapter";
        queryString = URLEncoder.encode(queryString, "UTF-8");
        return "redirect:" + PAY_INTERFACE + queryString;
    }

    /**
     * 支付结果收集器
     */
    @RequestMapping({"/payResultAdapter"})
    public String payResultAdapter(HttpServletRequest request) {
        String paymentDetailsId = request.getParameter("paymentDetailsId");
        PurchaseOrderPaymentDetails paymentDetails = (PurchaseOrderPaymentDetails) baseManager.getObject(PurchaseOrderPaymentDetails.class.getName(), paymentDetailsId);
        //1.如果支付成功就跳转到支付成功链接
        //2.如果支付失败就跳转到支付失败链接
        if (paymentDetails.getTransactionNumber() != null) {
            return "redirect:/order/paySuccess/" + paymentDetailsId;
        } else {
            return "redirect:/order/paySuccess/" + paymentDetailsId;
        }
    }


    private void sendTenantSMS(String purchaseOrderSerial, String productMassge, String deliveryName, String deliveryNum, String deliveryAddress, String phone) {
        HashMap<String, String> smsParam = new HashMap<>();
        smsParam.put("purchaseOrderSerial", purchaseOrderSerial);
        smsParam.put("productMassge", productMassge);
        smsParam.put("deliveryName", deliveryName);
        smsParam.put("deliveryNum", deliveryNum);
        smsParam.put("deliveryAddress", deliveryAddress);
        smsCheckManager.send(phone, smsParam, "1216591");
        smsCheckManager.send("13671386900", smsParam, "1216591");//和坤手机号
    }


    //@TODO 暂时不使用这种模式
    @RequestMapping({"/payFailed/{orderId}"})
    public String payFailed() {
        return "";
    }


    @RequestMapping({"/paySuccess/{orderId}"})
    public String paySuccess(@PathVariable String orderId) throws Exception {
        PurchaseOrderPaymentDetails paymentDetails = (PurchaseOrderPaymentDetails) baseManager.getObject(PurchaseOrderPaymentDetails.class.getName(), orderId);
        PurchaseOrder purchaseOrder = paymentDetails.getPurchaseOrderPayment().getPurchaseOrder();
        String productMessage = "";

        //支付成功发送短信
        for (PurchaseOrder order : purchaseOrder.getSubPurchaseOrder()) {
            for (PurchaseOrderProduct product : order.getPurchaseOrderProductList()) {
                productMessage += product.getProductModel().getProduct().getName() + "(" + product.getPurchaseAmount() + ");";
            }
            sendTenantSMS(order.getId(), productMessage, order.getReceiverName(), order.getReceiverPhone(), order.getPurchaseOrderAddress(), order.getBigTenant().getPhone());
        }

        if (purchaseOrder.getTenant() != null) {
            for (PurchaseOrderProduct product : purchaseOrder.getPurchaseOrderProductList()) {
                productMessage += product.getProductModel().getProduct().getName() + "(" + product.getPurchaseAmount() + ");";
            }
            sendTenantSMS(purchaseOrder.getSerial(), productMessage, purchaseOrder.getReceiverName(), purchaseOrder.getReceiverPhone(), purchaseOrder.getPurchaseOrderAddress(), purchaseOrder.getBigTenant().getPhone());
        }


        HashMap<String, String> smsParam = new HashMap<>();
        smsParam.put("order", purchaseOrder.getSerial());
        smsCheckManager.send(purchaseOrder.getReceiverPhone(), smsParam, "1646994");

        return "redirect:/app/order_details.html?purchaseOrderId=" + purchaseOrder.getId();

    }


}
