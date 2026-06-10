package dev.ograh.dynamicforms.form.entity;

import dev.ograh.dynamicforms.form.enums.FormStatus;
import dev.ograh.dynamicforms.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@Table(name = "forms")
@NoArgsConstructor
@AllArgsConstructor
public class Form extends BaseEntity {

    private String title;
    private String description;
    private String slug;

    @Enumerated(EnumType.STRING)
    private FormStatus status;

    @OneToMany(mappedBy = "form", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fieldOrder ASC")
    private List<FormField> fields = new ArrayList<>();

    private String createdBy;
}
