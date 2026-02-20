package com.bank.atlasbank.controller;

import com.bank.atlasbank.model.Customer;
import com.bank.atlasbank.repository.CustomerRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador responsable de exponer operaciones relacionadas con clientes.
 */
@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerRepository customerRepository;

    /**
     * Crea un nuevo cliente a partir de los datos enviados en el cuerpo de la
     * petición.
     *
     // @param customer entidad Customer con nombre y correo a registrar
     // @return el cliente persistido, incluyendo su identificador generado
     */
    @PostMapping
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody Customer customer) {
        Customer saved = customerRepository.save(customer);
        return ResponseEntity.ok(saved);
    }
}
