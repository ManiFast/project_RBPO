package ru.mtuci.coursemanagement.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
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
    private final StudentRepository studentRepository; // ДОБАВЬТЕ эту строку

    // Веб-страница логина
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // Веб-логин
    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpServletRequest req,
                          Model model) {
        try {
            Optional<User> opt = users.findByUsername(username);
            if (opt.isPresent()) {
                User u = opt.get();
                if (u.getPassword().equals(password)) {
                    log.info("User {} logged in with password {}", username, password);
                    HttpSession s = req.getSession(true);
                    s.setAttribute("username", username);
                    s.setAttribute("role", u.getRole());
                    s.setAttribute("userId", u.getId()); // ВАЖНО: сохраняем ID пользователя
                    return "redirect:/";
                }
            }
            model.addAttribute("error", "Неверные учетные данные");
            return "login";
        } catch (Exception e) {
            log.error("Login error for user {}: {}", username, e.getMessage());
            model.addAttribute("error", "Ошибка при входе: " + e.getMessage());
            return "login";
        }
    }

    // API логин для Postman
    @PostMapping("/api/auth/login")
    @ResponseBody
    public ResponseEntity<?> apiLogin(@RequestParam String username,
                                      @RequestParam String password,
                                      HttpServletRequest req) {
        try {
            Optional<User> opt = users.findByUsername(username);
            if (opt.isPresent()) {
                User u = opt.get();
                if (u.getPassword().equals(password)) {
                    log.info("User {} logged in via API", username);
                    HttpSession s = req.getSession(true);
                    s.setAttribute("username", username);
                    s.setAttribute("role", u.getRole());
                    s.setAttribute("userId", u.getId()); // ВАЖНО: сохраняем ID пользователя

                    // Возвращаем JSON ответ
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("message", "Login successful");
                    response.put("username", username);
                    response.put("role", u.getRole());
                    response.put("userId", u.getId());
                    return ResponseEntity.ok(response);
                }
            }

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Invalid credentials");
            return ResponseEntity.status(401).body(errorResponse);

        } catch (Exception e) {
            log.error("API Login error for user {}: {}", username, e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Login error: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // Веб-регистрация
    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam(required = false, defaultValue = "STUDENT") String role,
                           Model model) {
        try {
            // Проверяем, не существует ли уже пользователь с таким именем
            if (users.findByUsername(username).isPresent()) {
                model.addAttribute("error", "Пользователь с таким именем уже существует");
                return "login";
            }

            // Создаем нового пользователя
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(password);
            newUser.setRole(role);

            User savedUser = users.save(newUser);
            log.info("New user registered: {} with role {}", username, role);

            // АВТОМАТИЧЕСКОЕ СОЗДАНИЕ СТУДЕНТА ЕСЛИ РОЛЬ STUDENT
            if ("STUDENT".equals(role)) {
                Student student = new Student();
                student.setName(username);
                student.setEmail(username + "@example.com");
                student.setUserId(savedUser.getId());
                studentRepository.save(student);
                log.info("Student record created for user: {}", username);
            }

            return "redirect:/login?success=Registration successful";

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity error during registration: {}", e.getMessage());
            model.addAttribute("error", "Ошибка базы данных: пользователь с таким именем уже существует");
            return "login";
        } catch (Exception e) {
            log.error("Registration error for user {}: {}", username, e.getMessage());
            model.addAttribute("error", "Ошибка при регистрации: " + e.getMessage());
            return "login";
        }
    }

    // API регистрация для Postman
    @PostMapping("/api/auth/register")
    @ResponseBody
    public ResponseEntity<?> apiRegister(@RequestParam String username,
                                         @RequestParam String password,
                                         @RequestParam(required = false, defaultValue = "STUDENT") String role) {
        try {
            // Проверяем, не существует ли уже пользователь
            if (users.findByUsername(username).isPresent()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("status", "error");
                errorResponse.put("message", "User already exists");
                return ResponseEntity.status(409).body(errorResponse);
            }

            // Создаем нового пользователя
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(password);
            newUser.setRole(role);

            User savedUser = users.save(newUser);
            log.info("New user registered via API: {} with role {}", username, role);

            // АВТОМАТИЧЕСКОЕ СОЗДАНИЕ СТУДЕНТА ЕСЛИ РОЛЬ STUDENT
            if ("STUDENT".equals(role)) {
                Student student = new Student();
                student.setName(username);
                student.setEmail(username + "@example.com");
                student.setUserId(savedUser.getId());
                studentRepository.save(student);
                log.info("Student record created for user: {}", username);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Registration successful");
            response.put("username", username);
            response.put("role", role);
            response.put("userId", savedUser.getId());
            return ResponseEntity.ok(response);

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity error during API registration: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "User already exists");
            return ResponseEntity.status(409).body(errorResponse);
        } catch (Exception e) {
            log.error("API Registration error for user {}: {}", username, e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Registration error: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s != null) s.invalidate();
        return "redirect:/login";
    }

    // API logout для Postman
    @PostMapping("/api/auth/logout")
    @ResponseBody
    public ResponseEntity<?> apiLogout(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s != null) s.invalidate();

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Logout successful");
        return ResponseEntity.ok(response);
    }
}