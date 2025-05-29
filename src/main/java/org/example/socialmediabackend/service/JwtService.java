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

        String username = extractClaim(token, Claims::getSubject);
        log.info("Successfully extracted username from token: {}", username);
        return username;
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        log.info("Extracting claim from JWT token");

        final Claims claims = extractAllClaims(token);
        T claim = claimsResolver.apply(claims);
        log.info("Successfully extracted claim from token");
        return claim;
    }

    public String generateToken(UserDetails userDetails) {
        log.info("Generating JWT token for user: {}", userDetails.getUsername());

        String token = generateToken(new HashMap<>(), userDetails);
        log.info("Successfully generated JWT token for user: {}", userDetails.getUsername());
        return token;
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        log.info("Generating JWT token with extra claims for user: {}", userDetails.getUsername());

        String token = buildToken(extraClaims, userDetails, jwtExpiration);
        log.info("Successfully generated JWT token with extra claims for user: {}", userDetails.getUsername());
        return token;
    }

    public long getExpirationTime() {
        log.info("Getting JWT expiration time");

        log.info("Successfully retrieved JWT expiration time: {} ms", jwtExpiration);
        return jwtExpiration;
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        log.info("Building JWT token for user: {}", userDetails.getUsername());

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
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        log.info("Validating JWT token for user: {}", userDetails.getUsername());

        final String username = extractUsername(token);
        boolean isValid = (username.equals(userDetails.getUsername())) && !isTokenExpired(token);

        log.info("JWT token validation result for user {}: {}", userDetails.getUsername(), isValid);
        return isValid;
    }

    private boolean isTokenExpired(String token) {
        log.info("Checking if JWT token is expired");

        boolean expired = extractExpiration(token).before(new Date());
        log.info("JWT token expiration check result: {}", expired ? "expired" : "valid");
        return expired;
    }

    private Date extractExpiration(String token) {
        log.info("Extracting expiration date from JWT token");

        Date expiration = extractClaim(token, Claims::getExpiration);
        log.info("Successfully extracted expiration date from token");
        return expiration;
    }

    private Claims extractAllClaims(String token) {
        log.info("Extracting all claims from JWT token");

        Claims claims = Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        log.info("Successfully extracted all claims from token");
        return claims;
    }

    private Key getSignInKey() {
        log.info("Getting signing key for JWT");

        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        Key key = Keys.hmacShaKeyFor(keyBytes);
        log.info("Successfully retrieved signing key for JWT");
        return key;
    }
}