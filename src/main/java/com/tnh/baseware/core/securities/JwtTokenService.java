package com.tnh.baseware.core.securities;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.tnh.baseware.core.components.JwtSecretProvider;
import com.tnh.baseware.core.entities.user.CustomUserDetails;
import com.tnh.baseware.core.entities.user.Token;
import com.tnh.baseware.core.enums.TokenType;
import com.tnh.baseware.core.exceptions.BWCNotFoundException;
import com.tnh.baseware.core.properties.SecurityProperties;
import com.tnh.baseware.core.repositories.user.ITokenRepository;
import com.tnh.baseware.core.repositories.user.IUserRepository;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.utils.BasewareUtils;
import com.tnh.baseware.core.utils.LogStyleHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.KeyGenerator;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class JwtTokenService {

    ITokenRepository tokenRepository;
    IUserRepository userRepository;
    SecurityProperties securityProperties;
    JwtSecretProvider jwtSecretProvider;
    MessageService messageService;

    public static void main(String[] args) {
        try {
            var keyGenerator = KeyGenerator.getInstance("HmacSHA512");
            keyGenerator.init(512);

            var secretKey = keyGenerator.generateKey();
            var base64EncodedSecretKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
            log.info(LogStyleHelper.info("Generated HS512 Secret Key (Base64 encoded): {}"), base64EncodedSecretKey);
        } catch (Exception e) {
            log.error(LogStyleHelper.error("Error occurred while generating HS512 Secret Key"), e);
        }
    }

    public Optional<String> extractTenantId(String token) {
        return extractClaim(token, claims -> claims.getClaim("t").toString());
    }

    public Optional<String> extractSessionId(String token) {
        return extractClaim(token, claims -> claims.getClaim("sid").toString());
    }

    public Optional<String> extractUsername(String token) {
        return extractClaim(token, JWTClaimsSet::getSubject);
    }

    public Optional<String> extractJti(String token) {
        return extractClaim(token, JWTClaimsSet::getJWTID);
    }

    private <T> Optional<T> extractClaim(String token, Function<JWTClaimsSet, T> claimsResolver) {
        try {
            var signedJWT = SignedJWT.parse(token);
            return Optional.ofNullable(claimsResolver.apply(signedJWT.getJWTClaimsSet()));
        } catch (ParseException e) {
            log.error(LogStyleHelper.error("Error occurred while parsing JWT: {}"), e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<String> generateToken(CustomUserDetails userDetails, HttpServletRequest request, UUID sessionId, String tenantName) {
        return generateToken(new HashMap<>(), userDetails, request, sessionId, tenantName);
    }

    private Optional<String> generateToken(Map<String, Object> extraClaims, CustomUserDetails userDetails, HttpServletRequest request, UUID sessionId, String tenantName) {
        return buildToken(extraClaims, userDetails, securityProperties.getJwt().getExpiration(), TokenType.ACCESS.getValue(), request, sessionId, tenantName);
    }

    public Optional<String> generateRefreshToken(CustomUserDetails userDetails, HttpServletRequest request, UUID sessionId, String tenantName) {
        return buildToken(new HashMap<>(), userDetails, securityProperties.getJwt().getRefreshExpiration(), TokenType.REFRESH.getValue(), request, sessionId, tenantName);
    }

    private Optional<String> buildToken(Map<String, Object> extraClaims, CustomUserDetails userDetails, long expiration, String tokenType, HttpServletRequest request, UUID sessionId, String tenantName) {
        try {
            var header = new JWSHeader(JWSAlgorithm.HS512);
            var ip = request.getRemoteAddr();
            var userAgent = request.getHeader("User-Agent");

            var claimsBuilder = new JWTClaimsSet.Builder()
                    .subject(userDetails.getUsername())
                    .issuer(securityProperties.getJwt().getIssuer())
                    .issueTime(new Date())
                    .expirationTime(Date.from(Instant.now().plusMillis(expiration)))
                    .jwtID(UUID.randomUUID().toString())
                    .claim("sid", String.valueOf(sessionId))
                    .claim("t", tenantName);
            extraClaims.forEach(claimsBuilder::claim);

            var claimsSet = claimsBuilder.build();
            var signedJWT = new SignedJWT(header, claimsSet);
            signedJWT.sign(new MACSigner(jwtSecretProvider.getDecodedSecretKey()));

            var token = Token.builder()
                    .jti(claimsSet.getJWTID())
                    .sessionId(sessionId)
                    .tokenType(tokenType)
                    .ipAddress(ip)
                    .deviceId(BasewareUtils.generateDeviceId(request))
                    .device(BasewareUtils.getDevice(userAgent))
                    .platform(BasewareUtils.getPlatform(userAgent))
                    .browser(BasewareUtils.getBrowser(userAgent))
                    .revoked(false)
                    .expired(false)
                    .user(userDetails.getUser())
                    .expiration(Instant.now().plusMillis(expiration))
                    .build();
            tokenRepository.save(token);
            return signedJWT.serialize().describeConstable();
        } catch (JOSEException e) {
            log.error(LogStyleHelper.error("Error occurred while generating JWT: {}"), e.getMessage());
            return Optional.empty();
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isTokenValid(String token, CustomUserDetails userDetails) {
        try {
            var signedJWT = SignedJWT.parse(token);
            if (!signedJWT.verify(new MACVerifier(jwtSecretProvider.getDecodedSecretKey()))) {
                log.debug(LogStyleHelper.debug("JWT signature verification failed"));
                return false;
            }

            var claims = signedJWT.getJWTClaimsSet();
            var username = claims.getSubject();
            var expiration = claims.getExpirationTime();
            var jti = claims.getJWTID();

            if (!Objects.equals(userDetails.getUsername(), username)) {
                log.debug(LogStyleHelper.debug("JWT username mismatch: expected '{}', found '{}'"), userDetails.getUsername(), username);
                return false;
            }

            if (expiration == null || expiration.before(new Date())) {
                log.debug(LogStyleHelper.debug("JWT has expired at {}"), expiration);
                return false;
            }

            if (jti == null) {
                log.debug(LogStyleHelper.debug("JWT ID (jti) is missing"));
                return false;
            }

            return tokenRepository.findByJti(jti)
                    .map(tokenObj -> {
                        if (tokenObj.getRevoked() || tokenObj.getExpired()) {
                            log.debug(LogStyleHelper.debug("Token with jti={} is revoked or expired"), jti);
                            return false;
                        }
                        return true;
                    })
                    .orElseGet(() -> {
                        log.debug(LogStyleHelper.debug("Token with jti={} not found in repository"), jti);
                        return false;
                    });
        } catch (JOSEException | ParseException e) {
            log.error(LogStyleHelper.error("Error occurred while verifying token: {}"), e.getMessage());
            return false;
        }
    }

    @Transactional
    public void revokeAllValidTokensBySessionId(UUID sessionId) {
        tokenRepository.findAllBySessionId(sessionId)
                .forEach(token -> {
                    token.setRevoked(true);
                    token.setExpired(true);
                    tokenRepository.save(token);
                });
    }

    @Transactional
    public void revokeAllValidTokensByUser(UUID userId) {
        var user = userRepository.findById(userId).orElseThrow(() ->
                new BWCNotFoundException(messageService.getMessage("user.not.found", userId)));

        tokenRepository.findAllByUserAndRevokedFalseAndExpiredFalse(user)
                .forEach(token -> {
                    token.setRevoked(true);
                    token.setExpired(true);
                    tokenRepository.save(token);
                });
    }

    @Transactional
    public void revokeAllValidAccessTokensByUser(UUID userId) {
        var user = userRepository.findById(userId).orElseThrow(() ->
                new BWCNotFoundException(messageService.getMessage("user.not.found", userId)));

        tokenRepository.findAllByUserAndRevokedFalseAndExpiredFalse(user)
                .stream()
                .filter(token -> Objects.equals(token.getTokenType(), TokenType.ACCESS.getValue()))
                .forEach(token -> {
                    token.setRevoked(true);
                    token.setExpired(true);
                    tokenRepository.save(token);
                });
    }

    @Transactional
    public void revokeAllValidAccessTokensBySessionId(UUID sessionId) {
        tokenRepository.findAllBySessionId(sessionId)
                .stream()
                .filter(token -> Objects.equals(token.getTokenType(), TokenType.ACCESS.getValue()))
                .forEach(token -> {
                    token.setRevoked(true);
                    token.setExpired(true);
                    tokenRepository.save(token);
                });
    }

    @Transactional
    public void revokeAllValidTokensByUserAndDevice(UUID userId, String deviceId) {
        var user = userRepository.findById(userId).orElseThrow(() ->
                new BWCNotFoundException(messageService.getMessage("user.not.found", userId)));

        tokenRepository.findAllByUserAndDeviceId(user, deviceId)
                .forEach(token -> {
                    token.setRevoked(true);
                    token.setExpired(true);
                    tokenRepository.save(token);
                });
    }
}
