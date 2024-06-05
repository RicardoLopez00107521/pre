package org.example.preparcial.domain.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCourseDTO {
    @NotBlank
    private String tittle;

    @NotBlank
    private String description;
}
