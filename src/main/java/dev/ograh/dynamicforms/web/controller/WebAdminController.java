package dev.ograh.dynamicforms.web.controller;

import dev.ograh.dynamicforms.form.dto.FormDto;
import dev.ograh.dynamicforms.form.dto.request.CreateFormRequest;
import dev.ograh.dynamicforms.form.dto.request.FormFieldRequest;
import dev.ograh.dynamicforms.form.dto.request.UpdateFormRequest;
import dev.ograh.dynamicforms.submission.dto.SubmissionDto;
import dev.ograh.dynamicforms.web.client.ApiException;
import dev.ograh.dynamicforms.web.client.RestPage;
import dev.ograh.dynamicforms.web.client.WebApiClient;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/forms")
@RequiredArgsConstructor
public class WebAdminController {

    private final WebApiClient apiClient;

    @GetMapping
    public String listForms(HttpSession session, Model model) {
        List<FormDto> forms = apiClient.get(
                "/api/admin/forms",
                new ParameterizedTypeReference<>() {},
                session
        );
        model.addAttribute("forms", forms);
        return "admin/forms/list";
    }

    @GetMapping("/new")
    public String createFormPage() {
        return "admin/forms/create";
    }

    @PostMapping("/new")
    public String createForm(@RequestParam String title,
                             @RequestParam(required = false) String description,
                             HttpSession session,
                             Model model) {
        try {
            apiClient.post("/api/admin/forms",
                    new CreateFormRequest(title, description),
                    FormDto.class, session);
            return "redirect:/admin/forms";
        } catch (ApiException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("title", title);
            model.addAttribute("description", description);
            return "admin/forms/create";
        }
    }

    @GetMapping("/{id}/edit")
    public String editFormPage(@PathVariable String id, HttpSession session, Model model) {
        FormDto form = apiClient.get("/api/admin/forms/" + id, FormDto.class, session);
        model.addAttribute("form", form);
        return "admin/forms/edit";
    }

    @PostMapping("/{id}/edit")
    public String editForm(@PathVariable String id,
                           @RequestParam(required = false) String title,
                           @RequestParam(required = false) String description,
                           @RequestParam(required = false) String slug,
                           HttpSession session,
                           Model model) {
        try {
            apiClient.put("/api/admin/forms/" + id,
                    new UpdateFormRequest(title, description, slug),
                    FormDto.class, session);
            return "redirect:/admin/forms";
        } catch (ApiException e) {
            FormDto form = apiClient.get("/api/admin/forms/" + id, FormDto.class, session);
            model.addAttribute("form", form);
            model.addAttribute("error", e.getMessage());
            return "admin/forms/edit";
        }
    }

    @GetMapping("/{id}/fields")
    public String fieldEditorPage(@PathVariable String id, HttpSession session, Model model) {
        FormDto form = apiClient.get("/api/admin/forms/" + id, FormDto.class, session);
        model.addAttribute("form", form);
        return "admin/forms/fields";
    }

    @PutMapping(value = "/{id}/fields", consumes = "application/json",
                produces = "application/json")
    @ResponseBody
    public ResponseEntity<FormDto> saveFields(@PathVariable String id,
                                              @RequestBody List<FormFieldRequest> fields,
                                              HttpSession session) {
        try {
            FormDto updated = apiClient.put(
                    "/api/admin/forms/" + id + "/fields", fields, FormDto.class, session);
            return ResponseEntity.ok(updated);
        } catch (ApiException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PostMapping("/{id}/publish")
    public String publishForm(@PathVariable String id, HttpSession session) {
        apiClient.post("/api/admin/forms/" + id + "/publish", session);
        return "redirect:/admin/forms";
    }

    @GetMapping("/{id}/submissions")
    public String listSubmissions(@PathVariable String id,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  HttpSession session,
                                  Model model) {
        RestPage<SubmissionDto> submissions = apiClient.get(
                "/api/admin/forms/" + id + "/submissions?page=" + page + "&size=" + size,
                new ParameterizedTypeReference<>() {},
                session
        );
        FormDto form = apiClient.get("/api/admin/forms/" + id, FormDto.class, session);
        model.addAttribute("submissions", submissions);
        model.addAttribute("form", form);
        model.addAttribute("currentPage", page);
        return "admin/submissions/list";
    }
}
