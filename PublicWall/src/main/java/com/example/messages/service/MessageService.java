package com.example.messages.service;

import com.example.messages.model.Message;
import com.example.messages.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio encargado de la lógica de negocio de los mensajes.
 * Actúa como intermediario entre el controlador y el repositorio,
 * permitiendo realizar validaciones o procesamientos adicionales.
 */
@Service
public class MessageService {

    // Repositorio inyectado para acceder a la base de datos
    @Autowired
    private MessageRepository repository;

    /**
     * Obtiene todos los mensajes almacenados.
     * 
     * @return Lista de mensajes
     */
    public List<Message> getAllMessages() {
        return repository.findAll();
    }

    /**
     * Obtiene un mensaje específico por su ID.
     * 
     * @param id Identificador del mensaje
     * @return El mensaje encontrado o null si no existe
     */
    public Message getMessageById(Long id) {
        return repository.findById(id).orElse(null);
    }

    /**
     * Guarda un nuevo mensaje en la base de datos.
     * 
     * @param message Mensaje a guardar
     * @return El mensaje guardado con su ID generado
     */
    public Message saveMessage(Message message) {
        return repository.save(message);
    }

    /**
     * Actualiza un mensaje existente.
     * 
     * @param id             Identificador del mensaje a actualizar
     * @param messageDetails Nuevos detalles del mensaje
     * @return El mensaje actualizado o null si no se encontró
     */
    public Message updateMessage(Long id, Message messageDetails) {
        Message message = repository.findById(id).orElse(null);
        if (message != null) {
            message.setContent(messageDetails.getContent());
            message.setAuthor(messageDetails.getAuthor());
            return repository.save(message);
        }
        return null;
    }

    /**
     * Elimina un mensaje por su ID.
     * 
     * @param id Identificador del mensaje a eliminar
     * @return true si se eliminó, false si no se encontró
     */
    public boolean deleteMessage(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}