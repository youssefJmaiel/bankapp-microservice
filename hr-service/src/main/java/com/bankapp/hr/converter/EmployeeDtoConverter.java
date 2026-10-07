package com.bankapp.hr.converter;

import com.bankapp.hr.dto.DepartmentDto;
import com.bankapp.hr.dto.EmployeeDto;
import com.bankapp.hr.entity.Department;
import com.bankapp.hr.entity.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeDtoConverter {

    public EmployeeDto convert(Employee employee) {

        if (employee == null) {
            return null;
        }

        DepartmentDto departmentDto = null;

        Department department = employee.getDepartment();

        if (department != null) {
            departmentDto = DepartmentDto.builder()
                    .id(department.getId())
                    .name(department.getName())
                    .build();
        }

        return EmployeeDto.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .department(departmentDto)
                .build();
    }
}
