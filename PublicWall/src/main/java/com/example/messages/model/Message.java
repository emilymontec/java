package com.example.messages.model;

import jakarta.persistence.*;

/**
 * Entidad que representa un mensaje publicado en el muro.
 * Esta clase se mapea a una tabla en la base de datos gracias a la
 * anotación @Entity.
 */
@Entity
public class Message {

    /**
     * Identificador único del mensaje.
     * 
     * @Id indica que es la llave primaria.
     * @GeneratedValue define que el ID se genera automáticamente (incrementable).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * El texto o contenido del mensaje que el usuario desea publicar.
     */
    private String content;

    /**
     * El nombre del usuario o autor que escribió el mensaje.
     */
    private String author;

    /**
     * Fecha y hora en que se creó el mensaje.
     */
    @Column(nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    /**
     * Método que se ejecuta antes de persistir la entidad.
     * Asigna la fecha y hora actual automáticamente.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }

    /**
     * Constructor predeterminado sin argumentos.
     * Es obligatorio para que JPA pueda instanciar la entidad.
     */
    public Message() {
    }

    /**
     * Obtiene el identificador del mensaje.
     * 
     * @return El ID numérico.
     */
    public Long getId() {
        return id;
    }

    /**
     * Establece el identificador del mensaje.
     * 
     * @param id El nuevo ID.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Obtiene el contenido textual del mensaje.
     * 
     * @return El texto del mensaje.
     */
    public String getContent() {
        return content;
    }

    /**
     * Define el contenido del mensaje.
     * 
     * @param content El texto a guardar.
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Obtiene el autor del mensaje.
     * 
     * @return Nombre del autor.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Define quién escribió el mensaje.
     * 
     * @param author Nombre del autor.
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Obtiene la fecha de creación del mensaje.
     * 
     * @return La fecha y hora.
     */
    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Establece la fecha de creación del mensaje.
     * 
     * @param createdAt La fecha y hora.
     */
    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}