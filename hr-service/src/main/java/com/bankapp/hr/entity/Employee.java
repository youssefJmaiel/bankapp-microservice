package com.bankapp.hr.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")  // clé étrangère vers Department
    private Department department;
}