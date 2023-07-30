package com.example.alipay.controller;

import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.alipay.common.AlipayConfig;
import com.example.alipay.dao.OrdersMapper;
import com.example.alipay.entity.AliPayResponse;
import com.example.alipay.entity.Orders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// xjlugv6874@sandbox.com
// 9428521.24 - 30 = 9428491.24 + 30 = 9428521.24
@RestController
@RequestMapping("/alipay")
public class AliPayController {

    private static final String GATEWAY_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private static final String FORMAT = "JSON";
    private static final String CHARSET = "UTF-8";
    //签名方式
    private static final String SIGN_TYPE = "RSA2";

    @Resource
    private AlipayConfig aliPayConfig;

    @Resource
    private OrdersMapper ordersMapper;


    /**
     * 二维码支付
     *
     * @param aliPay       阿里pay
     * @param httpResponse 响应
     * @throws Exception 异常
     */
    @GetMapping("/payQR") // &subject=xxx&traceNo=xxx&totalAmount=xxx
    public String payQR(AliPay aliPay, HttpServletResponse httpResponse) throws Exception {
        // 1. 创建Client，通用SDK提供的Client，负责调用支付宝的API
        AlipayClient alipayClient = new DefaultAlipayClient(GATEWAY_URL, aliPayConfig.getAppId(),
                aliPayConfig.getAppPrivateKey(), FORMAT, CHARSET, aliPayConfig.getAlipayPublicKey(), SIGN_TYPE);

        // 2. 创建 Request并设置Request参数
        // 二维码方式
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setNotifyUrl(aliPayConfig.getNotifyUrl());
        request.setReturnUrl(aliPayConfig.getReturnUrl());
        JSONObject bizContent = new JSONObject();
        // 我们自己生成的订单编号
        bizContent.set("out_trade_no", aliPay.getTraceNo());
        // 订单的总金额
        bizContent.set("total_amount", aliPay.getTotalAmount());
        // 支付的名称
        bizContent.set("subject", aliPay.getSubject());
        request.setBizContent(bizContent.toString());

        // 执行请求，拿到响应的结果，返回给浏览器
        String form = "";
        try {
            // 二维码方式
            AlipayTradePrecreateResponse response = alipayClient.execute(request);
            form = response.getBody();
            AliPayResponse aliPayResponse = JSON.parseObject(form, AliPayResponse.class);
            if ("10000".equals(aliPayResponse.getAlipayTradePrecreateResponse().getCode())) {
                return aliPayResponse.getAlipayTradePrecreateResponse().getQrCode();
            } else if ("40004".equals(aliPayResponse.getAlipayTradePrecreateResponse().getCode())) {
                return "订单已支付";
            }
            return "支付失败";
            // 支付界面及二维码方式
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return "支付失败";
    }

    // ?subject=购买鼠标订单&traceNo=202307091688903243739&totalAmount=50
    @GetMapping("/payUrl")
    public void payUrl(AliPay aliPay, HttpServletResponse httpResponse) throws IOException {
        // 1. 创建Client，通用SDK提供的Client，负责调用支付宝的API
        AlipayClient alipayClient = new DefaultAlipayClient(GATEWAY_URL, aliPayConfig.getAppId(),
                aliPayConfig.getAppPrivateKey(), FORMAT, CHARSET, aliPayConfig.getAlipayPublicKey(), SIGN_TYPE);

        // 2. 创建 Request并设置Request参数
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();  // 发送请求的 Request类

        request.setNotifyUrl(aliPayConfig.getNotifyUrl());
        request.setReturnUrl(aliPayConfig.getReturnUrl());
        JSONObject bizContent = new JSONObject();
        // 我们自己生成的订单编号
        bizContent.set("out_trade_no", aliPay.getTraceNo());
        // 订单的总金额
        bizContent.set("total_amount", aliPay.getTotalAmount());
        // 支付的名称
        bizContent.set("subject", aliPay.getSubject());
        // 固定配置
        bizContent.set("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(bizContent.toString());

        // 执行请求，拿到响应的结果，返回给浏览器
        String form = "";
        try {
            // 调用SDK生成表单
            form = alipayClient.pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        httpResponse.setContentType("text/html;charset=" + CHARSET);
        // 直接将完整的表单html输出到页面
        httpResponse.getWriter().write(form);
        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();
    }

    @PostMapping("/notify")  // 注意这里必须是POST接口
    public String payNotify(HttpServletRequest request) throws Exception {
        if (request.getParameter("trade_status").equals("TRADE_SUCCESS")) {
            System.out.println("=========支付宝异步回调========");

            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet()) {
                params.put(name, request.getParameter(name));
                // System.out.println(name + " = " + request.getParameter(name));
            }

            String outTradeNo = params.get("out_trade_no");
            String gmtPayment = params.get("gmt_payment");
            String alipayTradeNo = params.get("trade_no");

            String sign = params.get("sign");
            String content = AlipaySignature.getSignCheckContentV1(params);
            boolean checkSignature = AlipaySignature.rsa256CheckContent(content, sign, aliPayConfig.getAlipayPublicKey(), "UTF-8"); // 验证签名
            // 支付宝验签
            if (checkSignature) {
                // 验签通过
                System.out.println("交易名称: " + params.get("subject"));
                System.out.println("交易状态: " + params.get("trade_status"));
                System.out.println("支付宝交易凭证号: " + params.get("trade_no"));
                System.out.println("商户订单号: " + params.get("out_trade_no"));
                System.out.println("交易金额: " + params.get("total_amount"));
                System.out.println("买家在支付宝唯一id: " + params.get("buyer_id"));
                System.out.println("买家付款时间: " + params.get("gmt_payment"));
                System.out.println("买家付款金额: " + params.get("buyer_pay_amount"));

                // 查询订单
                QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("order_id", outTradeNo);
                Orders orders = ordersMapper.selectOne(queryWrapper);

                if (orders != null) {
                    orders.setAlipayNo(alipayTradeNo);
                    orders.setPayTime(new Date());
                    orders.setState("已支付");
                    ordersMapper.updateById(orders);
                }
            }
        }
        return "success";
    }

}


