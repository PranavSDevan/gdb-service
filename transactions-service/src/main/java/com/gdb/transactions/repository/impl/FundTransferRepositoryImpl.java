package com.gdb.transactions.repository.impl;

import com.gdb.transactions.domain.model.FundTransfer;
import com.gdb.transactions.repository.FundTransferRepository;
import com.gdb.transactions.repository.mapper.FundTransferRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FundTransferRepositoryImpl implements FundTransferRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final FundTransferRowMapper rowMapper;

    @Override
    public FundTransfer save(FundTransfer fundTransfer) {
        if (fundTransfer.getId() == null) {
            return insert(fundTransfer);
        } else {
            return update(fundTransfer);
        }
    }

    private FundTransfer insert(FundTransfer fundTransfer) {

        String sql = """
            INSERT INTO fund_transfers (from_account, to_account, transfer_amount, transfer_mode)
            VALUES (:fromAccount, :toAccount, :amount, :transferMode)
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("fromAccount", fundTransfer.getFromAccount())
                .addValue("toAccount", fundTransfer.getToAccount())
                .addValue("amount", fundTransfer.getTransferAmount())
                .addValue("transferMode", fundTransfer.getTransferMode().name());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        namedParameterJdbcTemplate.update(sql, params, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return findById(id)
                .orElseThrow(() -> new RuntimeException("Failed to retrieve saved fund transfer"));
    }

    private FundTransfer update(FundTransfer fundTransfer) {

        String sql = """
            UPDATE fund_transfers 
            SET from_account = :fromAccount,
                to_account = :toAccount,
                transfer_amount = :amount,
                transfer_mode = :transferMode
            WHERE id = :id
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("fromAccount", fundTransfer.getFromAccount())
                .addValue("toAccount", fundTransfer.getToAccount())
                .addValue("amount", fundTransfer.getTransferAmount())
                .addValue("transferMode", fundTransfer.getTransferMode().name())
                .addValue("id", fundTransfer.getId());

        namedParameterJdbcTemplate.update(sql, params);

        return findById(fundTransfer.getId())
                .orElseThrow(() -> new RuntimeException("Failed to retrieve updated fund transfer"));
    }

    @Override
    public Optional<FundTransfer> findById(Long id) {

        String sql = """
            SELECT id, from_account, to_account, transfer_amount, transfer_mode, created_at, updated_at
            FROM fund_transfers
            WHERE id = :id
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        List<FundTransfer> results =
                namedParameterJdbcTemplate.query(sql, params, rowMapper);

        return results.isEmpty()
                ? Optional.empty()
                : Optional.of(results.get(0));
    }

    @Override
    public BigDecimal getDailyTransferAmount(Long accountNumber, LocalDate date) {

        String sql = """
            SELECT COALESCE(SUM(transfer_amount), 0)
            FROM fund_transfers
            WHERE from_account = :accountNumber
              AND DATE(created_at) = :date
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("accountNumber", accountNumber)
                .addValue("date", date);

        BigDecimal result =
                namedParameterJdbcTemplate.queryForObject(sql, params, BigDecimal.class);

        return result != null ? result : BigDecimal.ZERO;
    }

    @Override
    public Integer getDailyTransferCount(Long accountNumber, LocalDate date) {

        String sql = """
            SELECT COUNT(*)
            FROM fund_transfers
            WHERE from_account = :accountNumber
              AND DATE(created_at) = :date
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("accountNumber", accountNumber)
                .addValue("date", date);

        Integer result =
                namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);

        return result != null ? result : 0;
    }

    @Override
    public List<FundTransfer> findByAccount(Long accountNumber, int limit, int offset) {

        String sql = """
            SELECT id, from_account, to_account, transfer_amount, transfer_mode, created_at, updated_at
            FROM fund_transfers
            WHERE from_account = :accountNumber
               OR to_account = :accountNumber
            ORDER BY created_at DESC
            LIMIT :limit OFFSET :offset
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("accountNumber", accountNumber)
                .addValue("limit", limit)
                .addValue("offset", offset);

        return namedParameterJdbcTemplate.query(sql, params, rowMapper);
    }

    @Override
    public List<FundTransfer> findAll(int limit, int offset) {

        String sql = """
            SELECT id, from_account, to_account, transfer_amount, transfer_mode, created_at, updated_at
            FROM fund_transfers
            ORDER BY created_at DESC
            LIMIT :limit OFFSET :offset
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("limit", limit)
                .addValue("offset", offset);

        return namedParameterJdbcTemplate.query(sql, params, rowMapper);
    }

    @Override
    public Long countAll() {

        String sql = "SELECT COUNT(*) FROM fund_transfers";

        return namedParameterJdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), Long.class);
    }
}