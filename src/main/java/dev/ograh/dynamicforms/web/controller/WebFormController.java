package dev.ograh.dynamicforms.web.controller;

import dev.ograh.dynamicforms.form.dto.FormSchemaDto;
import dev.ograh.dynamicforms.submission.dto.SubmissionResponseDto;
import dev.ograh.dynamicforms.web.client.ApiException;
import dev.ograh.dynamicforms.web.client.WebApiClient;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/forms")
@RequiredArgsConstructor
public class WebFormController {

    private final WebApiClient apiClient;

    @GetMapping
    public String listForms(HttpSession session, Model model) {
        List<FormSchemaDto> forms = apiClient.get(
                "/api/forms",
                new ParameterizedTypeReference<>() {},
                session
        );
        model.addAttribute("forms", forms);
        return "forms/list";
    }

    @GetMapping("/{slug}")
    public String viewForm(@PathVariable String slug, HttpSession session, Model model) {
        FormSchemaDto form = apiClient.get("/api/forms/" + slug, FormSchemaDto.class, session);
        model.addAttribute("form", form);
        return "forms/view";
    }

    @PostMapping("/{slug}/submit")
    public String submitForm(@PathVariable String slug,
                             @RequestParam Map<String, String> formData,
                             HttpSession session,
                             Model model) {
        FormSchemaDto form = apiClient.get("/api/forms/" + slug, FormSchemaDto.class, session);
        try {
            apiClient.post(
                    "/api/forms/" + form.id() + "/submit",
                    formData,
                    SubmissionResponseDto.class,
                    session
            );
            return "redirect:/forms?submitted=true";
        } catch (ApiException e) {
            model.addAttribute("form", form);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("fieldErrors", e.getErrors());
            model.addAttribute("submittedData", formData);
            return "forms/view";
        }
    }
}
