package com.bank.atlasbank.customer;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Customer create(@Valid @RequestBody CreateCustomerRequest request) {
        return service.create(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String customerId = credentials.get("customerId");
        String password = credentials.get("password");

        Optional<Customer> customer = service.authenticate(customerId, password);
        if (customer.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "id", customer.get().getId(),
                    "customerId", customer.get().getCustomerId(),
                    "fullName", customer.get().getFullName(),
                    "role", "USER"
            ));
        }

        return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", "Credenciales inválidas"
        ));
    }

    @GetMapping
    public List<Customer> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Customer findById(@PathVariable Long id) {
        return service.findById(id);
    }
}
