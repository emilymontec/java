package com.example.messages.model;

import jakarta.persistence.*;

/**
 * Entidad que representa un mensaje en la base de datos.
 */
@Entity
public class Message {

    // Identificador único generado automáticamente
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Contenido del mensaje
    private String content;

    // Autor del mensaje
    private String author;

    // Constructor vacío requerido por JPA
    public Message() {}

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}