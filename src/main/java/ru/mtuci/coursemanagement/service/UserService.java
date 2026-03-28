package ru.mtuci.coursemanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.mtuci.coursemanagement.model.Student;
import ru.mtuci.coursemanagement.model.User;
import ru.mtuci.coursemanagement.repository.StudentRepository;
import ru.mtuci.coursemanagement.repository.UserRepository;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Pattern STRONG_PASSWORD = Pattern.compile(
            "^(?=.*[a-zа-я])(?=.*[A-ZА-Я])(?=.*\\d)(?=.*[^A-Za-zА-Яа-я\\d]).{8,}$"
    );

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean isPasswordStrong(String password) {
        return password != null && STRONG_PASSWORD.matcher(password).matches();
    }

    public User register(String username, String rawPassword) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Введите логин");
        }

        String normalizedUsername = username.trim();

        if (userRepository.findByUsername(normalizedUsername).isPresent()) {
            throw new IllegalArgumentException("Логин занят");
        }

        if (!isPasswordStrong(rawPassword)) {
            throw new IllegalArgumentException("Слабый пароль");
        }

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole("USER");

        User savedUser = userRepository.save(user);

        Student student = new Student();
        student.setName(normalizedUsername);
        student.setEmail(normalizedUsername + "@example.com");
        student.setUserId(savedUser.getId());
        studentRepository.save(student);

        return savedUser;
    }
}
