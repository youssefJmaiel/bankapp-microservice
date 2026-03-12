package com.bankapp.mission.client;

import com.bankapp.mission.dto.EmployeeDTO; // Crée un DTO simple avec id, firstName, lastName
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "hr-service", url = "http://localhost:8083/api")
public interface HrClient {

    @GetMapping("/employees")
    List<EmployeeDTO> getAllEmployees();

    @GetMapping("/employees/{id}")
    EmployeeDTO getEmployeeById(@PathVariable("id") Long id);
}