package dev.ograh.dynamicforms.web.controller;

import dev.ograh.dynamicforms.auth.dto.AuthResponse;
import dev.ograh.dynamicforms.auth.dto.UserResponse;
import dev.ograh.dynamicforms.security.JwtTokenService;
import dev.ograh.dynamicforms.shared.exception.SecurityAccessDeniedHandler;
import dev.ograh.dynamicforms.shared.exception.SecurityAuthEntryPoint;
import dev.ograh.dynamicforms.web.client.ApiException;
import dev.ograh.dynamicforms.web.client.WebApiClient;
import dev.ograh.dynamicforms.web.security.WebSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = WebAuthController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class})
@Import(WebSecurityConfig.class)
class WebAuthControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean WebApiClient webApiClient;
    @MockitoBean JwtTokenService jwtTokenService;
    @MockitoBean SecurityAuthEntryPoint securityAuthEntryPoint;
    @MockitoBean SecurityAccessDeniedHandler securityAccessDeniedHandler;
    @MockitoBean CorsConfigurationSource corsConfigurationSource;
    @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void loginPage_returnsLoginTemplate() throws Exception {
        mvc.perform(get("/login"))
           .andExpect(status().isOk())
           .andExpect(view().name("auth/login"));
    }

    @Test
    void login_onSuccess_redirectsToForms() throws Exception {
        UserResponse user = new UserResponse("1", "Alice", "alice@example.com", "USER", "2026-01-01");
        AuthResponse authResponse = new AuthResponse(user, "jwt-token");

        when(webApiClient.post(eq("/api/auth/login"), any(), eq(AuthResponse.class), any()))
                .thenReturn(authResponse);

        MockHttpSession session = new MockHttpSession();

        mvc.perform(post("/login").with(csrf())
                .param("email", "alice@example.com")
                .param("password", "secret")
                .session(session))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/forms"));

        assertThat(session.getAttribute("accessToken")).isEqualTo("jwt-token");
        assertThat(session.getAttribute("role")).isEqualTo("USER");
    }

    @Test
    void login_onBadCredentials_rendersLoginWithError() throws Exception {
        when(webApiClient.post(eq("/api/auth/login"), any(), eq(AuthResponse.class), any()))
                .thenThrow(new ApiException(401, "Bad credentials"));

        mvc.perform(post("/login").with(csrf())
                .param("email", "bad@example.com")
                .param("password", "wrong"))
           .andExpect(status().isOk())
           .andExpect(view().name("auth/login"))
           .andExpect(model().attributeExists("error"));
    }

    @Test
    void register_onSuccess_redirectsToForms() throws Exception {
        UserResponse user = new UserResponse("2", "Bob", "bob@example.com", "USER", "2026-01-01");
        AuthResponse authResponse = new AuthResponse(user, "jwt-token-bob");

        when(webApiClient.post(eq("/api/auth/register"), any(), eq(AuthResponse.class), any()))
                .thenReturn(authResponse);

        MockHttpSession session = new MockHttpSession();

        mvc.perform(post("/register").with(csrf())
                .param("name", "Bob")
                .param("email", "bob@example.com")
                .param("password", "password123")
                .session(session))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/forms"));
    }
}
