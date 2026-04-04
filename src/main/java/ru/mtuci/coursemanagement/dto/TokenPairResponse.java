package ru.mtuci.coursemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenPairResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long accessExpiresInSeconds;
    private long refreshExpiresInSeconds;
    private Long sessionId;
}
