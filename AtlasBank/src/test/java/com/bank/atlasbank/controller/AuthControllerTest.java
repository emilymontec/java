package com.bank.atlasbank.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuthControllerTest {

    @Test
    void loginReturnsAuthenticatedUser() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "user",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );

        AuthController controller = new AuthController();

        ResponseEntity<AuthController.LoginResponse> response = controller.login(authentication);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo("user");
        assertThat(response.getBody().getRoles()).containsExactly("ROLE_CUSTOMER");
    }

    @Test
    void loginWithoutAuthenticationReturnsUnauthorized() {
        AuthController controller = new AuthController();

        ResponseEntity<AuthController.LoginResponse> response = controller.login(null);

        assertThat(response.getStatusCode().value()).isEqualTo(401);
    }
}


