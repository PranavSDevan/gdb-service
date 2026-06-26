package com.gdb.bankstatements.service;

import com.gdb.bankstatements.dto.StatementDto;

public interface StatementService {
    StatementDto generateStatement(StatementDto request);
    StatementDto getStatementStatus(String id);
    byte[] downloadStatement(String id);
}
