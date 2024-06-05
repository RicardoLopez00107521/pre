package org.example.preparcial.domain.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.Date;
import java.util.Optional;

@Data
public class CreateAssignmentDTO {
    @NotBlank
    private String assignmentName;
    @NotBlank
    private String assignmentDescription;
    @NotBlank
    private Boolean multipleSubmits;
    @NotBlank
    private String assignmentDate;
}
