package com.javawiz.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.jms.Queue;

@Service
@RequiredArgsConstructor
public class Publisher {
    private final Queue queue;
    private final JmsTemplate jmsTemplate;

    public String publish(String message){
        jmsTemplate.convertAndSend(queue, message);
        return message;
    }
}
