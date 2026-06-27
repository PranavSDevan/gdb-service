package com.gdb.bankstatements.controller;

import com.gdb.bankstatements.dto.StatementDto;
import com.gdb.bankstatements.service.StatementService;
import com.gdb.bankstatements.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/statements")
@RequiredArgsConstructor
public class StatementController {

    private final StatementService statementService;

    @PostMapping("/generate")
    public ResponseEntity<StatementDto> generateStatement(@RequestBody StatementDto request) {
        SecurityUtils.checkAnyStaffRole();
        return ResponseEntity.ok(statementService.generateStatement(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StatementDto> getStatementStatus(@PathVariable String id) {
        SecurityUtils.checkAnyStaffRole();
        return ResponseEntity.ok(statementService.getStatementStatus(id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadStatement(@PathVariable String id) {
        SecurityUtils.checkAnyStaffRole();
        byte[] fileData = statementService.downloadStatement(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "statement-" + id + ".txt");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(fileData);
    }
}
