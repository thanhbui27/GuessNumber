package com.guessgame.service;

import com.guessgame.enums.PaymentProvider;
import com.guessgame.enums.PaymentStatus;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DemoPaymentService implements PaymentService {
    @Override
    public PaymentProvider provider() {
        return PaymentProvider.DEMO;
    }

    @Override
    public PaymentStartResult startPayment(Long userId, BigDecimal amount, int turns, String ipAddress) {
        return new PaymentStartResult(PaymentProvider.DEMO, PaymentStatus.SUCCESS, BigDecimal.ZERO, UUID.randomUUID().toString(), null);
    }

    @Override
    public PaymentCallbackResult handleReturn(Map<String, String> params) {
        return new PaymentCallbackResult(PaymentStatus.FAILED, null, "99", "Demo payment does not support callbacks.");
    }

    @Override
    public PaymentCallbackResult handleIpn(Map<String, String> params) {
        return new PaymentCallbackResult(PaymentStatus.FAILED, null, "99", "Demo payment does not support callbacks.");
    }
}
