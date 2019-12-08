package com.zhku.mh.gmall.user.mapper;

import com.zhku.mh.gmall.bean.UmsMember;
import com.zhku.mh.gmall.bean.UmsMemberReceiveAddress;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * ClassName：
 * Time：2019/12/4 22:06
 * Description：
 * Author： mh
 */
public interface UserMapper extends Mapper<UmsMember> {
    List<UmsMember> getAllUser();

    List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId);
}
