package com.javawiz.controller;

import com.javawiz.service.Publisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PublisherController {

    private final Publisher publisher;

    @GetMapping("message/{message}")
    public ResponseEntity<String> publish(@PathVariable("message") final String message) {
        return new ResponseEntity<>(publisher.publish(message), HttpStatus.OK);
    }
}