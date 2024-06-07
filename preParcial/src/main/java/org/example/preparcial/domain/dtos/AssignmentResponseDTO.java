package org.example.preparcial.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentResponseDTO {
    private Date creationDate;
    private String tittle;
    private String description;
    private Boolean multipleSubmits;
    private Date limitDate;
}
