package com.example.messages.controller;

import com.example.messages.model.Message;
import com.example.messages.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controlador REST para gestionar los mensajes.
 * Define los endpoints para obtener y crear mensajes.
 */
@RestController
@RequestMapping("/messages")
public class MessageController {

    // Inyecta el servicio de mensajes para manejar la lógica de negocio
    @Autowired
    private MessageService service;

    /**
     * Endpoint para obtener la lista de todos los mensajes.
     * @return Lista de objetos Message
     */
    @GetMapping
    public List<Message> getMessages() {
        return service.getAllMessages();
    }

    /**
     * Endpoint para crear un nuevo mensaje.
     * Recibe un JSON en el cuerpo de la petición y lo convierte en un objeto Message.
     * @param message Objeto Message recibido en el cuerpo de la petición
     * @return El mensaje guardado
     */
    @PostMapping(consumes = "application/json", produces = "application/json")
    public Message createMessage(@RequestBody Message message) {
        return service.saveMessage(message);
    }
}