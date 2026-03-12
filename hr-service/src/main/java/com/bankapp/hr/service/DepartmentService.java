package com.bankapp.hr.service;

import com.bankapp.hr.entity.Department;
import com.bankapp.hr.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));
    }

    public Department createDepartment(Department department) {
        return departmentRepository.save(department);
    }

    public Department updateDepartment(Long id, Department updatedDepartment) {
        Department dept = getDepartmentById(id);
        dept.setName(updatedDepartment.getName());
        return departmentRepository.save(dept);
    }

    public void deleteDepartment(Long id) {
        Department dept = getDepartmentById(id);
        departmentRepository.delete(dept);
    }
}