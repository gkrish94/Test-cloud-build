/**
 * This class handles exceptions thrown throughout the application and provides 
 * appropriate HTTP response codes and messages. It leverages Spring's
 * `@ControllerAdvice` annotation to be automatically applied to all controllers.
 */
package com.zinkworks.rapp.manager.bigquery.ai.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.cloud.bigquery.BigQueryException;
import com.zinkworks.rapp.manager.bigquery.ai.exception.CustomException;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class BigQueryAIErrorHandler {
    /**
     * Handles exceptions of type `CustomException`. These exceptions are typically
     * thrown
     * by your application logic and contain a specific HTTP status code and
     * message.
     * This method extracts the status code and message from the exception and
     * returns a
     * corresponding ResponseEntity object.
     * 
     * @param request The HttpServletRequest object associated with the request that
     *                caused the exception.
     * @param ex      The CustomException object containing the specific status code
     *                and message.
     * @return A ResponseEntity object with the appropriate HTTP status code and
     *         message from the CustomException.
     */
    @ExceptionHandler(CustomException.class)
    @ResponseBody
    public ResponseEntity<Object> handleCustomException(HttpServletRequest request, CustomException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(ex.getMessage());
    }

    /**
     * Handles exceptions of type `BigQueryException`. These exceptions are
     * typically thrown
     * by the BigQuery client library and may indicate errors during interaction
     * with BigQuery.
     * This method returns a generic "Internal Server Error" response for these
     * exceptions.
     * 
     * @param request The HttpServletRequest object associated with the request that
     *                caused the exception.
     * @param ex      The BigQueryException object containing the BigQuery error
     *                message.
     * @return A ResponseEntity object with the HTTP status code of
     *         INTERNAL_SERVER_ERROR and a generic error message.
     */
    @ExceptionHandler(BigQueryException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Object> handleBigQueryException(HttpServletRequest request, BigQueryException ex) {
        String errorMessage = "BigQuery Error: " + ex.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
    }

    /**
     * Handles exceptions of type `IllegalArgumentException`. These exceptions are
     * typically thrown
     * when a method receives an invalid argument. This method returns a "Bad
     * Request" response
     * with the exception's message.
     * 
     * @param request The HttpServletRequest object associated with the request that
     *                caused the exception.
     * @param ex      The IllegalArgumentException object containing the reason for
     *                the invalid argument.
     * @return A ResponseEntity object with the HTTP status code of BAD_REQUEST and
     *         the exception's message.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> handleIllegalArgumentException(HttpServletRequest request,
            IllegalArgumentException ex) {
        String errorMessage = "Bad Request: " + ex.getMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }
}