package com.example.messages.controller;

import com.example.messages.model.Message;
import com.example.messages.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controlador REST que expone los endpoints para interactuar con los mensajes.
 * La anotación @RestController combina @Controller y @ResponseBody.
 * @RequestMapping("/messages") define la ruta base para todos estos endpoints.
 */
@RestController
@RequestMapping("/messages")
public class MessageController {

    // Inyecta el servicio de mensajes para manejar la lógica de negocio
    @Autowired
    private MessageService service;

    /**
     * Endpoint para obtener la lista de todos los mensajes.
     * 
     * @return Lista de objetos Message
     */
    @GetMapping
    public List<Message> getMessages() {
        return service.getAllMessages();
    }

    /**
     * Endpoint para obtener un mensaje específico por su ID.
     * 
     * @param id Identificador del mensaje en la URL
     * @return El mensaje si existe, o null (se podría mejorar con ResponseEntity)
     */
    @GetMapping("/{id}")
    public Message getMessageById(@PathVariable Long id) {
        return service.getMessageById(id);
    }

    /**
     * Endpoint para crear un nuevo mensaje.
     * Recibe un JSON en el cuerpo de la petición.
     * 
     * @param message Objeto Message recibido
     * @return El mensaje guardado
     */
    @PostMapping(consumes = "application/json", produces = "application/json")
    public Message createMessage(@RequestBody Message message) {
        return service.saveMessage(message);
    }

    /**
     * Endpoint para actualizar un mensaje existente.
     * 
     * @param id             Identificador del mensaje a actualizar
     * @param messageDetails Nuevos datos del mensaje
     * @return El mensaje actualizado
     */
    @PutMapping("/{id}")
    public Message updateMessage(@PathVariable Long id, @RequestBody Message messageDetails) {
        return service.updateMessage(id, messageDetails);
    }

    /**
     * Endpoint para eliminar un mensaje.
     * 
     * @param id Identificador del mensaje a borrar
     * @return true si se borró, false si no
     */
    @DeleteMapping("/{id}")
    public boolean deleteMessage(@PathVariable Long id) {
        return service.deleteMessage(id);
    }
}