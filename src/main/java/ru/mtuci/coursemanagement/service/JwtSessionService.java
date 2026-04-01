package ru.mtuci.coursemanagement.service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mtuci.coursemanagement.dto.TokenPairResponse;
import ru.mtuci.coursemanagement.model.SessionStatus;
import ru.mtuci.coursemanagement.model.User;
import ru.mtuci.coursemanagement.model.UserSession;
import ru.mtuci.coursemanagement.repository.UserRepository;
import ru.mtuci.coursemanagement.repository.UserSessionRepository;
import ru.mtuci.coursemanagement.security.jwt.JwtTokenProvider;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class JwtSessionService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public TokenPairResponse login(String username, String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
        } catch (BadCredentialsException ex) {
            throw new IllegalArgumentException("Неверный логин или пароль");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        String accessJti = UUID.randomUUID().toString();
        String refreshJti = UUID.randomUUID().toString();

        UserSession session = new UserSession();
        session.setUser(user);
        session.setAccessJti(accessJti);
        session.setRefreshJti(refreshJti);
        session.setStatus(SessionStatus.ACTIVE);
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTtlSeconds()));

        session = userSessionRepository.save(session);

        String accessToken = jwtTokenProvider.generateAccessToken(user, session.getId(), accessJti);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user, session.getId(), refreshJti);

        return new TokenPairResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenProvider.getAccessTtlSeconds(),
                jwtTokenProvider.getRefreshTtlSeconds(),
                session.getId()
        );
    }

    public TokenPairResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.isValidRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Refresh токен недействителен");
        }

        Long sessionId = jwtTokenProvider.getSessionId(refreshToken);
        String refreshJti = jwtTokenProvider.getTokenId(refreshToken);

        UserSession currentSession = userSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Сессия не найдена"));

        if (currentSession.getStatus() != SessionStatus.ACTIVE) {
            throw new IllegalArgumentException("Refresh токен уже использован");
        }

        if (!refreshJti.equals(currentSession.getRefreshJti())) {
            throw new IllegalArgumentException("Refresh токен не совпадает");
        }

        if (currentSession.getExpiresAt().isBefore(LocalDateTime.now())) {
            currentSession.setStatus(SessionStatus.EXPIRED);
            currentSession.setUpdatedAt(LocalDateTime.now());
            userSessionRepository.save(currentSession);
            throw new IllegalArgumentException("Сессия уже истекла");
        }

        User user = currentSession.getUser();

        currentSession.setStatus(SessionStatus.REFRESHED);
        currentSession.setUpdatedAt(LocalDateTime.now());
        userSessionRepository.save(currentSession);

        String newAccessJti = java.util.UUID.randomUUID().toString();
        String newRefreshJti = java.util.UUID.randomUUID().toString();

        UserSession newSession = new UserSession();
        newSession.setUser(user);
        newSession.setAccessJti(newAccessJti);
        newSession.setRefreshJti(newRefreshJti);
        newSession.setStatus(SessionStatus.ACTIVE);
        newSession.setCreatedAt(LocalDateTime.now());
        newSession.setUpdatedAt(LocalDateTime.now());
        newSession.setExpiresAt(LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTtlSeconds()));
        newSession = userSessionRepository.save(newSession);

        String newAccessToken = jwtTokenProvider.generateAccessToken(user, newSession.getId(), newAccessJti);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user, newSession.getId(), newRefreshJti);

        return new TokenPairResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                jwtTokenProvider.getAccessTtlSeconds(),
                jwtTokenProvider.getRefreshTtlSeconds(),
                newSession.getId()
        );
    }
}
