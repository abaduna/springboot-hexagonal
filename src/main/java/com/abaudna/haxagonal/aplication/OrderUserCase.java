package com.abaudna.haxagonal.aplication;


import java.math.BigDecimal;
import java.util.UUID;

import com.abaudna.haxagonal.domain.Orders;
import com.abaudna.haxagonal.infra.inputport.OrderInputPort;
import com.abaudna.haxagonal.infra.outputPort.EntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



@Component
public class OrderUserCase implements OrderInputPort {

    @Autowired
    EntityRepository entityRepository;

    @Override
    public Orders createOrder(String customerId, BigDecimal total) {
        Orders order = Orders.builder()
                .id( UUID.randomUUID().toString() )
                .coustomerId( customerId )
                .total(String.valueOf(total))
                .build();

        return entityRepository.save( order );
    }

}