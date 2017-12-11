package com.scoprion.mall.wx.pay.util;

import com.github.pagehelper.Page;
import com.scoprion.constant.Constant;
import com.scoprion.enums.CommonEnum;
import com.scoprion.mall.backstage.mapper.OrderMapper;
import com.scoprion.mall.domain.order.Order;
import com.scoprion.mall.domain.order.OrderLog;
import com.scoprion.mall.wx.mapper.WxOrderLogMapper;
import com.scoprion.mall.wx.mapper.WxOrderMapper;
import com.scoprion.mall.wx.pay.WxPayConfig;
import com.scoprion.mall.wx.pay.domain.OrderQueryResponseData;
import com.scoprion.mall.wx.pay.domain.UnifiedOrderNotifyRequestData;
import com.scoprion.mall.wx.pay.domain.WxRefundNotifyResponseData;
import com.scoprion.mall.wx.service.pay.WxPayService;
import com.scoprion.result.BaseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ycj
 * @version V1.0 <定时查询订单定时器>
 * @date 2017-11-10 10:21
 */
@Component
public class WxOrderScheduler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    WxOrderMapper wxOrderMapper;

    @Autowired
    WxOrderLogMapper wxOrderLogMapper;

    @Autowired
    WxPayService wxPayService;

    /**
     * 12小时执行一次 查询申请退款时间大于两天（48小时） 的订单
     */
    @Transactional(rollbackFor = Exception.class)
    @Scheduled(fixedRate = 12 * 60 * 60 * 1000)
    public void findRefundingOrder() {
        List<Order> orderList = wxOrderMapper.findRefundingOrder();
        orderList.forEach(order -> {
            if (order.getRefundFee() > 0) {
                String nonceStr = WxUtil.createRandom(false, 10);
                String refundOrderNo = order.getOrderNo() + "T";
                //定义接收退款返回字符串
                String refundXML = WxPayUtil.refundSign(order.getOrderNo(), order.getPaymentFee(), order.getRefundFee(),
                        refundOrderNo, nonceStr);
                //接收退款返回
                String response = null;
                try {
                    response = WxPayUtil.doRefund(WxPayConfig.WECHAT_REFUND, refundXML, WxPayConfig.MCHID);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (response != null) {
                    WxRefundNotifyResponseData responseData = WxPayUtil.castXMLStringToWxRefundNotifyResponseData(response);
                    Boolean result = "success".equalsIgnoreCase(responseData.getReturn_code());
                    if (result) {
                        //退款成功，更新状态
                        wxOrderMapper.updateOrderStatusById(order.getId(), CommonEnum.REFUND_SUCCESS.getCode());
                    }
                    //记录退款日志
                    saveOrderLog(order.getId(), order.getOrderNo(), responseData.getReturn_msg());
                } else {
                    //记录退款失败日志
                    saveOrderLog(order.getId(), order.getOrderNo(), "微信退款失败");
                }
            }
        });
    }

    /**
     * 保存订单日志
     *
     * @param orderId
     * @param orderNo
     * @param action
     */
    private void saveOrderLog(Long orderId, String orderNo, String action) {
        OrderLog orderLog = new OrderLog();
        orderLog.setOrderId(orderId);
        orderLog.setOrderNo(orderNo);
        orderLog.setIpAddress("");
        orderLog.setAction(action);
        wxOrderLogMapper.add(orderLog);
    }

    /**
     * 查询未付款订单，校验微信支付结果
     */
    @Scheduled(fixedRate = 4 * 60 * 60 * 1000)
    public void findUnPayOrder() {
//        logger.info("每4小時执行一次。开始");
        List<Order> page = wxOrderMapper.findUnPayOrder();
        if (page == null || page.size() == 0) {
//            logger.info("每4小時执行一次。结束。");
            return;
        }
        page.forEach(order -> {
            if (order.getWxOrderNo() != null) {
                queryOrder(order.getId(), order.getWxOrderNo(), null);
            } else if (order.getOrderNo() != null) {
                queryOrder(order.getId(), null, order.getOrderNo());
            }
        });
//        logger.info("每4小時执行一次。结束。");
    }

    /**
     * 商户订单号
     *
     * @param orderId   订单id
     * @param orderNo   订单号
     * @param wxOrderNo 微信订单号
     */
    private void queryOrder(Long orderId, String wxOrderNo, String orderNo) {
        Map<String, Object> data = new HashMap<>(16);
        data.put("appid", WxPayConfig.APP_ID);
        data.put("mch_id", WxPayConfig.MCHID);
        if (wxOrderNo != null) {
            data.put("transaction_id", wxOrderNo);
        } else {
            data.put("out_trade_no", orderNo);
        }
        data.put("nonce_str", WxUtil.createRandom(false, 32));
        String sign = WxPayUtil.sort(data);
        sign = WxUtil.MD5(sign).toUpperCase();
        data.put("sign", sign);
        String param = WxPayUtil.mapConvertToXML(data);
        String result = WxUtil.httpsRequest(WxPayConfig.WECHAT_ORDER_QUERY_URL, "POST", param);
        OrderQueryResponseData response = WxPayUtil.castXMLStringToOrderQueryResponseData(result);
        updateLocalStatus(orderId, response);
    }

    /**
     * @param orderId      订单id
     * @param responseData 微信返回
     */
    private void updateLocalStatus(Long orderId, OrderQueryResponseData responseData) {
        if (orderId == null || responseData.getTrade_state() == null) {
            return;
        }
        switch (responseData.getTrade_state()) {
            case Constant.WX_PAY_SUCCESS:
                //SUCCESS—支付成功
                UnifiedOrderNotifyRequestData data = new UnifiedOrderNotifyRequestData();
                data.setTransaction_id(responseData.getTransaction_id());
                data.setOut_trade_no(responseData.getOut_trade_no());
                data.setTime_end(responseData.getTime_end());
                wxPayService.callback(data);
                break;
            default:
                break;
        }
    }
}
