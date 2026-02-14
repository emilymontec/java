package com.example.messages.repository;

import com.example.messages.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para la entidad Message.
 * Proporciona métodos CRUD básicos automáticamente.
 */
public interface MessageRepository extends JpaRepository<Message, Long> {
    
}