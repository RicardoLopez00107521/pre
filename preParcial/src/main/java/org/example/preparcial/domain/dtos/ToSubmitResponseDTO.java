package org.example.preparcial.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ToSubmitResponseDTO {
    private Date submitDate;
    private String user;
    private String course;
    private String assignment;
}
