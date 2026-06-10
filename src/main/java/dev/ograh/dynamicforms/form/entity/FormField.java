package dev.ograh.dynamicforms.form.entity;

import dev.ograh.dynamicforms.form.enums.FieldType;
import dev.ograh.dynamicforms.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Getter
@Setter
@Entity
@Builder
@Table(name = "form_fields")
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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<FieldOption> options;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private FieldValidation validation;
}
