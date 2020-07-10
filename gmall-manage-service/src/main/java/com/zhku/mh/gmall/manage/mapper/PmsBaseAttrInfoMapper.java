package com.zhku.mh.gmall.manage.mapper;

import com.zhku.mh.gmall.bean.PmsBaseAttrInfo;
import com.zhku.mh.gmall.bean.PmsBaseAttrValue;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * ClassName：
 * Time：2019/12/17 23:52
 * Description：
 * Author： mh
 */
public interface PmsBaseAttrInfoMapper extends Mapper<PmsBaseAttrInfo> {
    List<PmsBaseAttrInfo> selectAttrValueListByValueIds(@Param("valueIdStr") String valueIdStr);
}
