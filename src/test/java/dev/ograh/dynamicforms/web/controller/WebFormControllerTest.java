package dev.ograh.dynamicforms.web.controller;

import dev.ograh.dynamicforms.form.dto.FormSchemaDto;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = WebFormController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class})
@Import(WebSecurityConfig.class)
class WebFormControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean WebApiClient webApiClient;
    @MockitoBean JwtTokenService jwtTokenService;
    @MockitoBean SecurityAuthEntryPoint securityAuthEntryPoint;
    @MockitoBean SecurityAccessDeniedHandler securityAccessDeniedHandler;
    @MockitoBean CorsConfigurationSource corsConfigurationSource;
    @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @WithMockUser
    void listForms_returnsList() throws Exception {
        FormSchemaDto form = new FormSchemaDto("id1", "Survey", "desc", "survey-abc", List.of());
        when(webApiClient.get(eq("/api/forms"), any(ParameterizedTypeReference.class), any()))
                .thenReturn(List.of(form));

        mvc.perform(get("/forms"))
           .andExpect(status().isOk())
           .andExpect(view().name("forms/list"))
           .andExpect(model().attributeExists("forms"));
    }

    @Test
    @WithMockUser
    void viewForm_rendersViewTemplate() throws Exception {
        FormSchemaDto form = new FormSchemaDto("id1", "Survey", "desc", "survey-abc", List.of());
        when(webApiClient.get(eq("/api/forms/survey-abc"), eq(FormSchemaDto.class), any()))
                .thenReturn(form);

        mvc.perform(get("/forms/survey-abc"))
           .andExpect(status().isOk())
           .andExpect(view().name("forms/view"))
           .andExpect(model().attribute("form", form));
    }

    @Test
    @WithMockUser
    void submitForm_onSuccess_redirects() throws Exception {
        FormSchemaDto form = new FormSchemaDto("uuid-111", "Survey", "desc", "survey-abc", List.of());
        when(webApiClient.get(eq("/api/forms/survey-abc"), eq(FormSchemaDto.class), any()))
                .thenReturn(form);
        when(webApiClient.post(anyString(), any(), any(), any())).thenReturn(null);

        mvc.perform(post("/forms/survey-abc/submit").with(csrf())
                .param("name", "Alice"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/forms?submitted=true"));
    }

    @Test
    @WithMockUser
    void submitForm_onValidationError_rendersViewWithErrors() throws Exception {
        FormSchemaDto form = new FormSchemaDto("uuid-111", "Survey", "desc", "survey-abc", List.of());
        when(webApiClient.get(eq("/api/forms/survey-abc"), eq(FormSchemaDto.class), any()))
                .thenReturn(form);
        when(webApiClient.post(anyString(), any(), any(), any()))
                .thenThrow(new ApiException(400, "Validation failed"));

        mvc.perform(post("/forms/survey-abc/submit").with(csrf())
                .param("name", ""))
           .andExpect(status().isOk())
           .andExpect(view().name("forms/view"))
           .andExpect(model().attributeExists("error"));
    }
}
