/**
 * This class defines a custom exception (`CustomException`) that extends the
 * built-in `Exception` class. It's used to handle specific errors related to BigQuery model
 * existence or creation failures within the application. This exception type provides
 * additional information in the form of an HTTP status code and a descriptive error message.
 *
 * By throwing `CustomException` instances, the service layer can signal these specific
 * errors to the presentation layer, which can then translate them into appropriate user-facing
 * messages. This approach helps to decouple error handling from the presentation layer concerns.
 */
package com.zinkworks.rapp.manager.bigquery.ai.exception;

public class CustomException extends Exception {

    private final int statusCode;
    private final String message;

    /**
     * Constructor for the CustomException class.
     *
     * @param statusCode The HTTP status code associated with the error.
     * @param message    A descriptive message explaining the error.
     */
    public CustomException(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    /**
     * Getter method to retrieve the HTTP status code associated with the exception.
     *
     * @return The HTTP status code.
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Getter method to retrieve the descriptive error message associated with the
     * exception.
     *
     * @return The error message.
     */
    public String getMessage() {
        return message;
    }
}