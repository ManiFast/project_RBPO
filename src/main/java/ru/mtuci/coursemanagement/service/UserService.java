package ru.mtuci.coursemanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.mtuci.coursemanagement.model.User;
import ru.mtuci.coursemanagement.repository.UserRepository;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-zа-я])(?=.*[A-ZА-Я])(?=.*\\d)(?=.*[^A-Za-zА-Яа-я\\d]).{8,}$"
    );

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username == null ? null : username.trim());
    }

    public User registerUser(String username, String rawPassword) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Введите логин");
        }

        String normalizedUsername = username.trim();

        if (userRepository.findByUsername(normalizedUsername).isPresent()) {
            throw new IllegalArgumentException("Такой логин уже занят");
        }

        validatePassword(rawPassword);

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole("USER");

        return userRepository.save(user);
    }

    public void validatePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Введите пароль");
        }
        if (!PASSWORD_PATTERN.matcher(rawPassword).matches()) {
            throw new IllegalArgumentException("Пароль слишком простой");
        }
    }
}
