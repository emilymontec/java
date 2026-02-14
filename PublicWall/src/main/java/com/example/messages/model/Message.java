package com.example.messages.model;

import jakarta.persistence.*;

@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String content;
    private String author;

    // Getters y Setters
    public Long getId() {
        return id; }
    public void setId(Long id) {
        this.id = id; }
    public String getContent() {
        return content; }
    public void setContent(String content) {
        this.content = content; }
    public String getAuthor() {
        return author; }
    public void setAuthor(String author) {
        this.author = author; }
}