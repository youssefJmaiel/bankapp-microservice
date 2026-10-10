package com.bankapp.hr.repository;

import com.bankapp.hr.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Query("SELECT COUNT(e) > 0 FROM Employee e WHERE LOWER(TRIM(e.email)) = LOWER(TRIM(:email))")
    boolean existsByNormalizedEmail(@Param("email") String email);

    @Query("SELECT COUNT(e) > 0 FROM Employee e WHERE LOWER(TRIM(e.email)) = LOWER(TRIM(:email)) AND e.id <> :id")
    boolean existsByNormalizedEmailAndIdNot(
            @Param("email") String email,
            @Param("id") Long id
    );
}
