package com.guessgame.service;

import com.guessgame.config.GameProperties;
import com.guessgame.config.VnpayProperties;
import com.guessgame.entity.PurchaseHistory;
import com.guessgame.entity.User;
import com.guessgame.enums.PaymentProvider;
import com.guessgame.enums.PaymentStatus;
import com.guessgame.exception.ApiException;
import com.guessgame.repository.PurchaseHistoryRepository;
import com.guessgame.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VnpayPaymentService implements PaymentService {
    private static final DateTimeFormatter VNPAY_DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final VnpayProperties properties;
    private final GameProperties gameProperties;
    private final PurchaseHistoryRepository purchaseHistoryRepository;
    private final UserRepository userRepository;

    public VnpayPaymentService(VnpayProperties properties,
                               GameProperties gameProperties,
                               PurchaseHistoryRepository purchaseHistoryRepository,
                               UserRepository userRepository) {
        this.properties = properties;
        this.gameProperties = gameProperties;
        this.purchaseHistoryRepository = purchaseHistoryRepository;
        this.userRepository = userRepository;
    }

    @Override
    public PaymentProvider provider() {
        return PaymentProvider.VNPAY;
    }

    @Override
    public PaymentStartResult startPayment(Long userId, BigDecimal amount, int turns, String ipAddress) {
        validateConfigured();
        String transactionCode = "BUY" + userId + System.currentTimeMillis();
        LocalDateTime now = LocalDateTime.now(VIETNAM_ZONE);

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", properties.getVersion());
        params.put("vnp_Command", properties.getCommand());
        params.put("vnp_TmnCode", properties.getTmnCode());
        params.put("vnp_Amount", toVnpayAmount(amount));
        params.put("vnp_CurrCode", properties.getCurrCode());
        params.put("vnp_TxnRef", transactionCode);
        params.put("vnp_OrderInfo", "Mua " + turns + " luot choi Guess Number");
        params.put("vnp_OrderType", properties.getOrderType());
        params.put("vnp_Locale", properties.getLocale());
        params.put("vnp_ReturnUrl", properties.getReturnUrl());
        params.put("vnp_IpAddr", normalizeIpAddress(ipAddress));
        params.put("vnp_CreateDate", now.format(VNPAY_DATE));
        params.put("vnp_ExpireDate", now.plusMinutes(properties.getExpireMinutes()).format(VNPAY_DATE));
        Logger logger = Logger.getLogger(VnpayPaymentService.class.getName());

        logger.info(() -> "VNPAY config:"
                + " paymentUrl=" + properties.getPaymentUrl()
                + ", tmnCode=" + maskTmnCode(properties.getTmnCode())
                + ", tmnCodeLength=" + (
                properties.getTmnCode() == null
                        ? 0
                        : properties.getTmnCode().trim().length()
        )
                + ", version=" + properties.getVersion()
                + ", command=" + properties.getCommand()
                + ", returnUrl=" + properties.getReturnUrl());        String secureHash = hmacSha512(properties.getHashSecret(), buildData(params));
        String paymentUrl = properties.getPaymentUrl() + "?" + buildData(params) + "&vnp_SecureHash=" + secureHash;
        return new PaymentStartResult(PaymentProvider.VNPAY, PaymentStatus.PENDING, amount, transactionCode, paymentUrl);
    }

    //func for debug
    private String maskTmnCode(String value) {
        if (value == null || value.length() < 4) {
            return "***";
        }

        String trimmed = value.trim();
        return "***" + trimmed.substring(trimmed.length() - 4);
    }

    @Override
    @Transactional
    public PaymentCallbackResult handleReturn(Map<String, String> params) {
        return processCallback(params, false);
    }

    @Override
    @Transactional
    public PaymentCallbackResult handleIpn(Map<String, String> params) {
        return processCallback(params, true);
    }

    private PaymentCallbackResult processCallback(Map<String, String> params, boolean ipn) {
        if (!isValidSignature(params)) {
            return new PaymentCallbackResult(PaymentStatus.FAILED, params.get("vnp_TxnRef"), "97", "Invalid checksum");
        }

        String transactionCode = params.get("vnp_TxnRef");
        PurchaseHistory purchase = purchaseHistoryRepository.findByTransactionCodeForUpdate(transactionCode).orElse(null);
        if (purchase == null) {
            return new PaymentCallbackResult(PaymentStatus.FAILED, transactionCode, "01", "Order not found");
        }

        if (!toVnpayAmount(purchase.getAmount()).equals(params.get("vnp_Amount"))) {
            return new PaymentCallbackResult(PaymentStatus.FAILED, transactionCode, "04", "Invalid amount");
        }

        boolean vnpaySuccess = "00".equals(params.get("vnp_ResponseCode"))
                && "00".equals(params.get("vnp_TransactionStatus"));

        if (purchase.getStatus() != PaymentStatus.PENDING) {
            if (ipn) {
                return new PaymentCallbackResult(purchase.getStatus(), transactionCode, "02", "Order already confirmed");
            }
            return new PaymentCallbackResult(purchase.getStatus(), transactionCode, "00", messageFor(purchase.getStatus()));
        }

        if (vnpaySuccess) {
            User user = userRepository.findByIdForUpdate(purchase.getUser().getId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Khong tim thay user."));
            int addedTurns = gameProperties.getBuyTurnsAmount();
            user.setTurns(user.getTurns() + addedTurns);
            purchase.setTurnsAdded(addedTurns);
            purchase.setStatus(PaymentStatus.SUCCESS);
            return new PaymentCallbackResult(PaymentStatus.SUCCESS, transactionCode, "00", "Confirm Success");
        }

        purchase.setTurnsAdded(0);
        purchase.setStatus(PaymentStatus.FAILED);
        return new PaymentCallbackResult(PaymentStatus.FAILED, transactionCode, "00", "Confirm Success");
    }

    private boolean isValidSignature(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null || receivedHash.isBlank()) {
            return false;
        }

        Map<String, String> signedParams = new TreeMap<>(params);
        signedParams.remove("vnp_SecureHash");
        signedParams.remove("vnp_SecureHashType");
        String expectedHash = hmacSha512(properties.getHashSecret(), buildData(signedParams));
        return MessageDigest.isEqual(
                expectedHash.toLowerCase().getBytes(StandardCharsets.UTF_8),
                receivedHash.toLowerCase().getBytes(StandardCharsets.UTF_8)
        );
    }

    private void validateConfigured() {
        if (properties.getTmnCode().isBlank() || properties.getHashSecret().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Chua cau hinh VNPAY_TMN_CODE hoac VNPAY_HASH_SECRET.");
        }
    }

    private String toVnpayAmount(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .toPlainString();
    }

    private String buildData(Map<String, String> params) {
        StringBuilder builder = new StringBuilder();
        params.forEach((key, value) -> {
            if (value != null && !value.isBlank()) {
                if (!builder.isEmpty()) {
                    builder.append('&');
                }
                builder.append(encode(key)).append('=').append(encode(value));
            }
        });
        return builder.toString();
    }

    private String hmacSha512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot sign VNPay payload", ex);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String normalizeIpAddress(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank() || "0:0:0:0:0:0:0:1".equals(ipAddress)) {
            return "127.0.0.1";
        }
        return ipAddress;
    }

    private String messageFor(PaymentStatus status) {
        return status == PaymentStatus.SUCCESS ? "Thanh toan thanh cong." : "Thanh toan khong thanh cong.";
    }
}
