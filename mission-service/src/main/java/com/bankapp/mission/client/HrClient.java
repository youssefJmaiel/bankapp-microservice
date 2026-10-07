package com.bankapp.mission.client;

import com.bankapp.hr.dto.EmployeeDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "hr-service")
public interface HrClient {

    @GetMapping("/api/employees")
    List<EmployeeDto> getAllEmployees();

    @GetMapping("/api/employees/{id}")
    EmployeeDto getEmployeeById(@PathVariable("id") Long id);
}