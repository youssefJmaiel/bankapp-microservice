package com.bankapp.mission.service;

import com.bankapp.mission.client.HrClient;
import com.bankapp.mission.entity.Mission;
import com.bankapp.mission.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionService {

    private final MissionRepository missionRepository;
    private final HrClient hrClient; // Injection de HrClient via Feign

    public List<Mission> getAllMissions() {
        return missionRepository.findAll();
    }

    public Mission getMissionById(Long id) {
        return missionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mission not found with id: " + id));
    }

    public Mission createMission(Mission mission) {
        return missionRepository.save(mission);
    }

    public Mission updateMission(Long id, Mission updatedMission) {
        Mission mission = getMissionById(id);
        mission.setTitle(updatedMission.getTitle());
        mission.setDescription(updatedMission.getDescription());
        mission.setStartDate(updatedMission.getStartDate());
        mission.setEndDate(updatedMission.getEndDate());
        mission.setAssignedEmployeeId(updatedMission.getAssignedEmployeeId());
        return missionRepository.save(mission);
    }

    public void deleteMission(Long id) {
        Mission mission = getMissionById(id);
        missionRepository.delete(mission);
    }

    // Exemple : récupérer l’employé assigné via HrClient
    public Object getAssignedEmployee(Long employeeId) {
        return hrClient.getEmployeeById(employeeId);
    }
}