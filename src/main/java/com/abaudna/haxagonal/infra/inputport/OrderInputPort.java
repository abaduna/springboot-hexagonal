package com.abaudna.haxagonal.infra.inputport;

import com.abaudna.haxagonal.domain.Orders;

import java.math.BigDecimal;

public interface OrderInputPort {
    public Orders createOrder(String customerId, BigDecimal total );
}
