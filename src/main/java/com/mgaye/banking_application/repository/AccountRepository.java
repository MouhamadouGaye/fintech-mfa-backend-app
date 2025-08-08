package com.mgaye.banking_application.repository;

import com.mgaye.banking_application.entity.Account;
import com.mgaye.banking_application.entity.AccountType;
import com.mgaye.banking_application.entity.User;

import io.lettuce.core.dynamic.annotation.Param;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByUser(User user);

    List<Account> findByUserAndIsActiveTrue(User user);

    List<Account> findByAccountType(AccountType accountType);

    boolean existsByAccountNumber(String accountNumber);

    @Query("SELECT a FROM Account a WHERE a.user.id = :userId AND a.isActive = true")
    List<Account> findActiveAccountsByUserId(@Param("userId") Long userId);

    @Query("SELECT a FROM Account a WHERE a.balance > :amount")
    List<Account> findAccountsWithBalanceGreaterThan(@Param("amount") BigDecimal amount);

    @Query("SELECT a FROM Account a WHERE a.isFrozen = true")
    List<Account> findFrozenAccounts();

    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.user.id = :userId AND a.isActive = true")
    BigDecimal getTotalBalanceByUserId(@Param("userId") Long userId);
}