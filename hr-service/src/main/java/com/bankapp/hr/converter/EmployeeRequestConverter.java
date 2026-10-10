package com.bankapp.hr.converter;

import com.bankapp.hr.dto.EmployeeRequest;
import com.bankapp.hr.entity.Department;
import com.bankapp.hr.entity.Employee;
import com.bankapp.hr.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmployeeRequestConverter {

    private final DepartmentRepository departmentRepository;

    public Employee convert(EmployeeRequest request) {

        if (request == null) {
            return null;
        }

        Employee employee = Employee.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .position(request.getPosition())
                .hireDate(request.getHireDate())
                .salary(request.getSalary())
                .status(request.getStatus())
                .build();

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException(
                            "Department not found with id: " + request.getDepartmentId()
                    ));

            employee.setDepartment(department);
        }

        return employee;
    }
}
