package com.javawiz.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@EnableJms
public class Subscriber {
    private final Logger logger = LoggerFactory.getLogger(Subscriber.class);

    @JmsListener(destination = "sample-queue")
    public void listener(String message) {
        logger.info("Message received {} ", message);
    }
}