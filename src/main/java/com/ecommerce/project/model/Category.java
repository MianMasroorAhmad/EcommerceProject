package com.ecommerce.project.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    /**
     * Validation flow:

     * 1. Controller uses @Valid → validates CategoryDTO (API layer)
     *    → Throws MethodArgumentNotValidException on failure (handled with 400 response)

     * 2. Entity validation acts as a safety net (persistence layer)
     *    → Triggered during JPA persist/flush
     *    → Throws ConstraintViolationException if invalid data reaches this layer

     * Therefore:
     * - Validation annotations should exist on DTO (primary validation)
     * - Keeping them on Entity is recommended as a safeguard

     * TODO: ConstraintViolationException must be handled globally to avoid 500 errors
     */
    @NotBlank
    @Size(min=3, message = "Category name must have at least 3 characters")
    private String categoryName;
}
