package dev.ograh.dynamicforms.web.controller;

import dev.ograh.dynamicforms.form.dto.FormDto;
import dev.ograh.dynamicforms.form.enums.FormStatus;
import dev.ograh.dynamicforms.security.JwtTokenService;
import dev.ograh.dynamicforms.shared.exception.SecurityAccessDeniedHandler;
import dev.ograh.dynamicforms.shared.exception.SecurityAuthEntryPoint;
import dev.ograh.dynamicforms.web.client.WebApiClient;
import dev.ograh.dynamicforms.web.security.WebSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = WebAdminController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class})
@Import(WebSecurityConfig.class)
class WebAdminControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean WebApiClient webApiClient;
    @MockitoBean JwtTokenService jwtTokenService;
    @MockitoBean SecurityAuthEntryPoint securityAuthEntryPoint;
    @MockitoBean SecurityAccessDeniedHandler securityAccessDeniedHandler;
    @MockitoBean CorsConfigurationSource corsConfigurationSource;
    @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private FormDto sampleForm() {
        return new FormDto(UUID.randomUUID(), "Survey", "desc", "survey-abc",
                FormStatus.DRAFT, List.of(), "admin@test.com",
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void listForms_returnsAdminListTemplate() throws Exception {
        when(webApiClient.get(eq("/api/admin/forms"), any(ParameterizedTypeReference.class), any()))
                .thenReturn(List.of(sampleForm()));

        mvc.perform(get("/admin/forms"))
           .andExpect(status().isOk())
           .andExpect(view().name("admin/forms/list"))
           .andExpect(model().attributeExists("forms"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createForm_onSuccess_redirectsToList() throws Exception {
        when(webApiClient.post(eq("/api/admin/forms"), any(), eq(FormDto.class), any()))
                .thenReturn(sampleForm());

        mvc.perform(post("/admin/forms/new").with(csrf())
                .param("title", "Survey")
                .param("description", "A test survey"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/admin/forms"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void publishForm_redirectsToList() throws Exception {
        doNothing().when(webApiClient).post(anyString(), any());

        mvc.perform(post("/admin/forms/uuid-123/publish").with(csrf()))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/admin/forms"));
    }
}
