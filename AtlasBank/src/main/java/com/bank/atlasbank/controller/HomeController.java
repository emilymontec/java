package com.bank.atlasbank.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de la ruta pública raíz de la API.
 * <p>
 * Sirve como endpoint de healthcheck sencillo para verificar que el backend
 * está levantado y respondiendo.
 */
@RestController
public class HomeController {

    /**
     * Healthcheck simple de la aplicación.
     *
     * @return mensaje plano confirmando que AtlasBank está en ejecución
     */
    @GetMapping("/health")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("AtlasBank is running...");
    }
}
