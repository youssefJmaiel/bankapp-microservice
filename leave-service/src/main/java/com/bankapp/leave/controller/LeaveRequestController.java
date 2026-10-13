package com.bankapp.leave.controller;

import com.bankapp.leave.domain.CreateLeaveRequest;
import com.bankapp.leave.domain.LeaveDecisionRequest;
import com.bankapp.leave.domain.LeaveRequestDto;
import com.bankapp.leave.service.LeaveRequestService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/leaves")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<LeaveRequestDto> createRequest(
            @Valid @RequestBody CreateLeaveRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        LeaveRequestDto created = leaveRequestService.createRequest(
                getUsername(jwt), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/my")
    public ResponseEntity<List<LeaveRequestDto>> getMyRequests(
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(
                leaveRequestService.getMyRequests(getUsername(jwt)));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<LeaveRequestDto> cancelMyRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(
                leaveRequestService.cancelMyRequest(id, getUsername(jwt)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<LeaveRequestDto>> getAllRequests() {
        return ResponseEntity.ok(leaveRequestService.getAllRequests());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/decision")
    public ResponseEntity<LeaveRequestDto> decideRequest(
            @PathVariable Long id,
            @Valid @RequestBody LeaveDecisionRequest decisionRequest,
            @AuthenticationPrincipal Jwt jwt) {

        LeaveRequestDto updated = leaveRequestService.decideRequest(
                id, getUsername(jwt), decisionRequest);

        return ResponseEntity.ok(updated);
    }

    private String getUsername(Jwt jwt) {
        if (jwt == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Authentification requise");
        }

        String username = jwt.getClaimAsString("preferred_username");

        if (username != null && !username.trim().isEmpty()) {
            return username;
        }

        return jwt.getSubject();
    }
}
