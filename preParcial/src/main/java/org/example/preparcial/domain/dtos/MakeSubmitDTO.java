package org.example.preparcial.domain.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class MakeSubmitDTO {

    @NotBlank
    private String description;
}
