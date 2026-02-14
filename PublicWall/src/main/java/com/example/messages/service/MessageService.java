package com.example.messages.service;

import com.example.messages.model.Message;
import com.example.messages.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class MessageService {
    @Autowired
    private MessageRepository repository;
    public List<Message> getAllMessages() {
        return repository.findAll(); }
    public Message saveMessage(Message message) {
        return repository.save(message); }
}