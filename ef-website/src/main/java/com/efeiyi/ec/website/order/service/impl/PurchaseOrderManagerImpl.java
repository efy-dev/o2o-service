package com.efeiyi.ec.website.order.service.impl;

import com.efeiyi.ec.model.ConsumerAddress;
import com.efeiyi.ec.model.User;
import com.efeiyi.ec.model.ProductModel;
import com.efeiyi.ec.model.*;
import com.efeiyi.ec.model.Tenant;
import com.efeiyi.ec.website.base.util.ApplicationException;
import com.efeiyi.ec.website.base.util.AuthorizationUtil;
import com.efeiyi.ec.website.order.service.PurchaseOrderManager;
import com.ming800.core.base.service.BaseManager;
import com.ming800.core.p.service.AutoSerialManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Administrator on 2015/10/21 0021.
 */
@Service
public class PurchaseOrderManagerImpl implements PurchaseOrderManager {

    @Autowired
    private BaseManager baseManager;

    @Autowired
    private AutoSerialManager autoSerialManager;


    @Override
    public PurchaseOrder saveOrUpdatePurchaseOrder(ProductModel productModel, BigDecimal price, int amount, Model model) throws Exception {
        HashMap<String, List> productMap = new HashMap<>();
        PurchaseOrder purchaseOrder = createNewPurchaseOrder(productModel, price, amount);
        LinkedHashSet<Tenant> tenantSet = new LinkedHashSet<>();
        tenantSet.add(productModel.getProduct().getTenant());
        purchaseOrder.setTenant(productModel.getProduct().getTenant());
        baseManager.saveOrUpdate(PurchaseOrder.class.getName(), purchaseOrder);
        ArrayList<PurchaseOrderProduct> productModelArrayList = new ArrayList<>();
        productModelArrayList.add(purchaseOrder.getPurchaseOrderProductList().get(0));
        productMap.put(productModel.getProduct().getTenant().getId(), productModelArrayList);
        model.addAttribute("productMap", productMap);
        model.addAttribute("purchaseOrder", purchaseOrder);
        model.addAttribute("tenantList", tenantSet);
        return purchaseOrder;
    }



    @Override
    public PurchaseOrder saveOrUpdatePurchaseOrder(List<CartProduct> cartProductList, Tenant tenant) throws ApplicationException {
        PurchaseOrder purchaseOrder;
        try {
            purchaseOrder = createNewPurchaseOrder(cartProductList);
            purchaseOrder.setTenant(tenant);
            if (tenant.getDiscount() != null && tenant.getDiscount() > 0) {
                BigDecimal total = purchaseOrder.getOriginalPrice().multiply(new BigDecimal(tenant.getDiscount() / 100.0));
                total.setScale(2, BigDecimal.ROUND_HALF_UP);
                purchaseOrder.setTotal(total);
            }
            baseManager.saveOrUpdate(PurchaseOrder.class.getName(), purchaseOrder);
        } catch (Exception e) {
            throw new ApplicationException(ApplicationException.SQL_ERROR);
        }
        return purchaseOrder;
    }


    public PurchaseOrder saveOrUpdatePurchaseOrder(PurchaseOrder purchaseOrder) throws ApplicationException {
        try {
            baseManager.saveOrUpdate(PurchaseOrder.class.getName(), purchaseOrder);
        } catch (Exception e) {
            throw new ApplicationException(ApplicationException.SQL_ERROR);
        }
        return purchaseOrder;
    }

    private PurchaseOrder createNewPurchaseOrder(List<CartProduct> cartProductList) throws Exception {
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setSerial(autoSerialManager.nextSerial("orderSerial"));
        purchaseOrder.setCreateDatetime(new Date());
        baseManager.saveOrUpdate(PurchaseOrder.class.getName(), purchaseOrder);
        List<PurchaseOrderProduct> purchaseOrderProductList = new ArrayList<>();
        BigDecimal totalPrice = new BigDecimal(0);
        if (cartProductList != null && cartProductList.size() > 0) {
            for (CartProduct cartProduct : cartProductList) {
                if (cartProduct.getIsChoose().equals("1")) {
                    PurchaseOrderProduct purchaseOrderProduct = new PurchaseOrderProduct(purchaseOrder, cartProduct.getProductModel(), cartProduct.getAmount(), cartProduct.getProductModel().getPrice());
                    totalPrice = totalPrice.add(cartProduct.getProductModel().getPrice().multiply(new BigDecimal(cartProduct.getAmount())));
                    baseManager.saveOrUpdate(PurchaseOrderProduct.class.getName(), purchaseOrderProduct);
                    purchaseOrderProductList.add(purchaseOrderProduct);
                }
            }
            totalPrice = totalPrice.setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        purchaseOrder.setTotal(totalPrice);
        purchaseOrder.setOriginalPrice(totalPrice);
        purchaseOrder.setPurchaseOrderProductList(purchaseOrderProductList);
        baseManager.saveOrUpdate(PurchaseOrder.class.getName(), purchaseOrder);
        return purchaseOrder;
    }


    private PurchaseOrder createNewPurchaseOrder(ProductModel productModel, BigDecimal price, int amount) throws Exception {
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setSerial(autoSerialManager.nextSerial("orderSerial"));
        purchaseOrder.setCreateDatetime(new Date());
        baseManager.saveOrUpdate(PurchaseOrder.class.getName(), purchaseOrder);
        BigDecimal totalPrice = new BigDecimal(0);
        PurchaseOrderProduct purchaseOrderProduct = new PurchaseOrderProduct(purchaseOrder, productModel, amount, price);
        totalPrice = totalPrice.add(price.multiply(new BigDecimal(amount)));
        baseManager.saveOrUpdate(PurchaseOrderProduct.class.getName(), purchaseOrderProduct);
        List<PurchaseOrderProduct> purchaseOrderProductList = new ArrayList<>();
        purchaseOrderProductList.add(purchaseOrderProduct);
        purchaseOrder.setPurchaseOrderProductList(purchaseOrderProductList);
        totalPrice = totalPrice.setScale(2, BigDecimal.ROUND_HALF_UP);
        purchaseOrder.setTotal(totalPrice);
        purchaseOrder.setOriginalPrice(totalPrice);
        baseManager.saveOrUpdate(PurchaseOrder.class.getName(), purchaseOrder);
        return purchaseOrder;
    }


    @Override
    public PurchaseOrder confirmPurchaseOrder(PurchaseOrder purchaseOrder, ConsumerAddress consumerAddress, Map<String, String> messageMap, String payWay) {
        purchaseOrder.setStatus("1");
        purchaseOrder.setOrderStatus(PurchaseOrder.ORDER_STATUS_WPAY);
        purchaseOrder.setPayWay(payWay);
        if (consumerAddress != null) {
            purchaseOrder.setProvince(consumerAddress.getProvince());
            purchaseOrder.setCity(consumerAddress.getCity());
            purchaseOrder.setDistrict(consumerAddress.getDistrict());
            String purchaseOrderAddress = (consumerAddress.getProvince() != null ? consumerAddress.getProvince().getName() : "") + " " + (consumerAddress.getCity() != null ? consumerAddress.getCity().getName() : "") + " " + (consumerAddress.getDetails() != null ? consumerAddress.getDetails() : "");
            purchaseOrder.setPurchaseOrderAddress(purchaseOrderAddress);
            purchaseOrder.setReceiverName(consumerAddress.getConsignee() != null ? consumerAddress.getConsignee() : "");
            purchaseOrder.setReceiverPhone(consumerAddress.getPhone() != null ? consumerAddress.getPhone() : "");
        }
        List<PurchaseOrder> subPurchaseOrderList = purchaseOrder.getSubPurchaseOrder();
        if (subPurchaseOrderList != null && subPurchaseOrderList.size() > 1) {
            for (PurchaseOrder purchaseOrderTemp : subPurchaseOrderList) {
                purchaseOrderTemp.setStatus("1");
                purchaseOrderTemp.setOrderStatus(PurchaseOrder.ORDER_STATUS_WPAY);
                if (consumerAddress != null) {
                    String purchaseOrderAddress = (consumerAddress.getProvince() != null ? consumerAddress.getProvince().getName() : "") + " " + (consumerAddress.getCity() != null ? consumerAddress.getCity().getName() : "") + " " + (consumerAddress.getDetails() != null ? consumerAddress.getDetails() : "");
                    purchaseOrderTemp.setPurchaseOrderAddress(purchaseOrderAddress);
                    purchaseOrderTemp.setReceiverName(consumerAddress.getConsignee() != null ? consumerAddress.getConsignee() : "");
                    purchaseOrderTemp.setReceiverPhone(consumerAddress.getPhone() != null ? consumerAddress.getPhone() : "");
                }
                purchaseOrderTemp.setMessage(messageMap.get(purchaseOrderTemp.getTenant().getId() + "Message"));
                purchaseOrderTemp.setPayWay(payWay);
                baseManager.saveOrUpdate(PurchaseOrder.class.getName(), purchaseOrderTemp);
            }
        } else {
            purchaseOrder.setMessage(messageMap.get(purchaseOrder.getTenant().getId() + "Message"));
        }
        baseManager.saveOrUpdate(PurchaseOrder.class.getName(), purchaseOrder);


        return purchaseOrder;
    }


    @Override
    public PurchaseOrder confirmPurchaseOrder(PurchaseOrder purchaseOrder, ConsumerAddress consumerAddress, Map<String, String> messageMap, String payWay, Invoice invoice) throws ApplicationException {

        try {
            baseManager.saveOrUpdate(Invoice.class.getName(), invoice);
            confirmPurchaseOrder(purchaseOrder, consumerAddress, messageMap, payWay);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApplicationException(ApplicationException.SQL_ERROR);
        }

        return purchaseOrder;
    }

    @Override
    public PurchaseOrder getPurchaseOrderById(String id) throws ApplicationException {
        PurchaseOrder purchaseOrder;
        try {
            purchaseOrder = (PurchaseOrder) baseManager.getObject(PurchaseOrder.class.getName(), id);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApplicationException(ApplicationException.SQL_ERROR);
        }
        return purchaseOrder;
    }
}
