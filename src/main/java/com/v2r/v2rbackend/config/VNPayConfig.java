package com.v2r.v2rbackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "vnpay")
public class VNPayConfig {
    
    // VNPay Sandbox Configuration
    private String vnpUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private String vnpReturnUrl = "http://localhost:8080/api/payment/vnpay-return";
    private String vnpTmnCode="SPCCEJMW"; // Terminal ID - Get from VNPay sandbox
    private String vnpHashSecret="PE7EFFIBVR4876LOWF1ZJX2CNZEVDO2C"; // Secret key - Get from VNPay sandbox
    private String vnpApiUrl = "https://sandbox.vnpayment.vn/merchant_webapi/api/transaction";
    private String vnpVersion = "2.1.0";
    private String vnpCommand = "pay";
    private String vnpOrderType = "other";
    
    // Getters and Setters
    public String getVnpUrl() {
        return vnpUrl;
    }

    public void setVnpUrl(String vnpUrl) {
        this.vnpUrl = vnpUrl;
    }

    public String getVnpReturnUrl() {
        return vnpReturnUrl;
    }

    public void setVnpReturnUrl(String vnpReturnUrl) {
        this.vnpReturnUrl = vnpReturnUrl;
    }

    public String getVnpTmnCode() {
        return vnpTmnCode;
    }

    public void setVnpTmnCode(String vnpTmnCode) {
        this.vnpTmnCode = vnpTmnCode;
    }

    public String getVnpHashSecret() {
        return vnpHashSecret;
    }

    public void setVnpHashSecret(String vnpHashSecret) {
        this.vnpHashSecret = vnpHashSecret;
    }

    public String getVnpApiUrl() {
        return vnpApiUrl;
    }

    public void setVnpApiUrl(String vnpApiUrl) {
        this.vnpApiUrl = vnpApiUrl;
    }

    public String getVnpVersion() {
        return vnpVersion;
    }

    public void setVnpVersion(String vnpVersion) {
        this.vnpVersion = vnpVersion;
    }

    public String getVnpCommand() {
        return vnpCommand;
    }

    public void setVnpCommand(String vnpCommand) {
        this.vnpCommand = vnpCommand;
    }

    public String getVnpOrderType() {
        return vnpOrderType;
    }

    public void setVnpOrderType(String vnpOrderType) {
        this.vnpOrderType = vnpOrderType;
    }
    
    public java.util.Map<String, String> getVNPayConfig(boolean type) {
        java.util.Map<String, String> vnpParamsMap = new java.util.HashMap<>();
        vnpParamsMap.put("vnp_Version", this.vnpVersion);
        vnpParamsMap.put("vnp_Command", this.vnpCommand);
        vnpParamsMap.put("vnp_TmnCode", this.vnpTmnCode);
        vnpParamsMap.put("vnp_CurrCode", "VND");
        vnpParamsMap.put("vnp_TxnRef", com.v2r.v2rbackend.util.VNPayUtil.getRandomNumber(8));
        vnpParamsMap.put("vnp_OrderInfo", "Thanh toan don hang:" + com.v2r.v2rbackend.util.VNPayUtil.getRandomNumber(8));
        vnpParamsMap.put("vnp_OrderType", this.vnpOrderType);
        vnpParamsMap.put("vnp_Locale", "vn");
        
        if (type) {
            vnpParamsMap.put("vnp_ReturnUrl", this.vnpReturnUrl);
        } else {
            vnpParamsMap.put("vnp_ReturnUrl", this.vnpReturnUrl.replace("vn-pay-callback", "vn-pay-callback-consignment"));
        }
        
        java.util.Calendar calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Etc/GMT+7"));
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
        String vnpCreateDate = formatter.format(calendar.getTime());
        vnpParamsMap.put("vnp_CreateDate", vnpCreateDate);
        
        calendar.add(java.util.Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(calendar.getTime());
        vnpParamsMap.put("vnp_ExpireDate", vnp_ExpireDate);
        
        return vnpParamsMap;
    }
}
