package com.bankapp.hr.service;

import com.bankapp.hr.entity.Employee;
import com.bankapp.hr.entity.Department;
import com.bankapp.hr.repository.EmployeeRepository;
import com.bankapp.hr.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
    }

    public Employee createEmployee(Employee employee, Long departmentId) {
        if (departmentId != null) {
            Department dept = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new RuntimeException("Department not found with id: " + departmentId));
            employee.setDepartment(dept);
        }
        return employeeRepository.save(employee);
    }

    public Employee updateEmployee(Long id, Employee updatedEmployee, Long departmentId) {
        Employee employee = getEmployeeById(id);

        employee.setFirstName(updatedEmployee.getFirstName());
        employee.setLastName(updatedEmployee.getLastName());
        employee.setEmail(updatedEmployee.getEmail());

        if (departmentId != null) {
            Department dept = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new RuntimeException("Department not found with id: " + departmentId));
            employee.setDepartment(dept);
        }

        return employeeRepository.save(employee);
    }

    public void deleteEmployee(Long id) {
        Employee employee = getEmployeeById(id);
        employeeRepository.delete(employee);
    }
}