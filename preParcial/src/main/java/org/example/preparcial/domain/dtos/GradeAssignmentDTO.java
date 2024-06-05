package org.example.preparcial.domain.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GradeAssignmentDTO {
    @NotBlank
    Double note;
    String noteObservations;
}
