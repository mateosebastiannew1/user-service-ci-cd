package com.cat.user.service.dto.response;

import java.util.List;

public record ErrorResponse(String message, List<FieldError> errors) {}
