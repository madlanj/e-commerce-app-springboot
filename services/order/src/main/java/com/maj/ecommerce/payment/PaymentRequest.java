package com.maj.ecommerce.payment;

import com.maj.ecommerce.customer.CustomerResponse;
import com.maj.ecommerce.order.PaymentMethod;

import java.math.BigDecimal;

public record PaymentRequest(
        BigDecimal amount,
        PaymentMethod paymentMethod,
        Integer orderId,
        String orderReference,
        CustomerResponse customer
) {
}
