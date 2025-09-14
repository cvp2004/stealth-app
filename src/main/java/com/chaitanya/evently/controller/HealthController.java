package com.chaitanya.evently.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HealthController {

    @GetMapping("/")
    public String home() {
        return "Service is live 🎉";
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
