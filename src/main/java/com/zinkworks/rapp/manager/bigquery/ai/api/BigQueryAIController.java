/**
 * Spring Boot REST controller class for BigQuery ML on Google Cloud.
 */
package com.zinkworks.rapp.manager.bigquery.ai.api;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.bigquery.BigQueryException;
import com.zinkworks.rapp.manager.bigquery.ai.exception.CustomException;
import com.zinkworks.rapp.manager.bigquery.ai.service.BigQueryService;

@RestController
@RequestMapping("/bigquery-ai")
public class BigQueryAIController {

    @Autowired
    private BigQueryService bigQueryService;

    /**
     * Lists all BigQuery models within a specified dataset.
     *
     * This endpoint retrieves a list of models in the provided dataset and returns them
     * in the response body. If no models are found in the dataset, a 404 (Not Found)
     * error response is returned with an appropriate message.
     *
     * @param datasetName The name of the BigQuery dataset to list models from.
     * @return A ResponseEntity containing a list of model details (model ID, description, type)
     *         on success, or an error response with appropriate status code.
     * @throws CustomException If there's a custom error.
     * @throws BigQueryException If there's an error interacting with BigQuery.
     */
    @GetMapping("/model/{datasetName}")
    public ResponseEntity<Object> listModels(@PathVariable String datasetName) 
        throws BigQueryException, CustomException {
        return bigQueryService.listModels(datasetName);
    }

    /**
     * Creates a BigQuery model based on the provided definition in the request body.
     *
     * @param datasetName The name of the dataset where the model will be created.
     *                    Path variable in the request URL.
     * @param modelDefinition A JSON string containing the model definition with the following keys:
     *                       - `modelName`: The desired name for the model (required).
     *                       - `sql`: The complete SQL statement defining the model (required).
     * @return A `ResponseEntity` object with the following possible responses:
     *         - Success (HTTP Status 200): A JSON object with keys:
     *           - `modelName`: The name of the created model.
     *           - `sql`: The original SQL statement used for creation (for reference).
     *         - Bad Request (HTTP Status 400): If required fields (`modelName` or `sql`) are missing.
     *         - Internal Server Error (HTTP Status 500): If there's an error during model creation or parsing the request body.
     * @throws BigQueryException Thrown if there's an error interacting with BigQuery.
     * @throws InterruptedException Thrown if the model creation job is interrupted.
     * @throws JsonProcessingException Thrown if there's an error parsing the JSON request body.
     * @throws CustomException Thrown if there's a custom error.
     */
    @PostMapping("/model/{datasetName}")
    public ResponseEntity<Object> createModel(@PathVariable String datasetName, @RequestBody String modelDefinition)
        throws BigQueryException, InterruptedException, JsonProcessingException, CustomException {

        // Parse the JSON request body (assuming it has "modelName" and "sql" keys)
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> modelMap = mapper.readValue(modelDefinition, Map.class);

        String modelName = modelMap.get("modelName");
        String sql = modelMap.get("sql");

        if (modelName == null || sql == null) {
            throw new IllegalArgumentException("Missing required fields in request: modelName or sql");
        }

        // Call the BigQueryService method to create the model
        return bigQueryService.createModel(datasetName, modelMap);
    }

    /**
     * Deletes a BigQuery model from the specified dataset.
     *
     * @param datasetName The name of the dataset containing the model to delete.
     *                    Path variable in the request URL.
     * @param modelName The name of the model to be deleted. Request parameter.
     * @return A `ResponseEntity` object with the following possible responses:
     *         - Success (HTTP Status 200): A message indicating successful deletion with details (model name and dataset).
     *         - Not Found (HTTP Status 404):  If the specified model cannot be found in the dataset.
     *         - Internal Server Error (HTTP Status 500): If there's an error during deletion or communication with BigQuery.
     * @throws BigQueryException Thrown if there's an error interacting with BigQuery.
     * @throws CustomException Thrown if there's a custom error.
     */
    @DeleteMapping("/model/{datasetName}")
    public ResponseEntity<Object> deleteModel(@PathVariable String datasetName, @RequestParam String modelName)
        throws BigQueryException, CustomException {
        return bigQueryService.deleteModel(datasetName, modelName);
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
    @GetMapping("/checkTraining/{jobId}")
    public ResponseEntity<Object> checkTrainingStatus(@PathVariable String jobId) 
            throws BigQueryException, CustomException {
        return bigQueryService.checkTrainingStatus(jobId);
    }

    /**
     * Evaluates a BigQuery model by executing a provided evaluation query.
     *
     * This endpoint allows you to run a custom query against a specific BigQuery model,
     * allowing you to test and evaluate the model's predictions.
     *
     * @param datasetName The name of the dataset containing the model.
     * @param modelName The name of the BigQuery model to evaluate.
     * @param evaluationQuery A JSON string containing the evaluation query with the "sql" key.
     * @return A `ResponseEntity` object with the following possible responses:
     *         - Success (HTTP Status 200): A JSON object containing the results of the evaluation query.
     *         - Bad Request (HTTP Status 400): If the "sql" key is missing in the request body.
     *         - Not Found (HTTP Status 404): If the specified model or dataset does not exist.
     *         - Internal Server Error (HTTP Status 500): If there's an error executing the evaluation query or communicating with BigQuery.
     * @throws BigQueryException Thrown if there's an error interacting with BigQuery.
     * @throws InterruptedException Thrown if the model evaluation job is interrupted.
     * @throws CustomException Thrown if there's a custom error.
     * @throws JsonProcessingException Thrown if there's an error processing the JSON request body.
     */
    @GetMapping("/model/{datasetName}/{modelName}")
    public ResponseEntity<Object> evaluateModel(String datasetName, String modelName,  
            @RequestBody String evaluationQuery)
            throws BigQueryException, InterruptedException, CustomException, JsonProcessingException {
        
        // Parse the JSON request body ("sql" keys)
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> sqlMap = mapper.readValue(evaluationQuery, Map.class);

        String sql = sqlMap.get("sql");

        if (sql == null) {
            throw new IllegalArgumentException("Missing required field in request: sql");
        }
        
        return bigQueryService.evaluateModel(datasetName, modelName, sqlMap);
    }
}