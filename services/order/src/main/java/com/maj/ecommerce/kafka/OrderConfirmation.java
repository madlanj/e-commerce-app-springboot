package com.maj.ecommerce.kafka;

import com.maj.ecommerce.customer.CustomerResponse;
import com.maj.ecommerce.order.PaymentMethod;
import com.maj.ecommerce.product.PurchaseResponse;

import java.math.BigDecimal;
import java.util.List;

public record OrderConfirmation(
        String orderReference,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        CustomerResponse customer,
        List<PurchaseResponse> products
) {
}
