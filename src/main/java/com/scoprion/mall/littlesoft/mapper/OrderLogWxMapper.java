package com.scoprion.mall.littlesoft.mapper;

import com.scoprion.mall.domain.MallLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author by Administrator
 * @created on 2017/11/2/002.
 */
@Mapper
public interface OrderLogWxMapper {

    /**
     * 创建订单日志
     * @param mallLog
     */
    int add(MallLog mallLog);
}