package com.example.alipay.entity;

/**
 * 阿里支付响应对象
 *
 * @Author guofan
 * @Create 2023/7/30
 */

public class AliPayResponse {
    /**
     * 尝试支付请求
     */
    private AlipayTradePrecreateResponse alipayTradePrecreateResponse;
    /**
     * 签名
     */
    private String sign;

    public AliPayResponse() {
    }

    public AliPayResponse(AlipayTradePrecreateResponse alipayTradePrecreateResponse, String sign) {
        this.alipayTradePrecreateResponse = alipayTradePrecreateResponse;
        this.sign = sign;
    }

    /**
     * 响应类
     */
    public class AlipayTradePrecreateResponse {
        private String code;
        private String msg;
        private String outTradeNo;
        private String qrCode;

        public AlipayTradePrecreateResponse() {
        }

        public AlipayTradePrecreateResponse(String code, String msg, String outTradeNo, String qrCode) {
            this.code = code;
            this.msg = msg;
            this.outTradeNo = outTradeNo;
            this.qrCode = qrCode;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getOutTradeNo() {
            return outTradeNo;
        }

        public void setOutTradeNo(String outTradeNo) {
            this.outTradeNo = outTradeNo;
        }

        public String getQrCode() {
            return qrCode;
        }

        public void setQrCode(String qrCode) {
            this.qrCode = qrCode;
        }

        @Override
        public String toString() {
            return "AlipayTradePrecreateResponse{" +
                    "code='" + code + '\'' +
                    ", msg='" + msg + '\'' +
                    ", outTradeNo='" + outTradeNo + '\'' +
                    ", qrCode='" + qrCode + '\'' +
                    '}';
        }
    }

    public AlipayTradePrecreateResponse getAlipayTradePrecreateResponse() {
        return alipayTradePrecreateResponse;
    }

    public void setAlipayTradePrecreateResponse(AlipayTradePrecreateResponse alipayTradePrecreateResponse) {
        this.alipayTradePrecreateResponse = alipayTradePrecreateResponse;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
