package com.bankapp.hr.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "departments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    // Un département a plusieurs employés
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Employee> employees;
}