package com.maj.ecommerce.customer;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(
        name = "customer-service",
        url = "${application.config.customer-url}"
)
public interface CutomerClient {

    @GetMapping("/{customer-id}")
    Optional<CustomerResponse> findByCustomerId(@PathVariable("customer-id") String customerId);
}