package ru.mtuci.coursemanagement.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/jwt")
public class JwtProbeController {

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "ok");
        body.put("username", authentication.getName());
        body.put("authorities", authentication.getAuthorities());
        return body;
    }

    @GetMapping("/admin/ping")
    public Map<String, Object> adminPing(Authentication authentication) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "ok");
        body.put("message", "Доступ для ADMIN есть");
        body.put("username", authentication.getName());
        return body;
    }
}
