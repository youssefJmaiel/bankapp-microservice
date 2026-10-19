package com.bankapp.account.repository;

import com.bankapp.account.entity.AccountCredit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountCreditRepository extends JpaRepository<AccountCredit, Long> {

    Optional<AccountCredit> findByReference(String reference);

    List<AccountCredit> findByAccountIdOrderByCreatedAtDesc(Long accountId);
}
