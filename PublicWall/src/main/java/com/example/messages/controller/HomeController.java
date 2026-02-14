package com.example.messages.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador para la ruta raíz.
 * Proporciona un mensaje de bienvenida simple.
 */
@RestController
public class HomeController {

    /**
     * Endpoint raíz que devuelve un mensaje de bienvenida.
     * 
     * @return String de bienvenida.
     */
    @GetMapping("/")
    public String home() {
        return "Welcome to PublicWall!";
    }
}