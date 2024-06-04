/**
 * This class handles exceptions thrown throughout the application and provides 
 * appropriate HTTP response codes and messages. It leverages Spring's
 * `@ControllerAdvice` annotation to be automatically applied to all controllers.
 */
package com.zinkworks.rapp.manager.cloud.storage.exception.handler;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.cloud.storage.StorageException;
import com.zinkworks.rapp.manager.cloud.storage.exception.CustomException;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class CloudStorageErrorHandler {

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
     * Exception handler for `StorageException` thrown by methods within this
     * controller.
     *
     * This method intercepts `StorageException` exceptions thrown by methods
     * handling Google Cloud Storage operations.
     * It extracts the HTTP status code from the exception object (if available) and
     * uses it to create a
     * `ResponseEntity` object with an appropriate status code and a user-friendly
     * error message containing the exception's message.
     *
     * @param request The HttpServletRequest object representing the client request.
     *                (Not currently used in this implementation)
     * @param ex      The `StorageException` object thrown by the underlying storage
     *                operation.
     * @return A `ResponseEntity` object containing the HTTP status code and a
     *         user-friendly error message.
     */
    @ExceptionHandler(StorageException.class)
    @ResponseBody
    public ResponseEntity<Object> handleStorageException(HttpServletRequest request, StorageException ex) {
        return ResponseEntity.status(ex.getCode()).body("StorageException: " + ex.getMessage());
    }

    /**
     * Exception handler for `IOException` thrown by methods within this controller.
     *
     * This method intercepts `IOException` exceptions that might occur during file
     * uploads or other operations
     * involving file handling. It sets the response status to INTERNAL_SERVER_ERROR
     * (500) and returns a
     * `ResponseEntity` object with a generic error message indicating an internal
     * server error.
     * For security reasons, it's generally recommended not to expose specific
     * details of IOExceptions to the user.
     * 
     * @param request The HttpServletRequest object representing the client request.
     *                (Not currently used in this implementation)
     * @param ex      The `IOException` object thrown during file upload or other
     *                file-related operations.
     * @return A `ResponseEntity` object with status code INTERNAL_SERVER_ERROR and
     *         a generic error message.
     */
    @ExceptionHandler(IOException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Object> handleIOException(HttpServletRequest request, IOException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("IOException: " + ex.getMessage());
    }
}
