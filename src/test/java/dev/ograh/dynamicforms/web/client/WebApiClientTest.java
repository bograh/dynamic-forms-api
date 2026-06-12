package dev.ograh.dynamicforms.web.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class WebApiClientTest {

    private MockRestServiceServer server;
    private WebApiClient client;
    private MockHttpSession session;

    @BeforeEach
    void setup() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        client = new WebApiClient(restTemplate, "");
        session = new MockHttpSession();
        session.setAttribute("accessToken", "access-token-123");
    }

    @Test
    void get_attachesBearer() {
        server.expect(requestTo("/api/forms"))
              .andExpect(method(HttpMethod.GET))
              .andExpect(header("Authorization", "Bearer access-token-123"))
              .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        client.get("/api/forms", String.class, session);

        server.verify();
    }

    @Test
    void get_on401_refreshesTokenAndRetries() {
        session.setAttribute("refreshToken", "refresh-token-abc");

        server.expect(requestTo("/api/forms"))
              .andRespond(withUnauthorizedRequest());

        server.expect(requestTo("/api/auth/refresh-token"))
              .andExpect(method(HttpMethod.POST))
              .andRespond(withSuccess(
                  "{\"token\":\"new-token\",\"user\":{\"id\":\"1\",\"name\":\"Bob\",\"email\":\"b@b.com\",\"role\":\"USER\",\"lastUpdated\":\"2026-01-01\"}}",
                  MediaType.APPLICATION_JSON));

        server.expect(requestTo("/api/forms"))
              .andExpect(header("Authorization", "Bearer new-token"))
              .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        client.get("/api/forms", String.class, session);

        assertThat(session.getAttribute("accessToken")).isEqualTo("new-token");
        server.verify();
    }

    @Test
    void get_throwsApiException_on404() {
        server.expect(requestTo("/api/forms/unknown"))
              .andRespond(withResourceNotFound().body("{\"message\":\"Not found\",\"statusCode\":404}")
                      .contentType(MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.get("/api/forms/unknown", String.class, session))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatusCode()).isEqualTo(404));
    }
}
