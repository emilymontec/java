package com.example.messages.controller;

import com.example.messages.model.Message;
import com.example.messages.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController {
    @Autowired
    private MessageService service;

    @GetMapping
    public List<Message> getMessages() {
        return service.getAllMessages(); }

    @PostMapping
    public Message createMessage(@RequestBody Message message) {
        return service.saveMessage(message);
    }
}