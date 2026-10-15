package com.bankapp.account.repository;

import com.bankapp.account.entity.AccountTransfer;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountTransferRepository
        extends JpaRepository<AccountTransfer, Long> {

    Optional<AccountTransfer> findByReference(String reference);

    List<AccountTransfer> findBySourceAccountIdInOrDestinationAccountIdInOrderByCreatedAtDesc(
            List<Long> sourceAccountIds,
            List<Long> destinationAccountIds);

    boolean existsByReference(String reference);
}
