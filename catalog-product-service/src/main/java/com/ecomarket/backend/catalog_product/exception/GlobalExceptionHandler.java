package com.ecomarket.backend.catalog_product.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiException> handleNotFound(ResourceNotFoundException ex) {
        ApiException apiEx = new ApiException(
                ex.getMessage(),
                "Resource not found",
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(apiEx, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiException> handleGeneral(Exception ex) {
        ApiException apiEx = new ApiException(
                ex.getMessage(),
                "Unexpected error",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return new ResponseEntity<>(apiEx, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}