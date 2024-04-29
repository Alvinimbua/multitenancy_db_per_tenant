package com.imbuka.database_per_tenant.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.time.ZoneId;


public class ApiException extends RuntimeException{

    private static final long serialVersionUID = 1L;
    private static final ZoneId utc = ZoneId.of("UTC");
    @Getter
    private final OffsetDateTime timestamp;

    @Getter
    private final HttpStatus status;

    public ApiException(HttpStatus status, String msg) {
        super(msg);
        this.timestamp = OffsetDateTime.now(utc);
        this.status = status;
    }

}
