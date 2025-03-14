package com.abaudna.haxagonal.infra.inputport;

import com.abaudna.haxagonal.domain.Customer;

import java.util.List;

public interface CustomersInputPort {
    public Customer createCustomer(String name, String country);

    public Customer getById(String customerId);

    public List<Customer> getAll();
}
