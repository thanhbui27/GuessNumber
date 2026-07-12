package com.guessgame.controller;

import com.guessgame.config.VnpayProperties;
import com.guessgame.dto.payment.VnpayIpnResponse;
import com.guessgame.enums.PaymentProvider;
import com.guessgame.enums.PaymentStatus;
import com.guessgame.service.PaymentCallbackResult;
import com.guessgame.service.PaymentGatewayRegistry;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentGatewayRegistry paymentGatewayRegistry;
    private final VnpayProperties vnpayProperties;

    public PaymentController(PaymentGatewayRegistry paymentGatewayRegistry, VnpayProperties vnpayProperties) {
        this.paymentGatewayRegistry = paymentGatewayRegistry;
        this.vnpayProperties = vnpayProperties;
    }

    @GetMapping("/vnpay-return")
    ResponseEntity<Void> vnpayReturn(@RequestParam MultiValueMap<String, String> queryParams) {
        PaymentCallbackResult result = paymentGatewayRegistry.require(PaymentProvider.VNPAY).handleReturn(toSingleValueMap(queryParams));
        String status = result.status() == PaymentStatus.SUCCESS ? "success" : "failed";
        URI redirectUri = URI.create(vnpayProperties.getFrontendReturnUrl()
                + "?paymentStatus=" + status
                + "&transactionCode=" + encode(result.transactionCode())
                + "&message=" + encode(result.message()));
        return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, redirectUri.toString()).build();
    }

    @GetMapping("/vnpay-ipn")
    VnpayIpnResponse vnpayIpn(@RequestParam MultiValueMap<String, String> queryParams) {
        PaymentCallbackResult result = paymentGatewayRegistry.require(PaymentProvider.VNPAY).handleIpn(toSingleValueMap(queryParams));
        return new VnpayIpnResponse(result.rspCode(), result.message());
    }

    private Map<String, String> toSingleValueMap(MultiValueMap<String, String> queryParams) {
        return queryParams.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().isEmpty() ? "" : entry.getValue().get(0)));
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
