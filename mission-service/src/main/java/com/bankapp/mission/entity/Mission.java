package com.bankapp.mission.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "missions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mission implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @NotNull
    private Long assignedEmployeeId; // référence à Employee par ID seulement
}