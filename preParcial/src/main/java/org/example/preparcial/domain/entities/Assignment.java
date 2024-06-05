package org.example.preparcial.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "Assignments_tab")
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID assignmentId;

    private String tittle; //
    private String description; //

    private Boolean multipleSubmits; //

    private Date creationDate; // No DTO
    private Date limitDate; //

    @OneToMany(mappedBy = "assignment", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Submit> submit;

    @ManyToOne(optional = false)
    private Course course; //
}
