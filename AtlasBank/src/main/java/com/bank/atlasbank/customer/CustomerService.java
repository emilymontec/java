package com.bank.atlasbank.customer;

import com.bank.atlasbank.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    public Customer create(CreateCustomerRequest request) {
        repository.findByEmail(request.email()).ifPresent(existing -> {
            throw new BusinessException("Ya existe un cliente con ese email");
        });

        Customer customer = new Customer();
        customer.setFullName(request.fullName());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());
        return repository.save(customer);
    }

    public List<Customer> findAll() {
        return repository.findAll();
    }

    public Customer findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("Cliente no encontrado: " + id));
    }
}
