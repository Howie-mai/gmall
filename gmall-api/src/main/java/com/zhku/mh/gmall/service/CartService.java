package com.zhku.mh.gmall.service;

import com.zhku.mh.gmall.bean.OmsCartItem;

import java.util.List;

/**
 * ClassName：
 * Time：2020/7/5 15:42
 * Description：
 * Author： mh
 */
public interface CartService {
    OmsCartItem ifCartExistByUser(String memberId, String skuId);

    void addCart(OmsCartItem omsCartItem);

    void updateCart(OmsCartItem omsCartItem);

    List<OmsCartItem> flushCartCache(String memberId);

    List<OmsCartItem> getCartList(String memberId);

    List<OmsCartItem> checkCart(OmsCartItem omsCartItem);
}
