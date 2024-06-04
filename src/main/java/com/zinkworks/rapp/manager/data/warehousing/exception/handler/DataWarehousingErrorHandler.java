/**
 * Centralized Exception Handling for Data Warehousing Module.
 *
 * This class, `DataWarehousingErrorHandler`, provides centralized exception handling for the Data Warehousing module.
 * It's annotated with `@ControllerAdvice`, enabling it to handle exceptions thrown by controllers within this module.
 * 
 * The class defines various `@ExceptionHandler` methods to handle specific exception types:
 *  - `CustomException`: Handles custom exceptions thrown by your application logic, returning the intended HTTP status code 
 *                        and message.
 *  - `BigQueryException`: Handles exceptions thrown by the BigQuery client library, returning a generic "Internal Server Error"
 *                        response for security reasons.
 *  - `JobException`: Handles exceptions related to BigQuery job management operations, returning a generic "Internal Server Error"
 *                        response.
 *  - `JSONException`: Handles exceptions during JSON data processing, returning a "Bad Request" (400) status code to indicate invalid
 *                      request data.
 *  - `IOException`: Handles potential I/O exceptions that might occur during file uploads or other file-related operations, returning a
 *                    generic "Internal Server Error" response for security reasons.
 * 
 * By centralizing exception handling in this class, you ensure consistent error responses and improve the overall robustness of
 * your Data Warehousing module.
 */
package com.zinkworks.rapp.manager.data.warehousing.exception.handler;

import java.io.IOException;

import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import jakarta.servlet.http.HttpServletRequest;

import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.JobException;
import com.zinkworks.rapp.manager.data.warehousing.exception.CustomException;

@ControllerAdvice
public class DataWarehousingErrorHandler {
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
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("BigQuery Error: " + ex.getMessage());
    }

    /**
     * Handles exceptions of type `JobException`. These exceptions are typically
     * thrown by the BigQuery client library
     * during job management operations (e.g., job creation, deletion, or
     * monitoring). This method returns a generic
     * "Internal Server Error" response for these exceptions.
     * 
     * @param request The HttpServletRequest object associated with the request that
     *                caused the exception.
     * @param ex      The JobException object containing the BigQuery job management
     *                error message.
     * @return A ResponseEntity object with the HTTP status code of
     *         INTERNAL_SERVER_ERROR and a generic error message.
     */
    @ExceptionHandler(JobException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Object> handleJobException(HttpServletRequest request, JobException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("JobException Error: " + ex.getMessage());
    }

    /**
     * Handles exceptions of type `JSONException`. These exceptions are typically
     * thrown when processing JSON data
     * used for BigQuery interactions. This method returns a "Bad Request" (400)
     * status code to indicate invalid
     * request data.
     * 
     * @param request The HttpServletRequest object associated with the request that
     *                caused the exception.
     * @param ex      The JSONException object containing the JSON parsing error
     *                message.
     * @return A ResponseEntity object with the HTTP status code of BAD_REQUEST and
     *         a descriptive error message.
     */
    @ExceptionHandler(JSONException.class)
    @ResponseBody
    public ResponseEntity<Object> handleJSONException(HttpServletRequest request, JSONException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("JSONException Error: " + ex.getMessage());
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
