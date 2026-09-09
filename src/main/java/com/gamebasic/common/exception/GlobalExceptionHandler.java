package com.gamebasic.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleGameNotFound(
            GameNotFoundException exception,
            HttpServletRequest request
    ){
        return respond(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(GameFinishedException.class)
    public ResponseEntity<ErrorResponse> handleGameFinished(
            GameFinishedException exception,
            HttpServletRequest request
    ){
        return respond(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ){
        // 예외 안에서 어떤 필드가 어떤 검증에 실패했는지.
        FieldError fieldError = exception
                .getBindingResult()
                .getFieldErrors()
                .getFirst();

        String message =
                fieldError.getField()
                + " 값이 올바르지 않습니다: "
                + fieldError.getDefaultMessage();

        return respond(
                HttpStatus.BAD_REQUEST,
                message,
                request
        );
    }

    private ResponseEntity<ErrorResponse> respond(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ){
        ErrorResponse response = new ErrorResponse(
                status,
                message,
                request.getRequestURI()
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }
}