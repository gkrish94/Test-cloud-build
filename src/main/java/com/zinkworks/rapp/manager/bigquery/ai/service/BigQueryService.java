/**
 * This interface defines the BigQuery service contract. It outlines methods for
 * interacting with BigQuery models, including listing, creating, and deleting them.
 * This service is intended to be implemented by a concrete class that interacts with
 * the BigQuery client library to perform these operations.
 *
 * The methods throw specific exceptions to signal potential errors:
 *  - `BigQueryException`: Thrown for errors interacting with the BigQuery service.
 *  - `CustomException`: Thrown for specific errors related to model existence or creation failures.
 *  - `JsonProcessingException` (Optional, implementation-dependent): Thrown if there's an error
 *    parsing the JSON request body (applicable if your implementation uses JSON for model definitions).
 *
 * The service methods return `ResponseEntity<Object>` objects to provide flexibility in the
 * response format (e.g., JSON) and HTTP status codes. The specific format and content of the
 * response objects depend on the implementation and the type of operation performed.
 *
 */
package com.zinkworks.rapp.manager.bigquery.ai.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.cloud.bigquery.BigQueryException;
import com.zinkworks.rapp.manager.bigquery.ai.exception.CustomException;

@Service
public interface BigQueryService {

    /**
    * Lists all BigQuery models within a specified dataset.
    *
    * This method retrieves a list of models in the provided dataset from the BigQuery client
    * and returns them. It's intended to be used by the controller class.
    *
    * @param datasetName The name of the BigQuery dataset to list models from.
    * @return A list of maps containing details (model ID, description, type) for each model
    *         in the dataset, or null if no models are found.
    * @throws BigQueryException If there's an error interacting with BigQuery.
    * @throws CustomException If there's a custom error.
    */
    ResponseEntity<Object> listModels(String datasetName) throws BigQueryException, CustomException;

    /**
    * Creates a BigQuery model based on the provided definition.
    * 
    * @param datasetName The name of the dataset where the model will be created.
    * @param modelDefinition A map containing the model definition with keys:
    *        - `modelName`: The desired name for the model (required).
    *        - `sql`: The complete SQL statement defining the model (required).
    * @return A success message (including model name and dataset) on creation, 
    *         or an error message with appropriate status code.
    * @throws BigQueryException Thrown if there's an error interacting with BigQuery.
    * @throws InterruptedException Thrown if the model creation job is interrupted.
    * @throws CustomException If there's a custom error.
    * @throws JsonProcessingException (Optional) Thrown if there's an error parsing 
    *         the JSON request body (if applicable to your implementation).  
    */
    ResponseEntity<Object>  createModel(String datasetName, Map<String, String> modelDefinition) 
        throws BigQueryException, InterruptedException, CustomException;

    /**
    * Deletes a BigQuery model from the specified dataset.
    * 
    * @param datasetName The name of the dataset containing the model to delete.
    * @param modelName The name of the model to be deleted.
    * @return True if the model was deleted successfully, false otherwise.
    * @throws BigQueryException Thrown if there's an error interacting with BigQuery.
    * @throws CustomException If there's a custom error.
    */
    ResponseEntity<Object> deleteModel(String datasetName, String modelName) throws BigQueryException, CustomException;

    /**
     * Checks the status of a BigQuery model training job.
     *
     * @param jobId The ID of the BigQuery job to check.
     * @return A `ResponseEntity` containing information about the job's status.
     * @throws BigQueryException Thrown if there's an error interacting with BigQuery.
     * @throws CustomException Thrown if there's a custom error.
     */
    ResponseEntity<Object> checkTrainingStatus(String jobId) throws BigQueryException, CustomException;

        /**
     * Evaluates a BigQuery model by executing a provided evaluation query.
     *
     * This method allows you to run a custom query against a specific BigQuery model,
     * allowing you to test and evaluate the model's predictions.
     *
     * @param datasetName The name of the dataset containing the model.
     * @param modelName The name of the BigQuery model to evaluate.
     * @param evaluationQuery A map containing the evaluation query, with "sql" key.
     * @return A `ResponseEntity` object containing the results of the evaluation query.
     * @throws BigQueryException Thrown if there's an error interacting with BigQuery.
     * @throws InterruptedException Thrown if the model evaluation job is interrupted.
     * @throws CustomException Thrown if there's a custom error.
     */
    ResponseEntity<Object> evaluateModel(String datasetName, String modelName,  Map<String, String> evaluationQuery)
            throws BigQueryException, InterruptedException, CustomException;
}
