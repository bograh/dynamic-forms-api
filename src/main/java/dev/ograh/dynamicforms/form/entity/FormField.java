package dev.ograh.dynamicforms.form.entity;

import dev.ograh.dynamicforms.form.enums.FieldType;
import dev.ograh.dynamicforms.shared.BaseEntity;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.List;

@Getter
@Setter
@Entity
@Builder
@Table(name = "forms_fields")
@NoArgsConstructor
@AllArgsConstructor
public class FormField extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id")
    private Form form;

    private String label;
    private String fieldKey;
    private String helpText;
    private String placeholder;
    private String defaultValue;
    private int fieldOrder;
    private boolean required;

    @Enumerated(EnumType.STRING)
    private FieldType fieldType;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private List<FieldOption> options;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private FieldValidation validation;
}
