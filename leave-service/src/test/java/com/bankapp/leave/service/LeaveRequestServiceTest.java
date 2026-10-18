package com.bankapp.leave.service;

import com.bankapp.leave.converter.LeaveRequestConverter;
import com.bankapp.leave.converter.LeaveRequestDtoConverter;
import com.bankapp.leave.domain.CreateLeaveRequest;
import com.bankapp.leave.domain.LeaveDecisionRequest;
import com.bankapp.leave.domain.LeaveRequestDto;
import com.bankapp.leave.entity.LeaveRequest;
import com.bankapp.leave.repository.LeaveRequestRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceTest {

    @Mock
    private LeaveRequestRepository repository;

    @Mock
    private LeaveRequestConverter requestConverter;

    @Mock
    private LeaveRequestDtoConverter dtoConverter;

    @InjectMocks
    private LeaveRequestService service;

    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.now().plusDays(5);
        endDate = startDate.plusDays(3);
    }

    private CreateLeaveRequest validRequest() {
        CreateLeaveRequest request = new CreateLeaveRequest();
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setReason("Congé annuel");
        return request;
    }

    private LeaveRequest pendingEntity() {
        LeaveRequest entity = new LeaveRequest();
        entity.setRequesterUsername("youssef");
        entity.setStartDate(startDate);
        entity.setEndDate(endDate);
        entity.setReason("Congé annuel");
        entity.setStatus(LeaveRequest.Status.PENDING);
        return entity;
    }

    private LeaveRequestDto dto(String status) {
        LeaveRequestDto result = new LeaveRequestDto();
        result.setRequesterUsername("youssef");
        result.setStartDate(startDate);
        result.setEndDate(endDate);
        result.setReason("Congé annuel");
        result.setStatus(status);
        return result;
    }

    @Test
    void createRequestShouldSavePendingRequestForRequester() {
        CreateLeaveRequest input = validRequest();
        LeaveRequest entity = pendingEntity();
        LeaveRequestDto expected = dto("PENDING");

        when(requestConverter.convert(input)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(dtoConverter.convert(entity)).thenReturn(expected);

        LeaveRequestDto result = service.createRequest("youssef", input);

        assertSame(expected, result);
        assertEquals("youssef", entity.getRequesterUsername());
        assertEquals(LeaveRequest.Status.PENDING, entity.getStatus());
        verify(repository).save(entity);
    }

    @Test
    void createRequestShouldRejectBlankUsername() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.createRequest("  ", validRequest())
        );

        verifyNoInteractions(repository, requestConverter, dtoConverter);
    }

    @Test
    void createRequestShouldRejectNullRequest() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.createRequest("youssef", null)
        );

        verifyNoInteractions(repository, requestConverter, dtoConverter);
    }

    @Test
    void createRequestShouldRejectEndDateBeforeStartDate() {
        CreateLeaveRequest input = validRequest();
        input.setEndDate(startDate.minusDays(1));

        assertThrows(
            IllegalArgumentException.class,
            () -> service.createRequest("youssef", input)
        );

        verifyNoInteractions(repository, requestConverter, dtoConverter);
    }

    @Test
    void createRequestShouldRejectPastStartDate() {
        CreateLeaveRequest input = validRequest();
        input.setStartDate(LocalDate.now().minusDays(1));
        input.setEndDate(LocalDate.now().plusDays(1));

        assertThrows(
            IllegalArgumentException.class,
            () -> service.createRequest("youssef", input)
        );

        verifyNoInteractions(repository, requestConverter, dtoConverter);
    }

    @Test
    void createRequestShouldRejectBlankReason() {
        CreateLeaveRequest input = validRequest();
        input.setReason("   ");

        assertThrows(
            IllegalArgumentException.class,
            () -> service.createRequest("youssef", input)
        );

        verifyNoInteractions(repository, requestConverter, dtoConverter);
    }

    @Test
    void createRequestShouldRejectReasonLongerThan1000Characters() {
        CreateLeaveRequest input = validRequest();
        input.setReason("x".repeat(1001));

        assertThrows(
            IllegalArgumentException.class,
            () -> service.createRequest("youssef", input)
        );

        verifyNoInteractions(repository, requestConverter, dtoConverter);
    }

    @Test
    void getMyRequestsShouldReturnConvertedRequests() {
        LeaveRequest first = pendingEntity();
        LeaveRequest second = pendingEntity();
        LeaveRequestDto firstDto = dto("PENDING");
        LeaveRequestDto secondDto = dto("CANCELLED");

        when(repository.findByRequesterUsernameOrderByCreatedAtDesc("youssef"))
            .thenReturn(Arrays.asList(first, second));
        when(dtoConverter.convert(first)).thenReturn(firstDto);
        when(dtoConverter.convert(second)).thenReturn(secondDto);

        assertEquals(
            Arrays.asList(firstDto, secondDto),
            service.getMyRequests("youssef")
        );

        verify(repository)
            .findByRequesterUsernameOrderByCreatedAtDesc("youssef");
    }

    @Test
    void getMyRequestsShouldReturnEmptyListWhenNoRequestsExist() {
        when(repository.findByRequesterUsernameOrderByCreatedAtDesc("youssef"))
            .thenReturn(Collections.emptyList());

        assertTrue(service.getMyRequests("youssef").isEmpty());
    }

    @Test
    void getAllRequestsShouldReturnConvertedRequests() {
        LeaveRequest entity = pendingEntity();
        LeaveRequestDto expected = dto("PENDING");

        when(repository.findAllByOrderByCreatedAtDesc())
            .thenReturn(Collections.singletonList(entity));
        when(dtoConverter.convert(entity)).thenReturn(expected);

        assertEquals(
            Collections.singletonList(expected),
            service.getAllRequests()
        );
    }

    @Test
    void cancelMyRequestShouldCancelOwnPendingRequest() {
        LeaveRequest entity = pendingEntity();
        LeaveRequestDto expected = dto("CANCELLED");
        expected.setStatus("CANCELLED");

        when(repository.findByIdAndRequesterUsername(1L, "youssef"))
            .thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(dtoConverter.convert(entity)).thenReturn(expected);

        LeaveRequestDto result = service.cancelMyRequest(1L, "youssef");

        assertSame(expected, result);
        assertEquals(LeaveRequest.Status.CANCELLED, entity.getStatus());
        verify(repository).save(entity);
    }

    @Test
    void cancelMyRequestShouldRejectRequestNotOwnedByRequesterOrMissing() {
        when(repository.findByIdAndRequesterUsername(1L, "youssef"))
            .thenReturn(Optional.empty());

        assertThrows(
            IllegalArgumentException.class,
            () -> service.cancelMyRequest(1L, "youssef")
        );

        verify(repository, never()).save(any());
    }

    @Test
    void cancelMyRequestShouldRejectNonPendingRequest() {
        LeaveRequest entity = pendingEntity();
        entity.setStatus(LeaveRequest.Status.APPROVED);

        when(repository.findByIdAndRequesterUsername(1L, "youssef"))
            .thenReturn(Optional.of(entity));

        assertThrows(
            IllegalStateException.class,
            () -> service.cancelMyRequest(1L, "youssef")
        );

        verify(repository, never()).save(any());
    }

    @Test
    void decideRequestShouldApprovePendingRequestAndRecordAdministrator() {
        LeaveRequest entity = pendingEntity();
        LeaveDecisionRequest decision = new LeaveDecisionRequest();
        decision.setDecision(LeaveDecisionRequest.Decision.APPROVED);
        LeaveRequestDto expected = dto("APPROVED");

        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(dtoConverter.convert(entity)).thenReturn(expected);

        LeaveRequestDto result =
            service.decideRequest(1L, "admin", decision);

        assertSame(expected, result);
        assertEquals(LeaveRequest.Status.APPROVED, entity.getStatus());
        assertEquals("admin", entity.getDecisionBy());
        assertNotNull(entity.getDecisionAt());
        verify(repository).save(entity);
    }

    @Test
    void decideRequestShouldRejectPendingRequest() {
        LeaveRequest entity = pendingEntity();
        LeaveDecisionRequest decision = new LeaveDecisionRequest();
        decision.setDecision(LeaveDecisionRequest.Decision.REJECTED);
        LeaveRequestDto expected = dto("REJECTED");

        when(repository.findById(2L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(dtoConverter.convert(entity)).thenReturn(expected);

        LeaveRequestDto result =
            service.decideRequest(2L, "admin", decision);

        assertSame(expected, result);
        assertEquals(LeaveRequest.Status.REJECTED, entity.getStatus());
        assertEquals("admin", entity.getDecisionBy());
        assertNotNull(entity.getDecisionAt());
    }

    @Test
    void decideRequestShouldRejectBlankAdministrator() {
        LeaveDecisionRequest decision = new LeaveDecisionRequest();
        decision.setDecision(LeaveDecisionRequest.Decision.APPROVED);

        assertThrows(
            IllegalArgumentException.class,
            () -> service.decideRequest(1L, " ", decision)
        );

        verifyNoInteractions(repository);
    }

    @Test
    void decideRequestShouldRejectMissingDecision() {
        LeaveDecisionRequest decision = new LeaveDecisionRequest();

        assertThrows(
            IllegalArgumentException.class,
            () -> service.decideRequest(1L, "admin", decision)
        );

        verifyNoInteractions(repository);
    }

    @Test
    void decideRequestShouldRejectMissingRequest() {
        LeaveDecisionRequest decision = new LeaveDecisionRequest();
        decision.setDecision(LeaveDecisionRequest.Decision.APPROVED);

        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
            IllegalArgumentException.class,
            () -> service.decideRequest(99L, "admin", decision)
        );

        verify(repository, never()).save(any());
    }

    @Test
    void decideRequestShouldRejectAlreadyDecidedRequest() {
        LeaveRequest entity = pendingEntity();
        entity.setStatus(LeaveRequest.Status.APPROVED);

        LeaveDecisionRequest decision = new LeaveDecisionRequest();
        decision.setDecision(LeaveDecisionRequest.Decision.REJECTED);

        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        assertThrows(
            IllegalStateException.class,
            () -> service.decideRequest(1L, "admin", decision)
        );

        verify(repository, never()).save(any());
    }
}
