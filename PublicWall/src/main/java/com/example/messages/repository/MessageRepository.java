package com.example.messages.repository;

import com.example.messages.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para la entidad Message.
 * Al extender JpaRepository, Spring Data JPA genera automáticamente
 * la implementación con métodos como save(), findAll(), findById(),
 * deleteById(), etc.
 */
public interface MessageRepository extends JpaRepository<Message, Long> {
    // No es necesario escribir código aquí para operaciones CRUD básicas.
}