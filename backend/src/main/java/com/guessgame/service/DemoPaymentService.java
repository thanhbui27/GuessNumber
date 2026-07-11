package com.guessgame.service;

import com.guessgame.enums.PaymentProvider;
import com.guessgame.enums.PaymentStatus;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DemoPaymentService implements PaymentService {
    @Override
    public PaymentResult processPayment(Long userId) {
        return new PaymentResult(PaymentProvider.DEMO, PaymentStatus.SUCCESS, BigDecimal.ZERO, UUID.randomUUID().toString());
    }
}
