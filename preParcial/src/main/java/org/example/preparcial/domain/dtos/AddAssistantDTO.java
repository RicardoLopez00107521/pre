package org.example.preparcial.domain.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddAssistantDTO {
    @NotBlank
    private String user;

    @NotBlank
    private String tittle;
}
