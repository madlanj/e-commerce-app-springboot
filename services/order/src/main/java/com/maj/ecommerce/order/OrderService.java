package com.maj.ecommerce.order;

import com.maj.ecommerce.customer.CutomerClient;
import com.maj.ecommerce.exception.BusinessException;
import com.maj.ecommerce.kafka.OrderConfirmation;
import com.maj.ecommerce.kafka.OrderProducer;
import com.maj.ecommerce.orderline.OrderLineRequest;
import com.maj.ecommerce.orderline.OrderLineService;
import com.maj.ecommerce.payment.PaymentClient;
import com.maj.ecommerce.payment.PaymentRequest;
import com.maj.ecommerce.product.ProductClient;
import com.maj.ecommerce.product.PurchaseRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository repository;
    private final CutomerClient cutomerClient;
    private final ProductClient productClient;
    private final OrderMapper mapper;
    private final OrderLineService orderLineService;
    private final OrderProducer orderProducer;

    private final PaymentClient paymentClient;

    public Integer createOrder( OrderRequest request) {
        // check the customer -> openFeign
        var customer = this.cutomerClient.findByCustomerId(request.customerId())
                .orElseThrow(() -> new BusinessException("Cannot create order: No customer exist with customer id :: " + request.customerId()));

        // purchase the product --> product-microservice (rest template)
        var purchaseProduct = this.productClient.purchaseProducts(request.products());

        // persist order
        var order = this.repository.save(mapper.toOrder(request));

        // persist order line
        for(PurchaseRequest purchaseRequest : request.products()){
            orderLineService.saveOrderLine(
                    new OrderLineRequest(
                            null,
                            order.getId(),
                            purchaseRequest.productId(),
                            purchaseRequest.quantity()
                    )
            );
        }

        //  payment process
        var paymentRequest = new PaymentRequest(
                request.amount(),
                request.paymentMethod(),
                order.getId(),
                order.getReference(),
                customer
        );
        paymentClient.requestOrderPayment(paymentRequest);

        // send the order confirmation  --> notification-microservice (kafka)
        orderProducer.sendOrderConfirmation(
                new OrderConfirmation(
                        request.reference(),
                        request.amount(),
                        request.paymentMethod(),
                        customer,
                        purchaseProduct
                )
        );


        return order.getId();
    }

    public List<OrderResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::fromOrder)
                .collect(Collectors.toList());
    }

    public OrderResponse findById(Integer orderId) {
        return repository.findById(orderId)
                .map(mapper::fromOrder)
                .orElseThrow(() -> new EntityNotFoundException(String.format("No order found with provide ID :: %d" , orderId)));
    }
}
