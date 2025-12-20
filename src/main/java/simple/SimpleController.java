package ru.mtuci.coursemanagement.controller;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/demo")
public class SimpleController {

    // 1. Простой GET
    @GetMapping("/info")
    public String getInfo() {
        return "Демонстрационный API контроллер. Время: " + LocalDateTime.now();
    }

    // 2. GET с параметром пути
    @GetMapping("/user/{id}")
    public Map<String, Object> getUser(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", id);
        response.put("name", "Пользователь " + id);
        response.put("email", "user" + id + "@example.com");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    // 3. GET с query параметрами
    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam(required = false) String name,
                                      @RequestParam(required = false, defaultValue = "1") Integer page) {
        Map<String, Object> response = new HashMap<>();
        response.put("query", name != null ? name : "не указан");
        response.put("page", page);
        response.put("results", new String[]{"Результат 1", "Результат 2", "Результат 3"});
        return response;
    }

    // 4. POST запрос
    @PostMapping("/create")
    public Map<String, Object> createItem(@RequestBody Map<String, String> requestData) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "created");
        response.put("message", "Объект успешно создан");
        response.put("data", requestData);
        response.put("id", System.currentTimeMillis()); // имитация ID
        return response;
    }

    // 5. PUT запрос
    @PutMapping("/update/{id}")
    public Map<String, Object> updateItem(@PathVariable Long id, @RequestBody Map<String, String> updates) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "updated");
        response.put("id", id);
        response.put("updates", updates);
        response.put("message", "Объект с ID " + id + " обновлен");
        return response;
    }

    // 6. DELETE запрос
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteItem(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "deleted");
        response.put("id", id);
        response.put("message", "Объект с ID " + id + " удален");
        return response;
    }
}