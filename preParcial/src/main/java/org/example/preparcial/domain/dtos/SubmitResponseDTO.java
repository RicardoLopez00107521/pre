package org.example.preparcial.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmitResponseDTO {
    private Date submitDate;
    private Double Note;
    private String gradeObservations;
    private Date gradeDate; //NULL
    private String instructor;
    private String assignment;
}
