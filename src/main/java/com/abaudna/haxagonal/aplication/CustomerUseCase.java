package com.abaudna.haxagonal.aplication;

import java.util.List;
import java.util.UUID;

import com.abaudna.haxagonal.domain.Customer;
import com.abaudna.haxagonal.infra.inputport.CustomersInputPort;
import com.abaudna.haxagonal.infra.outputPort.EntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



@Component
public class CustomerUseCase implements CustomersInputPort {

    @Autowired
    EntityRepository entityRepository;

    @Override
    public Customer createCustomer(String name, String country) {
        Customer customer = Customer.builder()
                .Id( UUID.randomUUID().toString() )
                .name( name )
                .country( country )
                .build();

        return entityRepository.save( customer );
    }

    @Override
    public Customer getById(String customerId) {
        return entityRepository.getById( customerId, Customer.class );
    }

    @Override
    public List<Customer> getAll() {
        return entityRepository.getAll( Customer.class );
    }

}
