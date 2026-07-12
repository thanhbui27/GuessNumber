package com.guessgame.service;

import com.guessgame.enums.PaymentProvider;
import com.guessgame.exception.ApiException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class PaymentGatewayRegistry {
    private final Map<PaymentProvider, PaymentService> services = new EnumMap<>(PaymentProvider.class);

    public PaymentGatewayRegistry(List<PaymentService> paymentServices) {
        paymentServices.forEach(service -> services.put(service.provider(), service));
    }

    public PaymentService require(PaymentProvider provider) {
        PaymentService service = services.get(provider);
        if (service == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Phuong thuc thanh toan khong duoc ho tro.");
        }
        return service;
    }
}
