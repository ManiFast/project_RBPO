package ru.mtuci.coursemanagement.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.coursemanagement.dto.AuthLoginRequest;
import ru.mtuci.coursemanagement.dto.RefreshTokenRequest;
import ru.mtuci.coursemanagement.dto.TokenPairResponse;
import ru.mtuci.coursemanagement.service.JwtSessionService;
import ru.mtuci.coursemanagement.service.UserService;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtSessionService jwtSessionService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/register")
    public String registerFromPage(@RequestParam String username,
                                   @RequestParam String password,
                                   Model model) {
        try {
            userService.registerUser(username, password);
            return "redirect:/login?success=Регистрация прошла";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "login";
        }
    }

    @PostMapping("/api/auth/register")
    @ResponseBody
    public ResponseEntity<?> registerApi(@RequestParam String username,
                                         @RequestParam String password) {
        try {
            userService.registerUser(username, password);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", "success");
            body.put("message", "Пользователь создан");
            body.put("username", username);

            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(message(ex.getMessage()));
        }
    }

    @PostMapping("/auth/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody AuthLoginRequest request) {
        try {
            TokenPairResponse response = jwtSessionService.login(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(message(ex.getMessage()));
        }
    }

    @PostMapping("/auth/refresh")
    @ResponseBody
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest request) {
        try {
            TokenPairResponse response = jwtSessionService.refresh(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(message(ex.getMessage()));
        }
    }

    private Map<String, Object> message(String text) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "error");
        body.put("message", text);
        return body;
    }
}
