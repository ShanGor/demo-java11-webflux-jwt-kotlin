package tech.comfortheart.demo.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.comfortheart.demo.config.JwtPaths;

import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Token API filter
 */

@Component
@Order(0)
public class JwtFilter implements WebFilter {
    @Value("${jwt.header.key}")
    private String jwtHeader;
    @Value("${jwt.cert.path}")
    private String certPath;
    @Value("${jwt.cert.type}")
    private String certType;

    @Autowired
    ResourceLoader resourceLoader;

    @Autowired
    JwtPaths jwtPaths;


    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    PublicKey getCert() throws CertificateException, IOException {
        InputStream ins = resourceLoader.getResource(certPath).getInputStream();
        CertificateFactory cf = CertificateFactory.getInstance(certType);
        Certificate cert = cf.generateCertificate(ins);
        PublicKey publicKey = cert.getPublicKey();
        ins.close();
        return publicKey;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
        ServerHttpRequest request = serverWebExchange.getRequest();
        ServerHttpResponse response = serverWebExchange.getResponse();

        String path = request.getPath().pathWithinApplication().value();
        if (jwtPaths != null) {
            List<String> ignorePaths = jwtPaths.getIgnorePaths();
            if (ignorePaths != null) {
                for(String ignorePath:ignorePaths) {
                    if (ignorePath.equals(path)) {
                        return webFilterChain.filter(serverWebExchange);
                    }
                }
            }
        }

        HttpHeaders headers = request.getHeaders();
        if (headers != null) {
            List<String> headersWithKey = headers.get(jwtHeader);
            if (headersWithKey != null && !headersWithKey.isEmpty()) {
                String token = headersWithKey.get(0);
                logger.info("Token: " + token);

                try {
                    Jwts.parser()
                            .setSigningKey(getCert())
                            .parseClaimsJws(token)
                            .getBody();
                    logger.info("JWT validated successfully");
                } catch (JwtException | CertificateException | IOException e) {
                    logger.info("JWT validated failed: " + e.getMessage());

                    if (e instanceof ExpiredJwtException) {
                        return prepareErrorResponse(response, HttpStatus.UNAUTHORIZED,"Token Expired: " + token);
                    } else if (e instanceof JwtException) {
                        return prepareErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid Token: " + token);
                    } else if (e instanceof IOException) {
                        return prepareErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "Server error with failing to get the cert, please contact system admin!");
                    } else if (e instanceof CertificateException) {
                        return prepareErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "Server error with cert integrity issue, please contact system admin!");
                    } else {
                        return prepareErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "Server unknown error, please contact system admin!");
                    }
                }

                return webFilterChain.filter(serverWebExchange);
            }
        }

        return prepareErrorResponse(response, HttpStatus.UNAUTHORIZED, "Missing token in the header!");
    }

    private static final Mono<Void> prepareErrorResponse(final ServerHttpResponse response,
                                            final  HttpStatus responseCode,
                                            final String errorMessage) {
        response.setStatusCode(responseCode);
        response.getHeaders().add("Content-Type", "application/json");
        Map<String, String> error = new LinkedHashMap<>();
        error.put("error", ""+responseCode.value());
        error.put("message", errorMessage);
        DataBuffer buffer = null;
        try {
            buffer = response.bufferFactory().wrap(new ObjectMapper().writeValueAsString(error).getBytes());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return response.writeWith(Flux.just(buffer));
    }
}
