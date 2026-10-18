package com.bankapp.hr.service;

import com.bankapp.hr.converter.DepartmentDtoConverter;
import com.bankapp.hr.converter.DepartmentRequestConverter;
import com.bankapp.hr.dto.DepartmentDto;
import com.bankapp.hr.dto.DepartmentRequest;
import com.bankapp.hr.entity.Department;
import com.bankapp.hr.repository.DepartmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock private DepartmentRepository departmentRepository;
    @Mock private DepartmentRequestConverter departmentRequestConverter;
    @Mock private DepartmentDtoConverter departmentDtoConverter;

    @InjectMocks private DepartmentService departmentService;

    @Test
    void getAllDepartmentsConvertsResults() {
        Department department = Department.builder().id(1L).name("IT").build();
        DepartmentDto dto = DepartmentDto.builder().id(1L).name("IT").build();
        when(departmentRepository.findAll()).thenReturn(Collections.singletonList(department));
        when(departmentDtoConverter.convert(department)).thenReturn(dto);

        assertEquals(Collections.singletonList(dto), departmentService.getAllDepartments());
    }

    @Test
    void getAllDepartmentsReturnsEmptyListWhenNoDepartmentsExist() {
        when(departmentRepository.findAll()).thenReturn(Collections.emptyList());

        assertTrue(departmentService.getAllDepartments().isEmpty());
        verifyNoInteractions(departmentDtoConverter);
    }

    @Test
    void getDepartmentByIdReturnsConvertedDepartment() {
        Department department = Department.builder().id(2L).name("Finance").build();
        DepartmentDto dto = DepartmentDto.builder().id(2L).name("Finance").build();
        when(departmentRepository.findById(2L)).thenReturn(Optional.of(department));
        when(departmentDtoConverter.convert(department)).thenReturn(dto);

        assertEquals(dto, departmentService.getDepartmentById(2L));
    }

    @Test
    void getDepartmentByIdThrowsWhenDepartmentDoesNotExist() {
        when(departmentRepository.findById(90L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class, () -> departmentService.getDepartmentById(90L));

        assertEquals("Department not found with id: 90", exception.getMessage());
        verifyNoInteractions(departmentDtoConverter);
    }

    @Test
    void createDepartmentConvertsSavesAndReturnsDto() {
        DepartmentRequest request = DepartmentRequest.builder().name("Risk").build();
        Department department = Department.builder().name("Risk").build();
        Department saved = Department.builder().id(4L).name("Risk").build();
        DepartmentDto dto = DepartmentDto.builder().id(4L).name("Risk").build();

        when(departmentRequestConverter.convert(request)).thenReturn(department);
        when(departmentRepository.save(department)).thenReturn(saved);
        when(departmentDtoConverter.convert(saved)).thenReturn(dto);

        assertEquals(dto, departmentService.createDepartment(request));
        verify(departmentRepository).save(department);
    }

    @Test
    void updateDepartmentChangesNameAndSaves() {
        Department department = Department.builder().id(5L).name("Old").build();
        DepartmentRequest request = DepartmentRequest.builder().name("New").build();
        DepartmentDto dto = DepartmentDto.builder().id(5L).name("New").build();

        when(departmentRepository.findById(5L)).thenReturn(Optional.of(department));
        when(departmentRepository.save(department)).thenReturn(department);
        when(departmentDtoConverter.convert(department)).thenReturn(dto);

        assertEquals(dto, departmentService.updateDepartment(5L, request));
        assertEquals("New", department.getName());
        verify(departmentRepository).save(department);
    }

    @Test
    void updateDepartmentThrowsWhenDepartmentDoesNotExist() {
        when(departmentRepository.findById(50L)).thenReturn(Optional.empty());
        DepartmentRequest request = DepartmentRequest.builder().name("New").build();

        RuntimeException exception = assertThrows(
                RuntimeException.class, () -> departmentService.updateDepartment(50L, request));

        assertEquals("Department not found with id: 50", exception.getMessage());
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void deleteDepartmentDeletesExistingDepartment() {
        Department department = Department.builder().id(6L).name("Operations").build();
        when(departmentRepository.findById(6L)).thenReturn(Optional.of(department));

        departmentService.deleteDepartment(6L);

        verify(departmentRepository).delete(department);
    }

    @Test
    void deleteDepartmentThrowsWhenDepartmentDoesNotExist() {
        when(departmentRepository.findById(60L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class, () -> departmentService.deleteDepartment(60L));

        assertEquals("Department not found with id: 60", exception.getMessage());
        verify(departmentRepository, never()).delete(any(Department.class));
    }
}
