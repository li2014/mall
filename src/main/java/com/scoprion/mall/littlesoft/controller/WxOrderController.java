package com.scoprion.mall.littlesoft.controller;

import com.scoprion.mall.littlesoft.service.orders.WxOrderService;
import com.scoprion.result.BaseResult;
import com.scoprion.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author by Administrator
 * @created on 2017/11/6/006.
 */

@RestController
@RequestMapping("wx/orders")
public class WxOrderController {

    @Autowired
    private WxOrderService wxOrderService;

    /**
     * 订单列表
     * @param pageNo
     * @param pageSize
     * @param userId
     * @param orderStatus
     * @return
     */
    @RequestMapping(value = "/findByCondition",method = RequestMethod.GET)
    public PageResult findByCondition (Integer pageNo,Integer pageSize,Long userId,String orderStatus){
        return wxOrderService.findByCondition (pageNo,pageSize,userId,orderStatus);
    }

    @RequestMapping(value = "/orderLog",method = RequestMethod.GET)
    public BaseResult  findByCondition(Long orderId){
        return wxOrderService.findByCondition(orderId);
    }
}