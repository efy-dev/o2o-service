package com.efeiyi.ec.website.order.service;

import com.efeiyi.ec.model.ConsumerAddress;
import com.efeiyi.ec.model.ProductModel;
import com.efeiyi.ec.model.Cart;
import com.efeiyi.ec.model.CartProduct;
import com.efeiyi.ec.model.Invoice;
import com.efeiyi.ec.model.PurchaseOrder;
import com.efeiyi.ec.model.Tenant;
import com.efeiyi.ec.website.base.util.ApplicationException;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2015/10/21 0021.
 */
public interface PurchaseOrderManager {


    /**
     * 单品下单入口
     *
     * @param productModel 商品
     * @param price        单价
     * @param amount       数量
     * @param model
     * @return
     * @throws Exception
     */
    PurchaseOrder saveOrUpdatePurchaseOrder(ProductModel productModel, BigDecimal price, int amount, Model model) throws Exception;

    /**
     * 确认订单的接口，修改订单的状态 在个人中心可见，判断支付方式跳转到相应的支付环境
     *
     * @param purchaseOrder
     * @param consumerAddress
     * @param messageMap
     * @param payWay
     * @return
     */
    PurchaseOrder confirmPurchaseOrder(PurchaseOrder purchaseOrder, ConsumerAddress consumerAddress, Map<String, String> messageMap, String payWay);


    /**
     * 确认订单的接口，修改订单的状态 在个人中心可见，判断支付方式跳转到相应的支付环境
     *
     * @param purchaseOrder   订单
     * @param consumerAddress 消费者收货地址
     * @param messageMap      订单留言的map
     * @param payWay          支付方式
     * @param invoice         发票
     * @return
     */
    PurchaseOrder confirmPurchaseOrder(PurchaseOrder purchaseOrder, ConsumerAddress consumerAddress, Map<String, String> messageMap, String payWay, Invoice invoice) throws ApplicationException;


    /**
     * 多个相同店铺的商品同时下单
     *
     * @param cartProductList 商品信息列表
     * @param tenant          店铺数据
     * @return 生成的订单对象
     * @throws Exception ApplicationException.SQL_ERROR
     */
    PurchaseOrder saveOrUpdatePurchaseOrder(List<CartProduct> cartProductList, Tenant tenant) throws ApplicationException;


    PurchaseOrder saveOrUpdatePurchaseOrder(PurchaseOrder purchaseOrder) throws ApplicationException;

    /**
     * 通过订单的id获得订单对象
     *
     * @param id
     * @return
     */
    PurchaseOrder getPurchaseOrderById(String id) throws ApplicationException;


}
