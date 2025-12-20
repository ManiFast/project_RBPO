package ru.mtuci.coursemanagement.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/api/hello")
    public String home() {
        return "Главная страница API. Проверьте: http://localhost:8080/api/hello/world";
    }

    @GetMapping("/api/hello/world")
    public String sayHello() {
        return "Приветствую из REST API!";
    }

    @GetMapping("/api/hello/user")
    public String helloUser() {
        return "Привет, пользователь!";
    }

    @GetMapping("/api/hello/status")
    public String status() {
        return "API работает корректно!";
    }
}