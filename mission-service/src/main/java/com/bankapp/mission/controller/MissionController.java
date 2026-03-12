package com.bankapp.mission.controller;

import com.bankapp.mission.dto.EmployeeDTO;
import com.bankapp.mission.entity.Mission;
import com.bankapp.mission.service.MissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

    // Liste toutes les missions
    @GetMapping
    public ResponseEntity<List<Mission>> getAllMissions() {
        return ResponseEntity.ok(missionService.getAllMissions());
    }

    // Récupérer une mission par son ID
    @GetMapping("/{id}")
    public ResponseEntity<Mission> getMissionById(@PathVariable Long id) {
        return ResponseEntity.ok(missionService.getMissionById(id));
    }

    // Créer une nouvelle mission
    @PostMapping
    public ResponseEntity<Mission> createMission(@Valid @RequestBody Mission mission) {
        Mission savedMission = missionService.createMission(mission);
        return new ResponseEntity<>(savedMission, HttpStatus.CREATED);
    }

    // Mettre à jour une mission existante
    @PutMapping("/{id}")
    public ResponseEntity<Mission> updateMission(
            @PathVariable Long id,
            @Valid @RequestBody Mission updatedMission) {
        return ResponseEntity.ok(missionService.updateMission(id, updatedMission));
    }

    // Supprimer une mission
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMission(@PathVariable Long id) {
        missionService.deleteMission(id);
        return ResponseEntity.noContent().build();
    }

    // Récupérer l'employé assigné à une mission via HR microservice
    @GetMapping("/{id}/employee")
    public ResponseEntity<EmployeeDTO> getAssignedEmployee(@PathVariable Long id) {
        Mission mission = missionService.getMissionById(id);
        EmployeeDTO employee = (EmployeeDTO) missionService.getAssignedEmployee(mission.getAssignedEmployeeId());
        return ResponseEntity.ok(employee);
    }
}