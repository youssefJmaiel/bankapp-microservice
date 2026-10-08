package com.bankapp.hr.service;

import com.bankapp.hr.client.NotificationClient;
import com.bankapp.hr.converter.EmployeeDtoConverter;
import com.bankapp.hr.converter.EmployeeRequestConverter;
import com.bankapp.hr.dto.EmployeeDto;
import com.bankapp.hr.dto.EmployeeRequest;
import com.bankapp.notification.dto.MailRequest;
import com.bankapp.hr.entity.Employee;
import com.bankapp.hr.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeRequestConverter employeeRequestConverter;
    private final EmployeeDtoConverter employeeDtoConverter;
    private final NotificationClient notificationClient;

    @Transactional
    public List<EmployeeDto> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(employeeDtoConverter::convert)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmployeeDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Employee not found with id: " + id)
                );

        return employeeDtoConverter.convert(employee);
    }

    @Transactional
    public EmployeeDto createEmployee(EmployeeRequest request) {
        Employee employee = employeeRequestConverter.convert(request);

        Employee savedEmployee = employeeRepository.save(employee);

        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", savedEmployee.getFirstName());
        variables.put("lastName", savedEmployee.getLastName());
        variables.put("email", savedEmployee.getEmail());

        MailRequest mailRequest = MailRequest.builder()
                .to(savedEmployee.getEmail())
                .subject("Welcome to BankApp")
                .template("employee-created")
                .locale("en")
                .variables(variables)
                .build();

        notificationClient.sendMail(mailRequest);

        return employeeDtoConverter.convert(savedEmployee);
    }

    @Transactional
    public EmployeeDto updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Employee not found with id: " + id)
                );

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());

        if (request.getDepartmentId() != null) {
            Employee convertedEmployee = employeeRequestConverter.convert(request);
            employee.setDepartment(convertedEmployee.getDepartment());
        }

        Employee updatedEmployee = employeeRepository.save(employee);

        return employeeDtoConverter.convert(updatedEmployee);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Employee not found with id: " + id)
                );

        employeeRepository.delete(employee);
    }
}
