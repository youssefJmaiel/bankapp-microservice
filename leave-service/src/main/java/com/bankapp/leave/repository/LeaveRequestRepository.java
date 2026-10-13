package com.bankapp.leave.repository;

import com.bankapp.leave.entity.LeaveRequest;
import com.bankapp.leave.entity.LeaveRequest.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByRequesterUsernameOrderByCreatedAtDesc(String requesterUsername);

    List<LeaveRequest> findAllByOrderByCreatedAtDesc();

    Optional<LeaveRequest> findByIdAndRequesterUsername(Long id, String requesterUsername);

    List<LeaveRequest> findByStatusOrderByCreatedAtDesc(Status status);
}
