package com.bankapp.hr.controller;

import com.bankapp.hr.entity.Employee;
import com.bankapp.hr.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee,
                                                   @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(employeeService.createEmployee(employee, departmentId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id,
                                                   @RequestBody Employee employee,
                                                   @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, employee, departmentId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}