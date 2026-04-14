package com.bank.atlasbank.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateCustomerRequest(
        @NotBlank(message = "Nombre completo es obligatorio") String fullName,
        @NotBlank(message = "Email es obligatorio") @Email(message = "Email invalido") String email,
        @NotBlank(message = "Telefono es obligatorio") String phone
) {
}
