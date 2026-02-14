package com.example.messages.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para la ruta raíz.
 * Encargado de servir la página principal index.html.
 */
@Controller
public class HomeController {

    /**
     * Endpoint raíz que sirve la plantilla index.html ubicada en resources/templates.
     * 
     * @return El nombre de la vista (index).
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }
     
     /**
      * Endpoint HTML para la ruta /messages.
      * Sirve la vista messages.html cuando el cliente solicita HTML.
      */
     @GetMapping(value = "/messages")
     public String messagesPage() {
         return "messages";
     }
}
