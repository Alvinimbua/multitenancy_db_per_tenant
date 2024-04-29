package com.imbuka.database_per_tenant.controller;

import com.imbuka.database_per_tenant.exception.ApiException;
import com.imbuka.database_per_tenant.model.ErrorMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public final ResponseEntity<ErrorMessage> handleApiException(ApiException ex) {
        HttpStatus status = ex.getStatus();
        ErrorMessage errorDetails =
                ErrorMessage.builder()
                        .timestamp(ex.getTimestamp())
                        .status(status.value())
                        .error(status.getReasonPhrase())
                        .message(ex.getMessage())
                        .build();
        return new ResponseEntity<>(errorDetails, status);
    }
}
