package com.gdb.bankstatements.repository;

import com.gdb.bankstatements.domain.StatementTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface StatementTransactionRepository extends JpaRepository<StatementTransaction, String> {
    List<StatementTransaction> findByAccountIdAndTransactionDateBetweenOrderByTransactionDateDesc(String accountId, LocalDate fromDate, LocalDate toDate);
    List<StatementTransaction> findByAccountIdOrderByTransactionDateDesc(String accountId);
}
