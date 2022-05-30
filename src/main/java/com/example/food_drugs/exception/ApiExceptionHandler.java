package com.example.food_drugs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZoneId;
import java.time.ZonedDateTime;
/**
 * @author Assem
 */
@ControllerAdvice
public class ApiExceptionHandler {
    Object value;
    @ExceptionHandler(value = {ApiRequestException.class})
    public ResponseEntity<Object> handleApiRequestException(ApiRequestException e){
        value=ApiException
                .builder()
                .message(e.getMessage())
                .body(null)
                .httpStatus(HttpStatus.BAD_REQUEST)
                .zonedDateTime(ZonedDateTime.now(ZoneId.of("Z")))
                .build();

        return new ResponseEntity<>(value,HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(value = {ApiGetException.class})
    public ResponseEntity<Object> handleApiRequestException(ApiGetException e){
        value=ApiException
                .builder()
                .message(e.getMessage())
                .body(null)
                .httpStatus(HttpStatus.NOT_FOUND)
                .zonedDateTime(ZonedDateTime.now(ZoneId.of("Z")))
                .build();

        return new ResponseEntity<>(value,HttpStatus.BAD_REQUEST);
    }
}
