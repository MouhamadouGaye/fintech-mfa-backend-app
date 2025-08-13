package com.mgaye.banking_application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

// @Data
// @NoArgsConstructor
// @AllArgsConstructor
// public class ErrorResponseDto {
//     private int status;
//     private String error;
//     private String message;
//     private List<String> details;
//     private String path;
//     private LocalDateTime timestamp;

//     public ErrorResponseDto(int status, String error, String message, String path) {
//         this.status = status;
//         this.error = error;
//         this.message = message;
//         this.path = path;
//         this.timestamp = LocalDateTime.now();
//     }
// }

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private String error;
    private String message;
    private List<String> details;
    // private LocalDateTime timestamp;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
}