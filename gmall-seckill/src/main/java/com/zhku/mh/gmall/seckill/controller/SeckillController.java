package com.zhku.mh.gmall.seckill.controller;

import com.zhku.mh.gmall.service.util.RedisUtil;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

/**
 * ClassName：
 * Time：2020/11/18 3:39 下午
 * Description：
 *
 * @author mh
 */
@RestController
public class SeckillController {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 先到先得
     */
    @RequestMapping("/secKill")
    public String secKill(String skuId){
        Jedis jedis = redisUtil.getJedis();
        RSemaphore semaphore = redissonClient.getSemaphore("sukid:" + skuId);
        // 尝试去执行
        boolean b = semaphore.tryAcquire();
        int stock = Integer.parseInt(jedis.get("skuid:" + skuId));
        if (b) {
            System.out.println("当前库存剩余数量 ：" + stock + " ，某用户抢购成功，当前抢购人数：" + (100000 - stock));
            // 用消息队列发出订单消息

        }else {
            System.out.println("某用户抢购失败" + stock);
        }
        jedis.close();
        return "1";
    }

    /**
     * 随机运气，同一s里的请求里面只有一个请求成功
     */
    @RequestMapping("/kill")
    public String kill(String skuId) {
        Jedis jedis = redisUtil.getJedis();
        // 开启商品的监控
        jedis.watch("skuid:" + skuId);
        int stock = Integer.parseInt(jedis.get("skuid:" + skuId));
        if (stock > 0) {
            Transaction multi = jedis.multi();
            multi.incrBy("skuid:" + skuId, -1);
            List<Object> exec = multi.exec();
            if (!CollectionUtils.isEmpty(exec)) {
                System.out.println("当前库存剩余数量 ：" + stock + " ，某用户抢购成功，当前抢购人数：" + (100000 - stock));
                // 用消息队列发出订单消息

            }else {
                System.out.println("某用户抢购失败" + stock);
            }
        }

        jedis.close();
        return "1";
    }
}
