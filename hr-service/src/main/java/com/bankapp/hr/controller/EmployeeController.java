package com.bankapp.hr.controller;

import com.bankapp.hr.dto.EmployeeDto;
import com.bankapp.hr.dto.EmployeeRequest;
import com.bankapp.hr.dto.KeycloakUserDto;
import com.bankapp.hr.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/keycloak-users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<KeycloakUserDto> getKeycloakUsers() {
        return employeeService.getKeycloakUsers();
    }

    /**
     * Destinataires possibles pour la messagerie : comptes activés ayant
     * le même rôle USER ou ADMIN que l'appelant, selon le JWT Keycloak.
     */
    @GetMapping("/message-recipients")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<String> getMessageRecipients(
            @AuthenticationPrincipal Jwt jwt) {

        String username = jwt.getClaimAsString("preferred_username");
        if (username == null || username.trim().isEmpty()) {
            throw new AccessDeniedException("Authenticated username is missing");
        }

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        Object rolesObject = realmAccess == null ? null : realmAccess.get("roles");
        if (!(rolesObject instanceof Collection<?>)) {
            throw new AccessDeniedException("Realm roles are missing");
        }

        Collection<?> roles = (Collection<?>) rolesObject;
        boolean isUser = roles.contains("USER");
        boolean isAdmin = roles.contains("ADMIN");

        // Une identité portant simultanément USER et ADMIN est ambiguë :
        // on refuse de choisir arbitrairement un rôle pour la messagerie.
        if (isUser == isAdmin) {
            throw new AccessDeniedException(
                    "Exactly one of USER or ADMIN must be assigned for messaging");
        }

        String role = isAdmin ? "ADMIN" : "USER";
        return employeeService.getMessageRecipients(role, username)
                .stream()
                .map(KeycloakUserDto::getUsername)
                .filter(recipient -> recipient != null && !recipient.trim().isEmpty())
                .collect(java.util.stream.Collectors.toList());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<EmployeeDto> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @GetMapping("/archived")
    @PreAuthorize("hasRole('ADMIN')")
    public List<EmployeeDto> getArchivedEmployees() {
        return employeeService.getArchivedEmployees();
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeDto> restoreEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.restoreEmployee(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<EmployeeDto> getEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeDto> createEmployee(
            @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(
                employeeService.createEmployee(request)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeDto> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(
                employeeService.updateEmployee(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
