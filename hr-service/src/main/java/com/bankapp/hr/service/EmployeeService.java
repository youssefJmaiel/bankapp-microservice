package com.bankapp.hr.service;

import com.bankapp.hr.converter.EmployeeDtoConverter;
import com.bankapp.hr.converter.EmployeeRequestConverter;
import com.bankapp.hr.dto.EmployeeDto;
import com.bankapp.hr.dto.EmployeeRequest;
import com.bankapp.hr.entity.Employee;
import com.bankapp.hr.exception.DuplicateEmailException;
import com.bankapp.hr.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeRequestConverter employeeRequestConverter;
    private final EmployeeDtoConverter employeeDtoConverter;
    private final KeycloakAdminService keycloakAdminService;

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
        if (employeeRepository.existsByNormalizedEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        Employee employee = employeeRequestConverter.convert(request);

        Employee savedEmployee = employeeRepository.save(employee);

        // Crée le compte Keycloak, attribue le rôle USER
        // et envoie l'e-mail de vérification et de définition du mot de passe.
        keycloakAdminService.createAndActivateEmployee(savedEmployee);

        return employeeDtoConverter.convert(savedEmployee);
    }

    @Transactional
    public EmployeeDto updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Employee not found with id: " + id)
                );

        if (employeeRepository.existsByNormalizedEmailAndIdNot(request.getEmail(), id)) {
            throw new DuplicateEmailException(request.getEmail());
        }

        Employee convertedEmployee = employeeRequestConverter.convert(request);

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setPosition(request.getPosition());
        employee.setHireDate(request.getHireDate());
        employee.setSalary(request.getSalary());
        employee.setStatus(request.getStatus());

        if (request.getDepartmentId() != null) {
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
