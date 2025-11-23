package com.rideshare.authservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// Import the new props interface below
// import com.rideshare.authservice.security.JwtUtil.JwtProps;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

/**
 * JWT utility for generating and validating RS256 tokens.
 * Properties injected using @ConfigurationProperties.
 */
@Component
public class JwtUtil {

    // Inject the fully populated properties object via constructor
    private final JwtProps jwtProps;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public JwtUtil(JwtProps jwtProps) {
        this.jwtProps = jwtProps;
    }

    @PostConstruct
    public void init() {
        try {
            var kf = KeyFactory.getInstance("RSA");

            // Use the values from the props object
            var privClean = jwtProps.privateKey()
                    .replaceAll("-----\\w+ PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            var pubClean = jwtProps.publicKey()
                    .replaceAll("-----\\w+ PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] privBytes = Base64.getDecoder().decode(privClean);
            byte[] pubBytes = Base64.getDecoder().decode(pubClean);

            privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privBytes));
            publicKey = kf.generatePublic(new X509EncodedKeySpec(pubBytes));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load RSA keys for JWT", e);
        }
    }

    /**
     * Generate RS256-signed JWT with provided subject and claims.
     */
    public String generateToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(subject)
                .addClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(jwtProps.expirationSeconds()))) // Use the props value
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    /**
     * Validate a token (throws exception if invalid). Returns true if valid.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    // --- Configuration Properties Definition ---
    // Define a record (immutable data holder) for the properties
    // This assumes your properties start with "security.jwt"
    @ConfigurationProperties(prefix = "spring.security.jwt")
    public record JwtProps(
            String privateKey,
            String publicKey,
            // The default value handling moves to your YAML/properties file if using this approach
            long expirationSeconds
    ) {}
}
