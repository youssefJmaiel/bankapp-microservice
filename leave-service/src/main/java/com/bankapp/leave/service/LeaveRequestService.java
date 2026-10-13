package com.bankapp.leave.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bankapp.leave.converter.LeaveRequestConverter;
import com.bankapp.leave.converter.LeaveRequestDtoConverter;
import com.bankapp.leave.domain.CreateLeaveRequest;
import com.bankapp.leave.domain.LeaveDecisionRequest;
import com.bankapp.leave.domain.LeaveRequestDto;
import com.bankapp.leave.entity.LeaveRequest;
import com.bankapp.leave.repository.LeaveRequestRepository;

@Service
@Transactional
public class LeaveRequestService {

    private final LeaveRequestRepository repository;
    private final LeaveRequestConverter requestConverter;
    private final LeaveRequestDtoConverter dtoConverter;

    public LeaveRequestService(
            LeaveRequestRepository repository,
            LeaveRequestConverter requestConverter,
            LeaveRequestDtoConverter dtoConverter) {
        this.repository = repository;
        this.requestConverter = requestConverter;
        this.dtoConverter = dtoConverter;
    }

    public LeaveRequestDto createRequest(String requesterUsername, CreateLeaveRequest request) {
        if (requesterUsername == null || requesterUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom d'utilisateur est obligatoire");
        }

        if (request == null) {
            throw new IllegalArgumentException("La demande de congé est obligatoire");
        }

        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("Les dates de début et de fin sont obligatoires");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException(
                "La date de fin doit être égale ou postérieure à la date de début"
            );
        }

        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La date de début ne peut pas être dans le passé");
        }

        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new IllegalArgumentException("Le motif du congé est obligatoire");
        }

        if (request.getReason().trim().length() > 1000) {
            throw new IllegalArgumentException("Le motif ne doit pas dépasser 1000 caractères");
        }

        LeaveRequest entity = requestConverter.convert(request);
        entity.setRequesterUsername(requesterUsername);
        entity.setStatus(LeaveRequest.Status.PENDING);

        return dtoConverter.convert(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestDto> getMyRequests(String requesterUsername) {
        return repository.findByRequesterUsernameOrderByCreatedAtDesc(requesterUsername)
            .stream()
            .map(dtoConverter::convert)
            .collect(Collectors.toList());
    }

    public LeaveRequestDto cancelMyRequest(Long id, String requesterUsername) {
        LeaveRequest request = repository.findByIdAndRequesterUsername(id, requesterUsername)
            .orElseThrow(() -> new IllegalArgumentException(
                "Demande de congé introuvable pour cet utilisateur"
            ));

        if (request.getStatus() != LeaveRequest.Status.PENDING) {
            throw new IllegalStateException(
                "Seules les demandes en attente peuvent être annulées"
            );
        }

        request.setStatus(LeaveRequest.Status.CANCELLED);
        return dtoConverter.convert(repository.save(request));
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestDto> getAllRequests() {
        return repository.findAllByOrderByCreatedAtDesc()
            .stream()
            .map(dtoConverter::convert)
            .collect(Collectors.toList());
    }

    public LeaveRequestDto decideRequest(
            Long id,
            String administratorUsername,
            LeaveDecisionRequest decisionRequest) {

        if (administratorUsername == null || administratorUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("L'identifiant de l'administrateur est obligatoire");
        }

        if (decisionRequest == null || decisionRequest.getDecision() == null) {
            throw new IllegalArgumentException("La décision est obligatoire");
        }

        LeaveRequest request = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Demande de congé introuvable"));

        if (request.getStatus() != LeaveRequest.Status.PENDING) {
            throw new IllegalStateException(
                "Seules les demandes en attente peuvent recevoir une décision"
            );
        }

        request.setStatus(
            decisionRequest.getDecision() == LeaveDecisionRequest.Decision.APPROVED
                ? LeaveRequest.Status.APPROVED
                : LeaveRequest.Status.REJECTED
        );
        request.setDecisionBy(administratorUsername);
        request.setDecisionAt(LocalDateTime.now());

        return dtoConverter.convert(repository.save(request));
    }
}
