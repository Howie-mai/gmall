package com.zhku.mh.gmall.service;

import com.zhku.mh.gmall.bean.UmsMember;
import com.zhku.mh.gmall.bean.UmsMemberReceiveAddress;

import java.util.List;

/**
 * ClassName：
 * Time：2019/12/4 22:58
 * Description：
 * Author： mh
 */
public interface UserService {
    List<UmsMember> getAllUser();

    List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId);

    UmsMember login(UmsMember umsMember);

    void addUserToken(String token, String userId);

    void addOuthUser(UmsMember member);

    UmsMember selectOneByUmsMember(UmsMember member);
}
