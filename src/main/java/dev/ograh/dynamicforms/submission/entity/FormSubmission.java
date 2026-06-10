package dev.ograh.dynamicforms.submission.entity;

import dev.ograh.dynamicforms.form.entity.Form;
import dev.ograh.dynamicforms.shared.BaseEntity;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Entity
@Builder
@Table(name = "form_submissions")
@NoArgsConstructor
@AllArgsConstructor
public class FormSubmission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id")
    private Form form;

    private String submittedBy;
    private LocalDateTime submittedAt;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> data;
}
