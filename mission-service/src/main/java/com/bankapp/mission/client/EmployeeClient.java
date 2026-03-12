package com.bankapp.mission.client;

import com.bankapp.mission.dto.EmployeeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "hr-service", url = "http://localhost:8083") // HR service url
public interface EmployeeClient {

    @GetMapping("/api/employee/{id}")
    EmployeeDTO getEmployeeById(@PathVariable("id") Long id);
}