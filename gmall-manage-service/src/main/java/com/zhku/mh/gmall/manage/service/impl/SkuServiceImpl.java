package com.zhku.mh.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.zhku.mh.gmall.bean.PmsSkuImage;
import com.zhku.mh.gmall.bean.PmsSkuInfo;
import com.zhku.mh.gmall.manage.mapper.PmsSkuAttrValueMapper;
import com.zhku.mh.gmall.manage.mapper.PmsSkuImageMapper;
import com.zhku.mh.gmall.manage.mapper.PmsSkuInfoMapper;
import com.zhku.mh.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.zhku.mh.gmall.service.SkuService;
import com.zhku.mh.gmall.service.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.UUID;

/**
 * ClassName：
 * Time：2019/12/19 0:08
 * Description：
 * Author： mh
 */
@Service
public class SkuServiceImpl implements SkuService {
    @Autowired
    private PmsSkuInfoMapper skuInfoMapper;

    @Autowired
    private PmsSkuImageMapper skuImageMapper;

    @Autowired
    private PmsSkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private PmsSkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void saveSkuInfo(PmsSkuInfo skuInfo) {
        skuInfoMapper.insertSelective(skuInfo);
        String skuId = skuInfo.getId();

        //图片
        skuInfo.getSkuImageList().forEach(item -> {
            item.setSkuId(skuId);
        });
        skuImageMapper.batchInsertSkuImage(skuInfo.getSkuImageList());

        //平台属性
        skuInfo.getSkuAttrValueList().forEach(item -> {
            item.setSkuId(skuId);
        });
        skuAttrValueMapper.batchInsertSkuAttrValue(skuInfo.getSkuAttrValueList());

        //销售属性
        skuInfo.getSkuSaleAttrValueList().forEach(item -> {
            item.setSkuId(skuId);
        });
        skuSaleAttrValueMapper.batchInsertSkuSaleAttrValue(skuInfo.getSkuSaleAttrValueList());

    }

    @Override
    public PmsSkuInfo getSkuByIdFromDB(String skuId) {

        // sku商品对象
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        PmsSkuInfo skuInfo = skuInfoMapper.selectOne(pmsSkuInfo);

        if (skuInfo == null) {
            return skuInfo;
        }

        // sku的图片集合
        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        List<PmsSkuImage> pmsSkuImages = skuImageMapper.select(pmsSkuImage);
        skuInfo.setSkuImageList(pmsSkuImages);
        return skuInfo;
    }

    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId) {
        return skuInfoMapper.selectSkuSaleAttrValueListBySpu(productId);
    }

    @Override
    public PmsSkuInfo getSkuById(String skuId) {
        System.out.println(":"+Thread.currentThread().getName()+"进入的商品详情的请求");
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();

        //链接缓存
        Jedis jedis = redisUtil.getJedis();

        //查找缓存
        String skuInfoKey = "sku:" + skuId + ":info";
        //分布式锁
        String skuLockKey = "sku:" + skuId + ":lock";
        String skuJSON = jedis.get(skuInfoKey);
        if (StringUtils.isNoneBlank(skuJSON)) {
            System.out.println(":"+Thread.currentThread().getName()+"从缓存中获取商品详情");
            pmsSkuInfo = JSON.parseObject(skuJSON, PmsSkuInfo.class);
        } else {
            //设置分布式锁 10s
            System.out.println(":"+Thread.currentThread().getName()+"发现缓存中没有，申请缓存的分布式锁："+"sku:" + skuId + ":lock");

            //给分布式锁加唯一表示token
            String token = UUID.randomUUID().toString().replace("-","");
            String ok = jedis.set(skuLockKey,token,"nx","px",10 * 1000);
            if(StringUtils.isNoneBlank(ok) && "OK".equals(ok)){
                System.out.println(":"+Thread.currentThread().getName()+"有权在10秒的过期时间内访问数据库："+"sku:" + skuId + ":lock");
                //设置成功查询数据库
                pmsSkuInfo = getSkuByIdFromDB(skuId);

                if (pmsSkuInfo != null) {
                    jedis.set(skuInfoKey, JSON.toJSONString(pmsSkuInfo));
                } else {
                    //数据库中不存在该sku
                    //穿透：利用不存在的key去攻击mysql数据库
                    //雪崩：缓存中很多key失效，导致数据库负载过重
                    //为了防止缓存穿透，null或者空字符串设置给redis
                    jedis.setex(skuInfoKey, 60 * 3, JSON.toJSONString(""));
                }

                //访问完后释放分布式锁
                String lockToken = jedis.get(skuLockKey);
                if(StringUtils.isNotBlank(lockToken)&&lockToken.equals(token)){
//                    jedis.eval("lua");可与用lua脚本，在查询到key的同时删除该key，防止高并发下的意外的发生
                    jedis.del(skuLockKey);// 用token确认删除的是自己的sku的锁
                }
                System.out.println(":"+Thread.currentThread().getName()+"使用完毕，将锁归还："+"sku:" + skuId + ":lock");
            }else {
                //设置失败，自旋（该线程在睡眠几秒后，重新尝试访问）
                try {
                    Thread.sleep(3000);
                }catch (Exception e){
                    e.printStackTrace();;
                }
                System.out.println(":"+Thread.currentThread().getName()+"没有拿到锁，开始自旋");
                return getSkuById(skuId);
            }
        }

        jedis.close();
        return pmsSkuInfo;
    }

}
