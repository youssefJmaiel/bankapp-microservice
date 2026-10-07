package com.bankapp.mission.controller;

import com.bankapp.hr.dto.EmployeeDto;
import com.bankapp.mission.dto.MissionDto;
import com.bankapp.mission.dto.MissionRequest;
import com.bankapp.mission.service.MissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<MissionDto>> getAllMissions() {
        return ResponseEntity.ok(missionService.getAllMissions());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<MissionDto> getMissionById(@PathVariable Long id) {
        return ResponseEntity.ok(missionService.getMissionById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MissionDto> createMission(
            @Valid @RequestBody MissionRequest request) {

        MissionDto savedMission = missionService.createMission(request);

        return new ResponseEntity<>(savedMission, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MissionDto> updateMission(
            @PathVariable Long id,
            @Valid @RequestBody MissionRequest request) {

        return ResponseEntity.ok(
                missionService.updateMission(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMission(@PathVariable Long id) {
        missionService.deleteMission(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/employee")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<EmployeeDto> getAssignedEmployee(
            @PathVariable Long id) {

        MissionDto mission = missionService.getMissionById(id);

        EmployeeDto employee =
                missionService.getAssignedEmployee(
                        mission.getAssignedEmployeeId()
                );

        return ResponseEntity.ok(employee);
    }
}
