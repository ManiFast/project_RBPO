package ru.mtuci.coursemanagement.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.mtuci.coursemanagement.model.User;
import ru.mtuci.coursemanagement.service.UserService;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService users;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           RedirectAttributes redirectAttributes) {
        try {
            users.register(username, password);
            redirectAttributes.addFlashAttribute("success", "Регистрация готова");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Не удалось создать пользователя");
            return "redirect:/login";
        }
    }

    @PostMapping("/api/auth/register")
    @ResponseBody
    public ResponseEntity<?> apiRegister(@RequestParam String username,
                                         @RequestParam String password) {
        try {
            User savedUser = users.register(username, password);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "ok");
            response.put("message", "Готово");
            response.put("userId", savedUser.getId());
            response.put("username", savedUser.getUsername());
            response.put("role", savedUser.getRole());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            if ("Логин занят".equals(e.getMessage())) {
                return ResponseEntity.status(409).body(response);
            }
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Ошибка");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
