package com.mgaye.banking_application.repository;

import com.mgaye.banking_application.entity.Account;
import com.mgaye.banking_application.entity.Transaction;
import com.mgaye.banking_application.entity.TransactionStatus;
import com.mgaye.banking_application.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

        Optional<Transaction> findByTransactionReference(String transactionReference);

        List<Transaction> findByFromAccountOrToAccountOrderByCreatedAtDesc(Account fromAccount, Account toAccount);

        Page<Transaction> findByFromAccountOrToAccountOrderByCreatedAtDesc(Account fromAccount, Account toAccount,
                        Pageable pageable);

        List<Transaction> findByStatus(TransactionStatus status);

        List<Transaction> findByTransactionType(TransactionType transactionType);

        @Query("SELECT t FROM Transaction t WHERE (t.fromAccount.id = :accountId OR t.toAccount.id = :accountId) " +
                        "AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
        List<Transaction> findTransactionsByAccountAndDateRange(@Param("accountId") Long accountId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.fromAccount.id = :accountId " +
                        "AND t.transactionType = 'WITHDRAWAL' AND t.status = 'COMPLETED' " +
                        "AND DATE(t.createdAt) = CURRENT_DATE")
        BigDecimal getDailyWithdrawalAmount(@Param("accountId") Long accountId);

        @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = 'PENDING' AND t.createdAt < :cutoffTime")
        Long countPendingTransactionsOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);

        @Query("SELECT t FROM Transaction t WHERE (t.fromAccount.user.id = :userId OR t.toAccount.user.id = :userId) " +
                        "ORDER BY t.createdAt DESC")
        Page<Transaction> findTransactionsByUserId(@Param("userId") Long userId, Pageable pageable);
}