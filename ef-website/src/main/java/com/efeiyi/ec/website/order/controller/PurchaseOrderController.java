package com.efeiyi.ec.website.order.controller;

import com.efeiyi.ec.model.*;
import com.efeiyi.ec.website.base.util.ApplicationException;
import com.efeiyi.ec.website.base.util.AuthorizationUtil;
import com.efeiyi.ec.website.order.service.PaymentManager;
import com.efeiyi.ec.website.order.service.PurchaseOrderManager;
import com.efeiyi.ec.website.organization.service.AddressManager;
import com.efeiyi.ec.website.product.service.ProductManager;
import com.ming800.core.base.controller.BaseController;
import com.ming800.core.base.service.BaseManager;
import com.ming800.core.does.model.XSaveOrUpdate;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.efeiyi.ec.model.PurchaseOrder.orderStatusMap;
import static com.efeiyi.ec.model.PurchaseOrder.paymentTypeMap;
import static com.efeiyi.ec.model.PurchaseOrderDelivery.deliveryMap;


@Controller
@RequestMapping("/order")
public class PurchaseOrderController extends BaseController {

    private BaseManager baseManager;

    private PurchaseOrderManager purchaseOrderManager;

    private PaymentManager paymentManager;

    private ProductManager productManager;

    private AddressManager addressManager;

    @Autowired
    public PurchaseOrderController(BaseManager baseManager, PurchaseOrderManager purchaseOrderManager, PaymentManager paymentManager, ProductManager productManager, AddressManager addressManager) {
        this.baseManager = baseManager;
        this.purchaseOrderManager = purchaseOrderManager;
        this.paymentManager = paymentManager;
        this.productManager = productManager;
        this.addressManager = addressManager;
    }

    private String callbackFilter(String callback, String id) throws Exception {
        callback = URLDecoder.decode(callback, "UTF-8");
        if (callback.contains("?")) {
            callback += "&purchaseOrderId=" + id;
        } else {
            callback += "?purchaseOrderId=" + id;
        }
        return callback;
    }

    /**
     * 创建新的商品订单
     *
     * @param request productList  商品规格列表的json对象 [{"id":"100000000006","amount":"1"}]  tenantId 店铺id，如果没有改参数说明是多商铺订单，如果有改参数说明所有商品都是属于改店铺
     *                callback 支付成功之后的回调地址
     * @return JSONObject 新生成的订单
     */
    @RequestMapping({"/createNewOrder"})
    @ResponseBody
    public JSONObject createNewOrder(HttpServletRequest request) {
        String productListStr = request.getParameter("productList");
        String tenantId = request.getParameter("tenantId");
        List<CartProduct> cartProductList = new ArrayList<>();
        String callback = request.getParameter("callback");

        JSONArray productJSONArray;
        Tenant tenant;
        try {
            productListStr = URLDecoder.decode(productListStr, "UTF-8");
            tenant = (Tenant) baseManager.getObject(Tenant.class.getName(), tenantId);
            productJSONArray = JSONArray.fromObject(productListStr);
//            @TODO 当TenantId为空的时候说明是多店铺商品
        } catch (Exception e) {
            e.printStackTrace();
            ApplicationException exception = new ApplicationException(ApplicationException.PARAM_ERROR);
            return exception.toJSONObject();
        }


        try {
            for (Object productInfoJson : productJSONArray) {
                JSONObject productInfoJSONObject = (JSONObject) productInfoJson;
                ProductModel productModel = productManager.getProductModelById(productInfoJSONObject.get("id").toString());
                CartProduct cartProduct = productManager.saveCartProduct(productModel, productInfoJSONObject.getInt("amount"));
                cartProductList.add(cartProduct);
            }
        } catch (ApplicationException ae) {
            ae.printStackTrace();
            return ae.toJSONObject();
        } catch (JSONException je) {
            ApplicationException exception = new ApplicationException(ApplicationException.PARAM_ERROR);
            return exception.toJSONObject();
        }

        PurchaseOrder purchaseOrder;
        try {
            purchaseOrder = purchaseOrderManager.saveOrUpdatePurchaseOrder(cartProductList, tenant);
            User user = (User) baseManager.getObject(User.class.getName(), AuthorizationUtil.getMyUser(request).getId());
            purchaseOrder.setUser(user);
            baseManager.saveOrUpdate(PurchaseOrder.class.getName(), purchaseOrder);
        } catch (ApplicationException e) {
            e.printStackTrace();
            return e.toJSONObject();
        }

        try {
            callback = URLEncoder.encode(callback, "UTF-8");
            callback = URLDecoder.decode(callback, "UTF-8");
            if (callback.contains("?")) {
                callback += "&purchaseOrderId=" + purchaseOrder.getId();
            } else {
                callback += "?purchaseOrderId=" + purchaseOrder.getId();
            }
            purchaseOrder.setCallback(callback);
            purchaseOrderManager.saveOrUpdatePurchaseOrder(purchaseOrder);
        } catch (ApplicationException ae) {
            return ae.toJSONObject();
        } catch (Exception e) {
            return new ApplicationException(ApplicationException.PARAM_ERROR).toJSONObject();

        }

        JSONObject result = new JSONObject();
        result.put("code", "0");
        result.put("orderId", purchaseOrder.getId());
        return result;
    }

    /**
     * 确认订单状态，下一步是根据选择的支付类型进行在线支付
     *
     * @param request  invoiceName发票名 invoiceTitle发票抬头 invoiceType发票类型 paymentType 支付类型，purchaseOrderId 订单id， message JSONObject {"tenantId":"messageContent","tenantId2":"meessageContent2"} 留言，ConsumerAddressId 收货地址id
     * @param response JSONObject 已经确认的订单，由前端来判断是否
     * @return JSONObject 已经确认的订单
     */
    @RequestMapping({"/confirmOrderById"})
    @ResponseBody
    public JSONObject confirmOrderById(HttpServletRequest request, HttpServletResponse response) {
        String paymentType = request.getParameter("paymentType");
        String purchaseOrderId = request.getParameter("purchaseOrderId");
        String consumerAddressId = request.getParameter("consumerAddressId");
        String invoiceName = request.getParameter("invoiceName");
        String invoiceTitle = request.getParameter("invoiceTitle");
        String invoiceType = request.getParameter("invoiceType");
        String message = request.getParameter("message");

        PurchaseOrder purchaseOrder;
        JSONObject messageMap;
        ConsumerAddress consumerAddress;

        if (message != null && !message.equals("")) {
            messageMap = JSONObject.fromObject(message);
        } else {
            messageMap = new JSONObject();
        }

        try {
            purchaseOrder = purchaseOrderManager.getPurchaseOrderById(purchaseOrderId);
            consumerAddress = addressManager.getConsumerAddressById(consumerAddressId);
        } catch (ApplicationException ae) {
            return ae.toJSONObject();
        }

        Invoice invoice = new Invoice();
        invoice.setStatus("1");
        invoice.setName(invoiceName);
        invoice.setTitle(invoiceTitle);
        invoice.setType(invoiceType);
        invoice.setPurchaseOrder(purchaseOrder);

        try {
            purchaseOrderManager.confirmPurchaseOrder(purchaseOrder, consumerAddress, messageMap, paymentType, invoice);
        } catch (ApplicationException ae) {
            return ae.toJSONObject();
        }

        JSONObject result = new JSONObject();
        result.put("code", "0");
        result.put("purchaseOrderId", purchaseOrder.getId());
        return result;
    }

    /**
     * 通过订单的id获得订单
     *
     * @param request  purchaseOrderId  订单id
     * @param response PurchaseOrder 订单对象
     * @return PurchaseOrder 订单对象
     */
    @RequestMapping({"/getPurchaseOrderById"})
    @ResponseBody
    public JSONObject getPurchaseOrderById(HttpServletRequest request, HttpServletResponse response) {
        String purchaseOrderId = request.getParameter("purchaseOrderId");

        PurchaseOrder purchaseOrder;

        try {
            purchaseOrder = purchaseOrderManager.getPurchaseOrderById(purchaseOrderId);
        } catch (ApplicationException ae) {
            return ae.toJSONObject();
        }

        JSONObject result = new JSONObject();
        JSONObject purchaseOrderJSONObject = new JSONObject();
        JSONArray productListJSONArray = new JSONArray();
        for (PurchaseOrderProduct purchaseOrderProduct : purchaseOrder.getPurchaseOrderProductList()) {
            JSONObject productJSONObject = new JSONObject();
            productJSONObject.put("id", purchaseOrderProduct.getProductModel().getId());
            productJSONObject.put("productName", purchaseOrderProduct.getProductModel().getProduct().getName());
            productJSONObject.put("productModelName", purchaseOrderProduct.getProductModel().getName());
            productJSONObject.put("imageUrl", purchaseOrderProduct.getProductModel().getProductModel_url());
            productJSONObject.put("price", purchaseOrderProduct.getPurchasePrice().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            productJSONObject.put("amount", purchaseOrderProduct.getPurchaseAmount());
            productListJSONArray.add(productJSONObject);
        }

        JSONArray deliveryListJSONArray = new JSONArray();
        for (PurchaseOrderDelivery purchaseOrderDelivery : purchaseOrder.getPurchaseOrderDeliveryList()) {
            JSONObject deliveryJSONObject = new JSONObject();
            deliveryJSONObject.put("logisticsCompany", deliveryMap.get(purchaseOrderDelivery.getLogisticsCompany()));
            deliveryJSONObject.put("company", purchaseOrderDelivery.getLogisticsCompany());
            deliveryJSONObject.put("serial", purchaseOrderDelivery.getSerial());
            deliveryListJSONArray.add(deliveryJSONObject);
        }

        purchaseOrderJSONObject.put("id", purchaseOrder.getId());
        purchaseOrderJSONObject.put("serial", purchaseOrder.getSerial());
        purchaseOrderJSONObject.put("createDatetime", purchaseOrder.getCreateDatetime().getTime());
        purchaseOrderJSONObject.put("orderStatus", orderStatusMap.get(purchaseOrder.getOrderStatus()));
        purchaseOrderJSONObject.put("total", purchaseOrder.getTotal().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        purchaseOrderJSONObject.put("originalPrice", purchaseOrder.getOriginalPrice().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        purchaseOrderJSONObject.put("paymentType", paymentTypeMap.get(purchaseOrder.getPayWay()));
        purchaseOrderJSONObject.put("message", purchaseOrder.getMessage());
        purchaseOrderJSONObject.put("address", purchaseOrder.getPurchaseOrderAddress());
        purchaseOrderJSONObject.put("receiverName", purchaseOrder.getReceiverName());
        purchaseOrderJSONObject.put("receiverPhone", purchaseOrder.getReceiverPhone());
        purchaseOrderJSONObject.put("tenantName", purchaseOrder.getTenant().getName());
        result.put("code", "0");
        result.put("purchaseOrder", purchaseOrderJSONObject);
        result.put("productList", productListJSONArray);
        result.put("deliveryList", deliveryListJSONArray);


        return result;
    }

    /**
     * 通过运单编号查询物流信息
     *
     * @param request  serial 物流单号
     * @param response JSONObject 物流信息数据
     * @return JSONObject 物流信息数据
     */
    @RequestMapping({"/getDeliveryInfoBySerial"})
    @ResponseBody
    public JSONObject getDeliveryInfoBySerial(HttpServletRequest request, HttpServletResponse response) {
        String serial = request.getParameter("serial");
        String company = request.getParameter("company");
        String resultStr;
        try {
            resultStr = getLogistics(serial, company);
        } catch (Exception e) {
            return new ApplicationException(ApplicationException.INNER_ERROR).toJSONObject();
        }

        JSONObject result = new JSONObject();
        result.put("src", resultStr);

        return result;
    }


    /**
     * 获取物流信息
     */
    private String getLogistics(String serial, String logisticsCompany) {
        String content = "";//物流信息
        try {
            URL url = new URL("http://www.kuaidi100.com/applyurl?key=" + "f8e96a50d49ef863" + "&com=" + logisticsCompany + "&nu=" + serial);
            URLConnection con = url.openConnection();
            con.setAllowUserInteraction(false);
            InputStream urlStream = url.openStream();
            byte b[] = new byte[10000];
            int numRead = urlStream.read(b);
            content = new String(b, 0, numRead);
            while (numRead != -1) {
                numRead = urlStream.read(b);
                if (numRead != -1) {
                    String newContent = new String(b, 0, numRead, "UTF-8");
                    content += newContent;
                }
            }
            urlStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }

    private void formatPurchaseOrderMessage(String message, HashMap<String, String> messageMap) {
        if (message != null) {
            for (String messageTemp : message.split(";")) {
                if (messageTemp != null && !messageTemp.equals("")) {
                    if (messageTemp.split(":").length >= 2)
                        messageMap.put(messageTemp.split(":")[0], messageTemp.split(":")[1]);
                }
            }
        }
    }

    @RequestMapping({"/confirm/{orderId}"})
    public String orderConfirm(@PathVariable String orderId, HttpServletRequest request) throws Exception {
        //获取参数
        request.getSession().removeAttribute("cart");
        String payment = request.getParameter("payment");
        String addressId = request.getParameter("address");
        String message = request.getParameter("message");
        String balance = request.getParameter("balance");
        String couponId = request.getParameter("couponId");
        HashMap<String, String> messageMap = new HashMap<>();
        formatPurchaseOrderMessage(message, messageMap);

        PurchaseOrder purchaseOrder = purchaseOrderManager.getPurchaseOrderById(orderId);

        //订单收货地址//初始化订单状态
        ConsumerAddress consumerAddress = null;

        if (addressId != null) {
            consumerAddress = addressManager.getConsumerAddressById(addressId);
        }

        purchaseOrder = purchaseOrderManager.confirmPurchaseOrder(purchaseOrder, consumerAddress, messageMap, payment);
        //生成支付记录以及支付详情
        PurchaseOrderPaymentDetails paymentDetails = paymentManager.initPurchaseOrderPayment(purchaseOrder, balance, couponId);

        return "redirect:/order/payAdapter?paymentType=" + payment + "&paymentDetailsId=" + paymentDetails.getId();
    }


}
