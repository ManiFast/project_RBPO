package ru.mtuci.coursemanagement.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.coursemanagement.model.Student;
import ru.mtuci.coursemanagement.model.User;
import ru.mtuci.coursemanagement.repository.StudentRepository;
import ru.mtuci.coursemanagement.service.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserService users;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    // ==== ВЕБ-СТРАНИЦЫ ====

    @GetMapping("/login")
    public String loginPage(Model model) {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "login"; // Та же страница, что и login
    }

    // ==== ВЕБ-РЕГИСТРАЦИЯ ====
    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam(required = false, defaultValue = "STUDENT") String role,
                           Model model,
                           HttpServletRequest request) {
        try {
            log.info("Attempting registration for user: {}", username);

            // 1. Проверка существования пользователя
            if (users.findByUsername(username).isPresent()) {
                model.addAttribute("error", "Пользователь с таким именем уже существует");
                return "login";
            }

            // 2. Проверка пароля
            if (!isPasswordValid(password)) {
                model.addAttribute("error", "Пароль должен содержать минимум 6 символов, включая цифры и буквы");
                return "login";
            }

            // 3. Хеширование пароля
            String encodedPassword = passwordEncoder.encode(password);

            // 4. Создание пользователя
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(encodedPassword);
            newUser.setRole(role);

            User savedUser = users.save(newUser);
            log.info("New user registered: {} with role {}", username, role);

            // 5. Автоматическое создание студента
            if ("STUDENT".equals(role)) {
                Student student = new Student();
                student.setName(username);
                student.setEmail(username + "@example.com");
                student.setUserId(savedUser.getId());
                studentRepository.save(student);
                log.info("Student record created for user: {}", username);
            }

            // 6. Автоматический вход после регистрации
            HttpSession session = request.getSession(true);
            session.setAttribute("username", username);
            session.setAttribute("role", role);
            session.setAttribute("userId", savedUser.getId());

            return "redirect:/";

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity error: {}", e.getMessage());
            model.addAttribute("error", "Пользователь с таким именем уже существует");
            return "login";
        } catch (Exception e) {
            log.error("Registration error: {}", e.getMessage());
            model.addAttribute("error", "Ошибка при регистрации: " + e.getMessage());
            return "login";
        }
    }

    // ==== API РЕГИСТРАЦИЯ ====
    @PostMapping("/api/auth/register")
    @ResponseBody
    public ResponseEntity<?> apiRegister(@RequestParam String username,
                                         @RequestParam String password,
                                         @RequestParam(required = false, defaultValue = "STUDENT") String role) {
        try {
            log.info("API registration attempt for: {}", username);

            if (users.findByUsername(username).isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "User already exists");
                return ResponseEntity.status(409).body(error);
            }

            if (!isPasswordValid(password)) {
                Map<String, String> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "Password must contain at least 6 characters with numbers and letters");
                return ResponseEntity.status(400).body(error);
            }

            String encodedPassword = passwordEncoder.encode(password);

            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(encodedPassword);
            newUser.setRole(role);

            User savedUser = users.save(newUser);
            log.info("API registration successful: {}", username);

            if ("STUDENT".equals(role)) {
                Student student = new Student();
                student.setName(username);
                student.setEmail(username + "@example.com");
                student.setUserId(savedUser.getId());
                studentRepository.save(student);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Registration successful");
            response.put("username", username);
            response.put("role", role);
            response.put("userId", savedUser.getId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("API registration error: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Registration error: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // ==== ВЕБ-ЛОГАУТ ====
    @GetMapping("/logout")
    public String logout(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login?logout";
    }

    // ==== API ЛОГАУТ ====
    @PostMapping("/api/auth/logout")
    @ResponseBody
    public ResponseEntity<?> apiLogout(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Logout successful");
        return ResponseEntity.ok(response);
    }

    // ==== МЕТОД ПРОВЕРКИ ПАРОЛЯ ====
    private boolean isPasswordValid(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }

        boolean hasDigit = false;
        boolean hasLetter = false;

        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) hasDigit = true;
            if (Character.isLetter(c)) hasLetter = true;
            if (hasDigit && hasLetter) return true;
        }

        return false;
    }
}