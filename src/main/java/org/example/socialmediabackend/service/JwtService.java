package org.example.socialmediabackend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {
    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    public String extractUsername(String token) {
        log.info("Extracting username from JWT token");

        try {
            String username = extractClaim(token, Claims::getSubject);
            log.info("Successfully extracted username from token: {}", username);
            return username;
        } catch (Exception e) {
            log.error("Failed to extract username from token", e);
            throw e;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        log.info("Extracting claim from JWT token");

        try {
            final Claims claims = extractAllClaims(token);
            T claim = claimsResolver.apply(claims);
            log.info("Successfully extracted claim from token");
            return claim;
        } catch (Exception e) {
            log.error("Failed to extract claim from token", e);
            throw e;
        }
    }

    public String generateToken(UserDetails userDetails) {
        log.info("Generating JWT token for user: {}", userDetails.getUsername());

        try {
            String token = generateToken(new HashMap<>(), userDetails);
            log.info("Successfully generated JWT token for user: {}", userDetails.getUsername());
            return token;
        } catch (Exception e) {
            log.error("Failed to generate JWT token for user: {}", userDetails.getUsername(), e);
            throw e;
        }
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        log.info("Generating JWT token with extra claims for user: {}", userDetails.getUsername());

        try {
            String token = buildToken(extraClaims, userDetails, jwtExpiration);
            log.info("Successfully generated JWT token with extra claims for user: {}", userDetails.getUsername());
            return token;
        } catch (Exception e) {
            log.error("Failed to generate JWT token with extra claims for user: {}", userDetails.getUsername(), e);
            throw e;
        }
    }

    public long getExpirationTime() {
        log.info("Getting JWT expiration time");

        try {
            log.info("Successfully retrieved JWT expiration time: {} ms", jwtExpiration);
            return jwtExpiration;
        } catch (Exception e) {
            log.error("Failed to get JWT expiration time", e);
            throw e;
        }
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        log.info("Building JWT token for user: {}", userDetails.getUsername());

        try {
            String token = Jwts
                    .builder()
                    .setClaims(extraClaims)
                    .setSubject(userDetails.getUsername())
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + expiration))
                    .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                    .compact();

            log.info("Successfully built JWT token for user: {}", userDetails.getUsername());
            return token;
        } catch (Exception e) {
            log.error("Failed to build JWT token for user: {}", userDetails.getUsername(), e);
            throw e;
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        log.info("Validating JWT token for user: {}", userDetails.getUsername());

        try {
            final String username = extractUsername(token);
            boolean isValid = (username.equals(userDetails.getUsername())) && !isTokenExpired(token);

            log.info("JWT token validation result for user {}: {}", userDetails.getUsername(), isValid);
            return isValid;
        } catch (Exception e) {
            log.error("Failed to validate JWT token for user: {}", userDetails.getUsername(), e);
            throw e;
        }
    }

    private boolean isTokenExpired(String token) {
        log.info("Checking if JWT token is expired");

        try {
            boolean expired = extractExpiration(token).before(new Date());
            log.info("JWT token expiration check result: {}", expired ? "expired" : "valid");
            return expired;
        } catch (Exception e) {
            log.error("Failed to check JWT token expiration", e);
            throw e;
        }
    }

    private Date extractExpiration(String token) {
        log.info("Extracting expiration date from JWT token");

        try {
            Date expiration = extractClaim(token, Claims::getExpiration);
            log.info("Successfully extracted expiration date from token");
            return expiration;
        } catch (Exception e) {
            log.error("Failed to extract expiration date from token", e);
            throw e;
        }
    }

    private Claims extractAllClaims(String token) {
        log.info("Extracting all claims from JWT token");

        try {
            Claims claims = Jwts
                    .parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            log.info("Successfully extracted all claims from token");
            return claims;
        } catch (Exception e) {
            log.error("Failed to extract all claims from token", e);
            throw e;
        }
    }

    private Key getSignInKey() {
        log.info("Getting signing key for JWT");

        try {
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            Key key = Keys.hmacShaKeyFor(keyBytes);
            log.info("Successfully retrieved signing key for JWT");
            return key;
        } catch (Exception e) {
            log.error("Failed to get signing key for JWT", e);
            throw e;
        }
    }
}