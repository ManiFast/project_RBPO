package com.example.lab1;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Arrays;
import java.util.List;

@RestController
public class UserController {

    @GetMapping("/users")
    public List<String> users() {
        return Arrays.asList("Andreson", "Jadeney", "Cellane", "Rozayna");
    }
}