package com.example.messages.service;

import com.example.messages.model.Message;
import com.example.messages.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * Servicio para manejar la lógica de negocio de los mensajes.
 */
@Service
public class MessageService {

    // Repositorio inyectado para acceder a la base de datos
    @Autowired
    private MessageRepository repository;

    /**
     * Obtiene todos los mensajes almacenados.
     * @return Lista de mensajes
     */
    public List<Message> getAllMessages() {
        return repository.findAll();
    }

    /**
     * Guarda un nuevo mensaje en la base de datos.
     * @param message Mensaje a guardar
     * @return El mensaje guardado con su ID generado
     */
    public Message saveMessage(Message message) {
        return repository.save(message);
    }
}