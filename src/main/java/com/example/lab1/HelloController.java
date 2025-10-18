package com.example.lab1;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Пинг понг для теста через спринг Hello";
    }

    @GetMapping("/secure")
    public String secure() {
        return "ecure endpoint тест !";
    }
}