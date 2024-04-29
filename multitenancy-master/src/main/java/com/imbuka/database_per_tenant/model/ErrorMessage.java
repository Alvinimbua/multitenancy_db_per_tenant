package com.imbuka.database_per_tenant.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorMessage {

    @JsonProperty("timestamp")

    private OffsetDateTime timestamp;

    @JsonProperty("status")

    private Integer status;

    @JsonProperty("error")

    private String error;

    @JsonProperty("message")


    private String message;

    @JsonProperty("path")
    private String path;
}
