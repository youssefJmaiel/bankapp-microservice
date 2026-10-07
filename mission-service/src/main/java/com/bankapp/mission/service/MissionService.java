package com.bankapp.mission.service;

import com.bankapp.mission.client.HrClient;
import com.bankapp.mission.converter.MissionDtoConverter;
import com.bankapp.mission.converter.MissionRequestConverter;
import com.bankapp.mission.dto.MissionDto;
import com.bankapp.mission.entity.Mission;
import com.bankapp.mission.dto.MissionRequest;
import com.bankapp.hr.dto.EmployeeDto;
import com.bankapp.mission.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionService {

    private final MissionRepository missionRepository;
    private final HrClient hrClient;
    private final MissionRequestConverter missionRequestConverter;
    private final MissionDtoConverter missionDtoConverter;

    public List<MissionDto> getAllMissions() {
        return missionRepository.findAll()
                .stream()
                .map(missionDtoConverter::convert)
                .collect(Collectors.toList());
    }

    public MissionDto getMissionById(Long id) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Mission not found with id: " + id)
                );

        return missionDtoConverter.convert(mission);
    }

    public MissionDto createMission(MissionRequest request) {
        Mission mission = missionRequestConverter.convert(request);

        Mission savedMission = missionRepository.save(mission);

        return missionDtoConverter.convert(savedMission);
    }

    public MissionDto updateMission(Long id, MissionRequest request) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Mission not found with id: " + id)
                );

        mission.setTitle(request.getTitle());
        mission.setDescription(request.getDescription());
        mission.setStartDate(request.getStartDate());
        mission.setEndDate(request.getEndDate());
        mission.setAssignedEmployeeId(request.getAssignedEmployeeId());

        Mission updatedMission = missionRepository.save(mission);

        return missionDtoConverter.convert(updatedMission);
    }

    public void deleteMission(Long id) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Mission not found with id: " + id)
                );

        missionRepository.delete(mission);
    }

    public EmployeeDto getAssignedEmployee(Long employeeId) {
        return hrClient.getEmployeeById(employeeId);
    }
}
