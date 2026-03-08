package com.mzfuture.entire.auth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.mzfuture.entire.auth.dto.FullAuthUser;
import com.mzfuture.entire.auth.dto.LoginRequest;
import com.mzfuture.entire.config.AppProperties;
import com.mzfuture.entire.config.JwtProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.mzfuture.entire.common.exception.Errors;

import java.util.Date;

/// Authentication service
@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final String USAGE_ACCESS = "access";
    private static final String USAGE_REFRESH = "refresh";

    private final JwtProperties jwtProps;
    private final AppProperties appProps;

    public AuthService(JwtProperties jwtProps, AppProperties appProps) {
        this.jwtProps = jwtProps;
        this.appProps = appProps;
    }

/// Login
    public FullAuthUser login(LoginRequest request) {
        // Validate username and password
        if (!appProps.getUsername().equals(request.getUsername()) || !appProps.getPassword().equals(request.getPassword())) {
            throw Errors.INTERNAL_ERROR.toException("Invalid username or password");
        }

        // Generate token
        String accessToken = generateAccessToken(request.getUsername());
        String refreshToken = generateRefreshToken(request.getUsername());

        long expire = System.currentTimeMillis() + jwtProps.getAccessTokenTtl();
        long refreshExpire = System.currentTimeMillis() + jwtProps.getRefreshTokenTtl();

        return new FullAuthUser(request.getUsername(), accessToken, expire, refreshToken, refreshExpire);
    }

    /// Refresh token
    public FullAuthUser refresh(String refreshToken) {
        try {
            // Verify refresh token
            DecodedJWT decodedJWT = verifyToken(refreshToken, USAGE_REFRESH);

            // Get username
            String username = decodedJWT.getSubject();

            // Generate new token
            String newAccessToken = generateAccessToken(username);
            String newRefreshToken = generateRefreshToken(username);

            long expire = System.currentTimeMillis() + jwtProps.getAccessTokenTtl();
            long refreshExpire = System.currentTimeMillis() + jwtProps.getRefreshTokenTtl();

            return new FullAuthUser(username, newAccessToken, expire, newRefreshToken, refreshExpire);
        } catch (JWTVerificationException e) {
            logger.warn("Refresh token verification failed: {}", e.getMessage());
            throw Errors.INTERNAL_ERROR.toException("Refresh token is invalid or expired");
        }
    }

    /// Validate access token
    public String validateAccessToken(String token) {
        try {
            DecodedJWT decodedJWT = verifyToken(token, USAGE_ACCESS);
            return decodedJWT.getSubject();
        } catch (JWTVerificationException e) {
            logger.warn("Access token verification failed: {}", e.getMessage());
            throw Errors.INTERNAL_ERROR.toException("Access token is invalid or expired");
        }
    }

    /// Generate Access Token
    private String generateAccessToken(String username) {
        Algorithm algo = Algorithm.HMAC256(jwtProps.getSecret());
        return JWT.create().withSubject(username).withIssuer(jwtProps.getIssuer()).withExpiresAt(new Date(System.currentTimeMillis() + jwtProps.getAccessTokenTtl())).withClaim("for", USAGE_ACCESS).sign(algo);
    }

    /// Generate Refresh Token
    private String generateRefreshToken(String username) {
        Algorithm algo = Algorithm.HMAC256(jwtProps.getSecret());
        return JWT.create().withSubject(username).withIssuer(jwtProps.getIssuer()).withExpiresAt(new Date(System.currentTimeMillis() + jwtProps.getRefreshTokenTtl())).withClaim("for", USAGE_REFRESH).sign(algo);
    }

    /// Verify token
    private DecodedJWT verifyToken(String token, String expectedUsage) {
        Algorithm algo = Algorithm.HMAC256(jwtProps.getSecret());
        DecodedJWT decodedJWT = JWT.require(algo).withIssuer(jwtProps.getIssuer()).build().verify(token);

        // Verify usage claim
        String usage = decodedJWT.getClaim("for").asString();
        if (!expectedUsage.equals(usage)) {
            throw Errors.INVALID_ARGUMENT.toException("Token usage does not match");
        }

        return decodedJWT;
    }
}

