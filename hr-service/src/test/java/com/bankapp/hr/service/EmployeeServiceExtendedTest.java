package com.bankapp.hr.service;

import com.bankapp.hr.converter.EmployeeDtoConverter;
import com.bankapp.hr.converter.EmployeeRequestConverter;
import com.bankapp.hr.dto.EmployeeDto;
import com.bankapp.hr.dto.EmployeeRequest;
import com.bankapp.hr.entity.Employee;
import com.bankapp.hr.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceExtendedTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private EmployeeRequestConverter employeeRequestConverter;
    @Mock private EmployeeDtoConverter employeeDtoConverter;
    @Mock private KeycloakAdminService keycloakAdminService;

    @InjectMocks private EmployeeService employeeService;

    @Test
    void getAllEmployeesConvertsActiveEmployees() {
        Employee employee = Employee.builder().id(1L).firstName("Ada").build();
        EmployeeDto dto = EmployeeDto.builder().id(1L).firstName("Ada").build();
        when(employeeRepository.findByArchivedFalse()).thenReturn(Collections.singletonList(employee));
        when(employeeDtoConverter.convert(employee)).thenReturn(dto);

        assertEquals(Collections.singletonList(dto), employeeService.getAllEmployees());
        verify(employeeDtoConverter).convert(employee);
    }

    @Test
    void getAllEmployeesReturnsEmptyListWhenNoActiveEmployees() {
        when(employeeRepository.findByArchivedFalse()).thenReturn(Collections.emptyList());

        assertTrue(employeeService.getAllEmployees().isEmpty());
        verifyNoInteractions(employeeDtoConverter);
    }

    @Test
    void getEmployeeByIdReturnsConvertedEmployee() {
        Employee employee = Employee.builder().id(7L).firstName("Ada").build();
        EmployeeDto dto = EmployeeDto.builder().id(7L).firstName("Ada").build();
        when(employeeRepository.findByIdAndArchivedFalse(7L)).thenReturn(Optional.of(employee));
        when(employeeDtoConverter.convert(employee)).thenReturn(dto);

        assertEquals(dto, employeeService.getEmployeeById(7L));
    }

    @Test
    void getEmployeeByIdThrowsWhenEmployeeDoesNotExistOrIsArchived() {
        when(employeeRepository.findByIdAndArchivedFalse(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class, () -> employeeService.getEmployeeById(99L));

        assertEquals("Employee not found with id: 99", exception.getMessage());
        verifyNoInteractions(employeeDtoConverter);
    }

    @Test
    void createEmployeeSavesCreatesKeycloakAccountAndReturnsDto() {
        EmployeeRequest request = EmployeeRequest.builder()
                .firstName("Ada").lastName("Lovelace").email("ada@example.com").build();
        Employee employee = Employee.builder().firstName("Ada").lastName("Lovelace").build();
        Employee saved = Employee.builder().id(5L).firstName("Ada").lastName("Lovelace").build();
        EmployeeDto dto = EmployeeDto.builder().id(5L).firstName("Ada").lastName("Lovelace").build();

        when(employeeRepository.existsByNormalizedEmail(request.getEmail())).thenReturn(false);
        when(employeeRequestConverter.convert(request)).thenReturn(employee);
        when(employeeRepository.save(employee)).thenReturn(saved);
        when(employeeDtoConverter.convert(saved)).thenReturn(dto);

        assertEquals(dto, employeeService.createEmployee(request));

        verify(employeeRepository).save(employee);
        verify(keycloakAdminService).createAndActivateEmployee(saved);
    }

    @Test
    void createEmployeeDoesNotSaveOrCreateKeycloakUserWhenEmailExists() {
        EmployeeRequest request = EmployeeRequest.builder().email("used@example.com").build();
        when(employeeRepository.existsByNormalizedEmail(request.getEmail())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> employeeService.createEmployee(request));

        verify(employeeRepository, never()).save(any(Employee.class));
        verifyNoInteractions(employeeRequestConverter, keycloakAdminService);
    }

    @Test
    void updateEmployeeThrowsWhenEmployeeDoesNotExist() {
        when(employeeRepository.findByIdAndArchivedFalse(44L)).thenReturn(Optional.empty());
        EmployeeRequest request = EmployeeRequest.builder().email("new@example.com").build();

        RuntimeException exception = assertThrows(
                RuntimeException.class, () -> employeeService.updateEmployee(44L, request));

        assertEquals("Employee not found with id: 44", exception.getMessage());
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void deleteEmployeeArchivesInsteadOfDeletingFromRepository() {
        Employee employee = Employee.builder().id(8L).archived(false).build();
        when(employeeRepository.findByIdAndArchivedFalse(8L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(employee)).thenReturn(employee);

        employeeService.deleteEmployee(8L);

        assertTrue(employee.isArchived());
        verify(employeeRepository).save(employee);
        verify(employeeRepository, never()).delete(any(Employee.class));
    }

    @Test
    void deleteEmployeeThrowsWhenEmployeeDoesNotExist() {
        when(employeeRepository.findByIdAndArchivedFalse(8L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> employeeService.deleteEmployee(8L));

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void getArchivedEmployeesReturnsConvertedArchivedEmployees() {
        Employee employee = Employee.builder().id(3L).archived(true).build();
        EmployeeDto dto = EmployeeDto.builder().id(3L).build();
        when(employeeRepository.findByArchivedTrue()).thenReturn(Arrays.asList(employee));
        when(employeeDtoConverter.convert(employee)).thenReturn(dto);

        assertEquals(Collections.singletonList(dto), employeeService.getArchivedEmployees());
    }

    @Test
    void restoreEmployeeRestoresArchivedEmployee() {
        Employee employee = Employee.builder().id(12L).archived(true).build();
        EmployeeDto dto = EmployeeDto.builder().id(12L).build();
        when(employeeRepository.findById(12L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeDtoConverter.convert(employee)).thenReturn(dto);

        assertEquals(dto, employeeService.restoreEmployee(12L));

        assertFalse(employee.isArchived());
        verify(employeeRepository).save(employee);
    }

    @Test
    void restoreEmployeeThrowsWhenEmployeeDoesNotExist() {
        when(employeeRepository.findById(13L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class, () -> employeeService.restoreEmployee(13L));

        assertEquals("Employee not found with id: 13", exception.getMessage());
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void restoreEmployeeThrowsWhenEmployeeIsAlreadyActive() {
        Employee employee = Employee.builder().id(14L).archived(false).build();
        when(employeeRepository.findById(14L)).thenReturn(Optional.of(employee));

        RuntimeException exception = assertThrows(
                RuntimeException.class, () -> employeeService.restoreEmployee(14L));

        assertEquals("Employee is not archived: 14", exception.getMessage());
        verify(employeeRepository, never()).save(any(Employee.class));
    }
}
