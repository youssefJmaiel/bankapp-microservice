package com.bankapp.mission.dto;

import lombok.Data;

@Data
public class EmployeeDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
}