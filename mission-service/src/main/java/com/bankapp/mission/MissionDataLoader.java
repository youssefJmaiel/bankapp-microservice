package com.bankapp.mission;

import com.bankapp.mission.entity.Mission;
import com.bankapp.mission.repository.MissionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class MissionDataLoader implements CommandLineRunner {

    private final MissionRepository missionRepository;

    public MissionDataLoader(MissionRepository missionRepository) {
        this.missionRepository = missionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if(missionRepository.count() == 0) {

            Mission missionA = new Mission();
            missionA.setTitle("Mission A");
            missionA.setDescription("Développer nouvelle fonctionnalité");
            missionA.setStartDate(LocalDateTime.now().minusDays(2));
            missionA.setEndDate(LocalDateTime.now().plusDays(5));
            missionA.setAssignedEmployeeId(1L);
            missionA.setStatus("PENDING");
            missionA.setPriority("MEDIUM");

            Mission missionB = new Mission();
            missionB.setTitle("Mission B");
            missionB.setDescription("Révision du process RH");
            missionB.setStartDate(LocalDateTime.now());
            missionB.setEndDate(LocalDateTime.now().plusDays(3));
            missionB.setAssignedEmployeeId(2L);
            missionB.setStatus("PENDING");
            missionB.setPriority("MEDIUM");

            missionRepository.saveAll(List.of(missionA, missionB));

            System.out.println("Mission-Service: Missions loaded!");
        }
    }
}