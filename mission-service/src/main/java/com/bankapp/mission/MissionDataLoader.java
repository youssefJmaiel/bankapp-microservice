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

            missionRepository.saveAll(List.of(
                    new Mission(null, "Mission A", "Développer nouvelle fonctionnalité", LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(5), 1L),
                    new Mission(null, "Mission B", "Révision du process RH", LocalDateTime.now(), LocalDateTime.now().plusDays(3), 2L)
            ));

            System.out.println("Mission-Service: Missions loaded!");
        }
    }
}