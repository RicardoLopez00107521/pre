package org.example.preparcial.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Submits_table")
public class Submit {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID submit_id;

    private Date submitDate;
    private String description;
    private Double note; // NULL
    private String gradeObservations; //NULL
    private Boolean graded; // NULL
    private Date gradeDate; //NULL
    private String instructor; //NULL

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Assignment assignment;
}
