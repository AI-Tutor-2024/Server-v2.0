package com.example.ai_tutor.global.payload;

import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseCustom<T> {

    private T data;
    private LocalDateTime transactionTime;
    private HttpStatus status;
    private String description;
    private int statusCode;

    // OK
    public static <T> ResponseCustom<T> OK(@Nullable T data) {
        return ResponseCustom.<T>builder()
                .transactionTime(LocalDateTime.now())
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .data(data)
                .build();
    }

    public static <T> ResponseCustom<T> OK() {
        return ResponseCustom.<T>builder()
                .transactionTime(LocalDateTime.now())
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .build();
    }

    // CREATED
    public static <T> ResponseCustom<T> CREATED(@Nullable T data) {
        return ResponseCustom.<T>builder()
                .transactionTime(LocalDateTime.now())
                .status(HttpStatus.CREATED)
                .statusCode(HttpStatus.CREATED.value())
                .data(data)
                .build();
    }

    // BAD REQUEST
    public static <T> ResponseCustom<T> BAD_REQUEST(@Nullable String description) {
        return ResponseCustom.<T>builder()
                .transactionTime(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .description(description)
                .build();
    }

    public static <T> ResponseCustom<T> BAD_REQUEST(@Nullable T data) {
        return ResponseCustom.<T>builder()
                .transactionTime(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .data(data)
                .build();
    }

    // NOT FOUND
    public static <T> ResponseCustom<T> NOT_FOUND(@Nullable T data) {
        return ResponseCustom.<T>builder()
                .transactionTime(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND)
                .statusCode(HttpStatus.NOT_FOUND.value())
                .data(data)
                .build();
    }

    public static <T> ResponseCustom<T> NOT_FOUND(@Nullable String description) {
        return ResponseCustom.<T>builder()
                .transactionTime(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND)
                .statusCode(HttpStatus.NOT_FOUND.value())
                .description(description)
                .build();
    }

    // FORBIDDEN
    public static <T> ResponseCustom<T> FORBIDDEN() {
        return ResponseCustom.<T>builder()
                .transactionTime(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN)
                .statusCode(HttpStatus.FORBIDDEN.value())
                .build();
    }

    public static <T> ResponseCustom<T> FORBIDDEN(String description) {
        return ResponseCustom.<T>builder()
                .transactionTime(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN)
                .statusCode(HttpStatus.FORBIDDEN.value())
                .description(description)
                .build();
    }

    // UNAUTHORIZED
    public static <T> ResponseCustom<T> UNAUTHORIZED() {
        return ResponseCustom.<T>builder()
                .transactionTime(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED)
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .build();
    }

    // INTERNAL SERVER ERROR
    public static <T> ResponseCustom<T> INTERNAL_SERVER_ERROR() {
        return ResponseCustom.<T>builder()
                .transactionTime(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();
    }

    public static <T> ResponseCustom<T> INTERNAL_SERVER_ERROR(String description) {
        return ResponseCustom.<T>builder()
                .transactionTime(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .description(description)
                .build();
    }

    // JWT EXPIRED (Custom code)
    public static <T> ResponseCustom<T> JWT_EXPIRED() {
        return ResponseCustom.<T>builder()
                .transactionTime(LocalDateTime.now())
                .status(null)
                .statusCode(441) // custom status code
                .description("JWT_EXPIRED")
                .build();
    }
}
