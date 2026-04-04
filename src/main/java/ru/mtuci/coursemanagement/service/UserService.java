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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$");

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public boolean isPasswordStrong(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    public User register(String username, String rawPassword) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Логин пустой");
        }

        if (findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Пользователь уже есть");
        }

        if (!isPasswordStrong(rawPassword)) {
            throw new IllegalArgumentException("Слабый пароль");
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole("USER");

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Пользователь не найден");
        }
        userRepository.deleteById(id);
    }
}