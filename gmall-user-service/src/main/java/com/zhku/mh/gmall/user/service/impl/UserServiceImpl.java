package com.zhku.mh.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.zhku.mh.gmall.bean.UmsMember;
import com.zhku.mh.gmall.bean.UmsMemberReceiveAddress;
import com.zhku.mh.gmall.service.UserService;
import com.zhku.mh.gmall.service.util.RedisUtil;
import com.zhku.mh.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.zhku.mh.gmall.user.mapper.UserMapper;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

/**
 * ClassName：
 * Time：2019/12/4 22:03
 * Description：
 * Author： mh
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;

    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<UmsMember> getAllUser() {

        return userMapper.selectAll();

    }

    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId) {

        // 封装的参数对象
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(memberId);

        return umsMemberReceiveAddressMapper.select(umsMemberReceiveAddress);
    }

    @Override
    public UmsMember login(UmsMember umsMember) {
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();

            if(jedis != null){
                String umsMemberStr = jedis.get("user:" + umsMember.getPassword() + "_" + umsMember.getUsername() + ":info");

                if(StringUtils.isNotBlank(umsMemberStr)){

                    return JSON.parseObject(umsMemberStr, UmsMember.class);
                }else {
                    // 密码错误 或者 缓存莫得
                    // 查询数据库
                    UmsMember umsMemberDb = loginFromDb(umsMember);

                    if(umsMemberDb != null){
                        jedis.setex("user:" + umsMember.getPassword() + "_" + umsMember.getUsername() + ":info",
                                60 * 60 * 24,JSON.toJSONString(umsMemberDb));


                    }

                    return umsMemberDb;
                }
            }else {
                // 查询数据库
            }

        }finally {
            jedis.close();
        }


        return null;
    }

    @Override
    public void addUserToken(String token,String userId) {
        Jedis jedis = redisUtil.getJedis();

        jedis.setex("user:" + userId + ":token",60 * 60 * 2 , token);

        jedis.close();
    }

    @Override
    public void addOuthUser(UmsMember member) {
        userMapper.insert(member);
    }

    @Override
    public UmsMember selectOneByUmsMember(UmsMember member) {
        return userMapper.selectOne(member);
    }

    private UmsMember loginFromDb(UmsMember umsMember) {
        List<UmsMember> umsMembers = userMapper.select(umsMember);

        if(umsMembers!=null){
            return umsMembers.get(0);
        }

        return null;
    }
}
