package com.efeiyi.ec.website.product.service;

import com.efeiyi.ec.model.ProductModel;
import com.efeiyi.ec.model.CartProduct;
import com.efeiyi.ec.website.base.util.ApplicationException;
import com.efeiyi.ec.website.product.model.RequestParamModel;

/**
 * Created by Administrator on 2016/12/5 0005.
 */
public interface ProductManager {

    ProductModel getProductModelById(String id) throws ApplicationException;

    CartProduct saveCartProduct(ProductModel productModel, Integer amount) throws ApplicationException;

    Object searchProductModels(RequestParamModel requestParamModel) throws Exception;

    Object getProductCategory() throws Exception;

    Object getTenantGroup(RequestParamModel requestParamModel) throws Exception;

    Object searchProductModelsByGroup(RequestParamModel requestParamModel) throws Exception;


}
