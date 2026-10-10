package com.bankapp.hr.service;

import com.bankapp.hr.converter.EmployeeDtoConverter;
import com.bankapp.hr.converter.EmployeeRequestConverter;
import com.bankapp.hr.dto.EmployeeRequest;
import com.bankapp.hr.entity.Employee;
import com.bankapp.hr.exception.DuplicateEmailException;
import com.bankapp.hr.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeRequestConverter employeeRequestConverter;

    @Mock
    private EmployeeDtoConverter employeeDtoConverter;

    @Mock
    private KeycloakAdminService keycloakAdminService;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void createEmployeeRejectsDuplicateEmailIgnoringCaseAndSpaces() {
        EmployeeRequest request = EmployeeRequest.builder()
                .firstName("Test")
                .lastName("User")
                .email(" TEST@example.com ")
                .build();

        when(employeeRepository.existsByNormalizedEmail(request.getEmail()))
                .thenReturn(true);

        assertThrows(
                DuplicateEmailException.class,
                () -> employeeService.createEmployee(request)
        );

        verify(employeeRepository, never()).save(any(Employee.class));
        verifyNoInteractions(keycloakAdminService);
    }

    @Test
    void updateEmployeeRejectsEmailUsedByAnotherEmployee() {
        Long employeeId = 1L;

        Employee employee = Employee.builder()
                .id(employeeId)
                .firstName("Old")
                .lastName("User")
                .email("old@example.com")
                .build();

        EmployeeRequest request = EmployeeRequest.builder()
                .firstName("New")
                .lastName("User")
                .email("other@example.com")
                .build();

        when(employeeRepository.findById(employeeId))
                .thenReturn(Optional.of(employee));
        when(employeeRepository.existsByNormalizedEmailAndIdNot(
                request.getEmail(), employeeId))
                .thenReturn(true);

        assertThrows(
                DuplicateEmailException.class,
                () -> employeeService.updateEmployee(employeeId, request)
        );

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void updateEmployeeAllowsKeepingItsOwnEmail() {
        Long employeeId = 1L;

        Employee employee = Employee.builder()
                .id(employeeId)
                .firstName("Old")
                .lastName("User")
                .email("user@example.com")
                .build();

        EmployeeRequest request = EmployeeRequest.builder()
                .firstName("New")
                .lastName("User")
                .email("user@example.com")
                .build();

        when(employeeRepository.findById(employeeId))
                .thenReturn(Optional.of(employee));
        when(employeeRepository.existsByNormalizedEmailAndIdNot(
                request.getEmail(), employeeId))
                .thenReturn(false);
        when(employeeRepository.save(employee)).thenReturn(employee);

        employeeService.updateEmployee(employeeId, request);

        assertEquals("New", employee.getFirstName());
        assertEquals("User", employee.getLastName());
        assertEquals("user@example.com", employee.getEmail());

        verify(employeeRepository).save(employee);
    }
}
