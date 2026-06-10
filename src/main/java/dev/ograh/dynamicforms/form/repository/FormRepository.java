package dev.ograh.dynamicforms.form.repository;

import dev.ograh.dynamicforms.form.entity.Form;
import dev.ograh.dynamicforms.form.enums.FormStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FormRepository extends JpaRepository<Form, UUID> {
    Optional<Form> findBySlugAndStatus(String slug, FormStatus status);
}
