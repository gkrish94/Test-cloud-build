/**
 * This class implements the `BigQueryService` interface and provides methods
 * for interacting with BigQuery models. It leverages the Spring-injected BigQuery
 * client to perform operations like listing, creating, and deleting models.
 * 
 * This service handles potential exceptions and translates them into appropriate
 * HTTP response codes and messages for the consumer. It uses custom exceptions
 * (`CustomException`) for errors related to model existence or creation failures,
 * and re-throws wrapped BigQueryExceptions for internal server errors.
 */
package com.zinkworks.rapp.manager.bigquery.ai.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.google.api.gax.paging.Page;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQuery.ModelListOption;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobInfo;
import com.google.cloud.bigquery.JobStatus;
import com.google.cloud.bigquery.Model;
import com.google.cloud.bigquery.ModelId;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.zinkworks.rapp.manager.bigquery.ai.exception.CustomException;

public class BigQueryServiceImpl implements BigQueryService {

    // BigQuery client injected via Spring
    @Autowired
    private BigQuery bigquery;

    /**
     * Lists all BigQuery models within a specified dataset.
     *
     * This method retrieves a page of models (up to 100) from the BigQuery client
     * using the provided dataset name. It iterates through the models and creates a list of maps
     * containing details for each model.
     *
     * - If no models are found, a 404 (Not Found) error response is returned with an appropriate message.
     * - If there's an error interacting with BigQuery, a 500 (Internal Server Error) response
     *   is returned with a generic error message.
     *
     * @param datasetName The name of the BigQuery dataset to list models from.
     * @return A ResponseEntity containing a list of model details (model ID, description, type)
     *         on success, or an error response with appropriate status code.
     * @throws BigQueryException If there's an error interacting with BigQuery.
     * @throws CustomException If there's a custom error related to model existence.
     */
    @Override
    public ResponseEntity<Object> listModels(String datasetName) throws BigQueryException, CustomException{
        try {
            Page<Model> models = bigquery.listModels(datasetName,
                    ModelListOption.pageSize(100));
            if (models == null) {
                throw new CustomException(HttpStatus.NOT_FOUND.value(), "Dataset does not contain any models.");
            }

            List<Map<String, Object>> modelList = new ArrayList<>();
            for (com.google.cloud.bigquery.Model model : models.iterateAll()) {
                Map<String, Object> modelDetails = new HashMap<>();
                modelDetails.put("modelId", model.getModelId());
                modelDetails.put("modelDescription", model.getDescription());
                modelDetails.put("modelType", model.getModelType());
                modelList.add(modelDetails);
            }

            return ResponseEntity.ok(modelList);

        } catch (BigQueryException e) {
            throw new BigQueryException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
    }

    /**
     * Creates a BigQuery model based on a provided SQL query definition.
     *
     * This method extracts the model name and SQL query from the provided definition map. It
     * then creates a BigQuery job to execute the query and waits for it to complete.
     *
     * - If the request is missing required fields (`modelName` or `sql`), a 400 (Bad Request)
     *   error response is thrown.
     * - If the job execution fails, a 500 (Internal Server Error) response is thrown with details
     *   about the failure reason retrieved from the job status.
     * - On successful model creation, a 200 (OK) response is returned with a success message.
     *
     * @param datasetName The name of the BigQuery dataset to create the model in.
     * @param modelDefinition A map containing the model name and SQL query definition.
     * @return A ResponseEntity indicating success or failure of the model creation operation.
     * @throws BigQueryException If there's an error interacting with BigQuery.
     * @throws InterruptedException If the job execution is interrupted.
     * @throws CustomException If there's a custom error related to model creation failure.
     */
    @Override
    public ResponseEntity<Object> createModel(String datasetName, Map<String, String> modelDefinition)
            throws BigQueryException, InterruptedException, CustomException {

        String modelName = modelDefinition.get("modelName");
        String sql = modelDefinition.get("sql");

        if (modelName == null || sql == null) {
            throw new IllegalArgumentException("Missing required fields in request: modelName or sql");
        }

        QueryJobConfiguration config = QueryJobConfiguration.newBuilder(sql).build();
        Job job = bigquery.create(JobInfo.of(config));

        if (job.getStatus().getError() == null) {
            return ResponseEntity.ok("Model created successfully: " + modelName + " in dataset: " + datasetName + "\nJobID: " + job.getJobId().getJob());
        } else {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Model creation failed for dataset: "
                    + datasetName + ", definition: " + modelDefinition + ".\nError: " + job.getStatus().getError());
        } 
    }

    /**
     * Deletes a BigQuery model from a specified dataset.
     *
     * This method attempts to delete the model using the provided dataset and model names.
     *
     * - If the model deletion is successful, a 200 (OK) response is returned with a success message.
     * - If the model is not found, a 404 (Not Found) response is thrown with an error message.
     * - If there's an error interacting with BigQuery, a 500 (Internal Server Error) response
     *   is re-thrown (wrapped in a CustomException).
     *
     * @param datasetName The name of the BigQuery dataset containing the model.
     * @param modelName The name of the model to be deleted.
     * @return A ResponseEntity indicating success or failure of the model deletion operation.
     * @throws BigQueryException If there's an error interacting with BigQuery (wrapped in CustomException).
     * @throws CustomException If the model is not found.
     */
    @Override
    public ResponseEntity<Object> deleteModel(String datasetName, String modelName) 
        throws BigQueryException, CustomException {
        try {

            boolean success = bigquery.delete(ModelId.of(datasetName, modelName));
            if (success) {
                return ResponseEntity.ok()
                        .body("Model deleted successfully: " + modelName + " in dataset: " + datasetName);
            } else {
                String errorMessage = "Model not found: " + modelName + " in dataset: " + datasetName;
                //throw new BigQueryException(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorMessage);
                throw new CustomException(HttpStatus.NOT_FOUND.value(), errorMessage);
            }

        } catch (BigQueryException e) {
            throw new BigQueryException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
    }

    /**
     * Checks the status of a BigQuery model training job.
     *
     * @param jobId The ID of the BigQuery job to check.
     * @return A `ResponseEntity` object with the following possible responses:
     *         - Success (HTTP Status 200): A JSON object containing the job status
     *           (e.g., "DONE", "RUNNING", "FAILED") and any error message if the job failed.
     *         - Not Found (HTTP Status 404): If the specified job ID cannot be found.
     *         - Internal Server Error (HTTP Status 500): If there's an error during status retrieval.
     * @throws BigQueryException Thrown if there's an error interacting with BigQuery.
     * @throws CustomException Thrown if there's a custom error.
     */
    @Override
    public ResponseEntity<Object> checkTrainingStatus(String jobId) throws BigQueryException, CustomException {
        try {
            Job job = bigquery.getJob(jobId);

            if (job == null) {
                throw new CustomException(HttpStatus.NOT_FOUND.value(), "Job not found: " + jobId);
            }

            JobStatus status = job.getStatus();
            Map<String, Object> response = new HashMap<>();
            response.put("Status", status.getState().toString());

            if (status.getError() != null) {
                response.put("Error", status.getError().toString());
            }

            return ResponseEntity.ok(response);
        } catch (BigQueryException e) {
            throw new BigQueryException(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Error checking training status: " + e.getMessage());
        }
    }

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
    @Override
    public ResponseEntity<Object> evaluateModel(String datasetName, String modelName,  Map<String, String> evaluationQuery) 
        throws BigQueryException, InterruptedException, CustomException {
            
            String sql = evaluationQuery.get("sql");
    
            if (sql == null) {
                throw new IllegalArgumentException("Missing required fields in request: sql");
            }
    
            QueryJobConfiguration config = QueryJobConfiguration.newBuilder(sql).build();
            Job job = bigquery.create(JobInfo.of(config));
    
            if (job.getStatus().getError() == null) {
                return ResponseEntity.ok("Model evaluation successfully: " + modelName + " in dataset: " + datasetName + "\nJobID: " + job.getJobId().getJob());
            } else {
                throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Model evaulation failed for dataset: "
                        + datasetName + ", definition: " + evaluationQuery + ".\nError: " + job.getStatus().getError());
            }
        }
}