package com.zhku.mh.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.zhku.mh.gmall.bean.PmsProductImage;
import com.zhku.mh.gmall.bean.PmsProductInfo;
import com.zhku.mh.gmall.bean.PmsProductSaleAttr;
import com.zhku.mh.gmall.bean.PmsProductSaleAttrValue;
import com.zhku.mh.gmall.manage.mapper.PmsProductImageMapper;
import com.zhku.mh.gmall.manage.mapper.PmsProductInfoMapper;
import com.zhku.mh.gmall.manage.mapper.PmsProductSaleAttrMapper;
import com.zhku.mh.gmall.manage.mapper.PmsProductSaleAttrValueMapper;
import com.zhku.mh.gmall.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * ClassName：
 * Time：2019/12/19 0:08
 * Description：
 * Author： mh
 */
@Service
public class SpuServiceImpl implements SpuService {
    @Autowired
    private PmsProductInfoMapper productInfoMapper;

    @Autowired
    private PmsProductImageMapper productImageMapper;

    @Autowired
    private PmsProductSaleAttrMapper productSaleAttrMapper;

    @Autowired
    private PmsProductSaleAttrValueMapper productSaleAttrValueMapper;

    @Override
    public List<PmsProductInfo> querySpuList(String catalog3Id) {
        PmsProductInfo productInfo = new PmsProductInfo();
        productInfo.setCatalog3Id(catalog3Id);
        return productInfoMapper.select(productInfo);
    }

    @Override
    public void saveSpuInfo(PmsProductInfo productInfo) {

        productInfoMapper.insertSelective(productInfo);

        String productId = productInfo.getId();

        /**
         * 插入图片
         */
        for(PmsProductImage image : productInfo.getSpuImageList()){
            image.setProductId(productId);
            productImageMapper.insertSelective(image);
        }

        for(PmsProductSaleAttr attr : productInfo.getSpuSaleAttrList()){
            attr.setProductId(productId);
            productSaleAttrMapper.insertSelective(attr);

            for(PmsProductSaleAttrValue attrValue : attr.getSpuSaleAttrValueList()){
                attrValue.setProductId(productId);
                productSaleAttrValueMapper.insertSelective(attrValue);
            }
        }
    }

    @Override
    public List<PmsProductImage> getSpuImageList(String spuId) {
        PmsProductImage pmsProductImage = new PmsProductImage();
        pmsProductImage.setProductId(spuId);
        return productImageMapper.select(pmsProductImage);
    }

    @Override
    public List<PmsProductSaleAttr> getSpuSaleAttrList(String spuId) {
        PmsProductSaleAttr attr = new PmsProductSaleAttr();
        attr.setProductId(spuId);
        List<PmsProductSaleAttr> attrList = productSaleAttrMapper.select(attr);

        for (PmsProductSaleAttr pmsProductSaleAttr : attrList) {
            String saleAttrId = pmsProductSaleAttr.getSaleAttrId();
            PmsProductSaleAttrValue attrValue = new PmsProductSaleAttrValue();
            attrValue.setSaleAttrId(saleAttrId);
            attrValue.setProductId(spuId);
            List<PmsProductSaleAttrValue> attrValueList = productSaleAttrValueMapper.select(attrValue);
            pmsProductSaleAttr.setSpuSaleAttrValueList(attrValueList);
        }
        return attrList;
    }

    @Override
    public List<PmsProductSaleAttr> getSpuSaleAttrListCheckBySku(String productId,String skuId) {
//       PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
//       pmsProductSaleAttr.setProductId(productId);
//       List<PmsProductSaleAttr> list = productSaleAttrMapper.select(pmsProductSaleAttr);
//
//        for (PmsProductSaleAttr attr : list) {
//            PmsProductSaleAttrValue saleAttrValue = new PmsProductSaleAttrValue();
//            saleAttrValue.setSaleAttrId(attr.getSaleAttrId());
//            saleAttrValue.setProductId(productId);
//            List<PmsProductSaleAttrValue> valueList = productSaleAttrValueMapper.select(saleAttrValue);
//            attr.setSpuSaleAttrValueList(valueList);
//        }

        return productSaleAttrMapper.getSpuSaleAttrListCheckBySku(productId,skuId);
    }


}
