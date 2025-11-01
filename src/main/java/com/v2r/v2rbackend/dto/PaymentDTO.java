package com.v2r.v2rbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class PaymentDTO {

    @Getter
    @Setter
    @NoArgsConstructor

    public static class VNPayResponse {
        private String code;
        private String message;
        private String paymentUrl;

        public VNPayResponse(String code, String message, String paymentUrl) {
            this.code = code;
            this.message = message;
            this.paymentUrl = paymentUrl;
        }

        public static VNPayResponseBuilder builder() {
            return new VNPayResponseBuilder();
        }
        
        public static class VNPayResponseBuilder {
            private String code;
            private String message;
            private String paymentUrl;

            public VNPayResponseBuilder(String code, String message, String paymentUrl) {
                this.code = code;
                this.message = message;
                this.paymentUrl = paymentUrl;
            }

            public VNPayResponseBuilder() {

            }

            public VNPayResponseBuilder code(String code) {
                this.code = code;
                return this;
            }
            
            public VNPayResponseBuilder message(String message) {
                this.message = message;
                return this;
            }
            
            public VNPayResponseBuilder paymentUrl(String paymentUrl) {
                this.paymentUrl = paymentUrl;
                return this;
            }
            
            public VNPayResponse build() {
                return new VNPayResponse(code, message, paymentUrl);
            }
        }
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentRequest {
        private Integer subscriptionId;
        private String bankCode;
    }
}
