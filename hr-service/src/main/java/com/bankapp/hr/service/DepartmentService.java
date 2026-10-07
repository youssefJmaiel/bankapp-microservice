package com.bankapp.hr.service;

import com.bankapp.hr.converter.DepartmentDtoConverter;
import com.bankapp.hr.converter.DepartmentRequestConverter;
import com.bankapp.hr.dto.DepartmentDto;
import com.bankapp.hr.dto.DepartmentRequest;
import com.bankapp.hr.entity.Department;
import com.bankapp.hr.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentRequestConverter departmentRequestConverter;
    private final DepartmentDtoConverter departmentDtoConverter;

    @Transactional
    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(departmentDtoConverter::convert)
                .collect(Collectors.toList());
    }

    @Transactional
    public DepartmentDto getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Department not found with id: " + id)
                );

        return departmentDtoConverter.convert(department);
    }

    @Transactional
    public DepartmentDto createDepartment(DepartmentRequest request) {
        Department department = departmentRequestConverter.convert(request);

        Department savedDepartment = departmentRepository.save(department);

        return departmentDtoConverter.convert(savedDepartment);
    }

    @Transactional
    public DepartmentDto updateDepartment(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Department not found with id: " + id)
                );

        department.setName(request.getName());

        Department updatedDepartment = departmentRepository.save(department);

        return departmentDtoConverter.convert(updatedDepartment);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Department not found with id: " + id)
                );

        departmentRepository.delete(department);
    }
}
