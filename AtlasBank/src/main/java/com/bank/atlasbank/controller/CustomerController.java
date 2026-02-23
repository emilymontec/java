package com.bank.atlasbank.controller;

import com.bank.atlasbank.model.Customer;
import com.bank.atlasbank.repository.CustomerRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador responsable de exponer operaciones relacionadas con clientes.
 */
@RestController
@RequestMapping("/admin/customers")
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
        validateKyc(customer);
        customerRepository.findByDocumentId(customer.getDocumentId()).ifPresent(existing -> {
            throw new IllegalArgumentException("Ya existe un cliente con ese documento");
        });
        customer.setStatus("ACTIVE");
        Customer saved = customerRepository.save(customer);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @Valid @RequestBody Customer customer) {
        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        existing.setName(customer.getName());
        existing.setEmail(customer.getEmail());
        existing.setDocumentId(customer.getDocumentId());
        validateKyc(existing);
        customerRepository.findByDocumentId(existing.getDocumentId())
                .filter(c -> !c.getId().equals(id))
                .ifPresent(c -> {
                    throw new IllegalArgumentException("Ya existe un cliente con ese documento");
                });
        Customer saved = customerRepository.save(existing);
        return ResponseEntity.ok(saved);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Customer> updateStatus(@PathVariable Long id, @RequestParam String status) {
        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        existing.setStatus(normalizeStatus(status));
        Customer saved = customerRepository.save(existing);
        return ResponseEntity.ok(saved);
    }

    private void validateKyc(Customer customer) {
        if (customer.getName() == null || customer.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (customer.getDocumentId() == null || customer.getDocumentId().isBlank()) {
            throw new IllegalArgumentException("El documento es obligatorio");
        }
        if (customer.getDocumentId().length() < 5) {
            throw new IllegalArgumentException("El documento no es válido");
        }
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }
        String value = status.trim().toUpperCase();
        if (!value.equals("ACTIVE") && !value.equals("BLOCKED") && !value.equals("CLOSED")) {
            throw new IllegalArgumentException("Estado de cliente inválido");
        }
        return value;
    }
}
