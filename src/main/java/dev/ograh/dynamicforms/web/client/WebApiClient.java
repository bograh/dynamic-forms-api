package dev.ograh.dynamicforms.web.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ograh.dynamicforms.auth.dto.AuthResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class WebApiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public WebApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public <T> T get(String path, Class<T> type, HttpSession session) {
        try {
            return restTemplate.exchange(path, HttpMethod.GET, entity(null, session), type).getBody();
        } catch (HttpClientErrorException.Unauthorized e) {
            refreshAndUpdate(session);
            return restTemplate.exchange(path, HttpMethod.GET, entity(null, session), type).getBody();
        } catch (HttpClientErrorException e) {
            throw toApiException(e);
        }
    }

    public <T> T get(String path, ParameterizedTypeReference<T> type, HttpSession session) {
        try {
            return restTemplate.exchange(path, HttpMethod.GET, entity(null, session), type).getBody();
        } catch (HttpClientErrorException.Unauthorized e) {
            refreshAndUpdate(session);
            return restTemplate.exchange(path, HttpMethod.GET, entity(null, session), type).getBody();
        } catch (HttpClientErrorException e) {
            throw toApiException(e);
        }
    }

    public <T> T post(String path, Object body, Class<T> type, HttpSession session) {
        try {
            return restTemplate.exchange(path, HttpMethod.POST, entity(body, session), type).getBody();
        } catch (HttpClientErrorException.Unauthorized e) {
            refreshAndUpdate(session);
            return restTemplate.exchange(path, HttpMethod.POST, entity(body, session), type).getBody();
        } catch (HttpClientErrorException e) {
            throw toApiException(e);
        }
    }

    public void post(String path, HttpSession session) {
        try {
            restTemplate.exchange(path, HttpMethod.POST, entity(null, session), Void.class);
        } catch (HttpClientErrorException.Unauthorized e) {
            refreshAndUpdate(session);
            restTemplate.exchange(path, HttpMethod.POST, entity(null, session), Void.class);
        } catch (HttpClientErrorException e) {
            throw toApiException(e);
        }
    }

    public <T> T put(String path, Object body, Class<T> type, HttpSession session) {
        try {
            return restTemplate.exchange(path, HttpMethod.PUT, entity(body, session), type).getBody();
        } catch (HttpClientErrorException.Unauthorized e) {
            refreshAndUpdate(session);
            return restTemplate.exchange(path, HttpMethod.PUT, entity(body, session), type).getBody();
        } catch (HttpClientErrorException e) {
            throw toApiException(e);
        }
    }

    private HttpEntity<?> entity(Object body, HttpSession session) {
        HttpHeaders headers = new HttpHeaders();
        String token = (String) session.getAttribute("accessToken");
        if (token != null) {
            headers.setBearerAuth(token);
        }
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private void refreshAndUpdate(HttpSession session) {
        String refreshToken = (String) session.getAttribute("refreshToken");
        if (refreshToken == null) {
            throw new ApiException(401, "Session expired");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "refreshToken=" + refreshToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            AuthResponse response = restTemplate.exchange(
                    "/api/auth/refresh-token",
                    HttpMethod.POST,
                    new HttpEntity<>(null, headers),
                    AuthResponse.class
            ).getBody();
            if (response != null) {
                session.setAttribute("accessToken", response.token());
            }
        } catch (HttpClientErrorException e) {
            throw new ApiException(401, "Session expired — please log in again");
        }
    }

    private ApiException toApiException(HttpClientErrorException e) {
        try {
            Map<String, Object> body = objectMapper.readValue(
                    e.getResponseBodyAsString(),
                    new TypeReference<>() {}
            );
            String message = (String) body.getOrDefault("message", e.getMessage());
            @SuppressWarnings("unchecked")
            Map<String, String> errors = (Map<String, String>) body.get("errors");
            return new ApiException(e.getStatusCode().value(), message, errors);
        } catch (Exception parseEx) {
            return new ApiException(e.getStatusCode().value(), e.getMessage());
        }
    }
}
