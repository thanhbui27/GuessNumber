package com.guessgame.service;

import com.guessgame.enums.PaymentProvider;
import java.math.BigDecimal;
import java.util.Map;

public interface PaymentService {
    PaymentProvider provider();

    PaymentStartResult startPayment(Long userId, BigDecimal amount, int turns, String ipAddress);

    PaymentCallbackResult handleReturn(Map<String, String> params);

    PaymentCallbackResult handleIpn(Map<String, String> params);
}
