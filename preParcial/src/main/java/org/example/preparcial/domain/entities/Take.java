package org.example.preparcial.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Takes_table")
public class Take {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID take_id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Course course;
}
