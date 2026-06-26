package com.gdb.ai.service;

import com.gdb.ai.dto.AiResponseDto;

public interface AiService {
    AiResponseDto getResponse(String query, String userId);
}
