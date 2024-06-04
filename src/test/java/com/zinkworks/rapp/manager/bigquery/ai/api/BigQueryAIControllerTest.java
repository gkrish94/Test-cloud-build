package com.zinkworks.rapp.manager.bigquery.ai.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.bigquery.BigQueryException;
import com.zinkworks.rapp.manager.bigquery.ai.exception.CustomException;
import com.zinkworks.rapp.manager.bigquery.ai.service.BigQueryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BigQueryAIController.class)
public class BigQueryAIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BigQueryService bigQueryService;

    @Test
    void testListModels_Success() throws Exception {
        String datasetName = "mydataset";
        ResponseEntity<Object> mockResponse = ResponseEntity.ok().body("Mocked Model List"); // Mock successful response

        Mockito.when(bigQueryService.listModels(eq(datasetName))).thenReturn(mockResponse);

        mockMvc.perform(get("/bigquery-ai/model/{datasetName}", datasetName))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mocked Model List"))); // Check for the mock response
    }

    @Test
    void testListModels_NotFound() throws Exception {
        String datasetName = "nonexistentdataset";
        Mockito.when(bigQueryService.listModels(eq(datasetName)))
                .thenThrow(new CustomException(HttpStatus.NOT_FOUND.value(), "Dataset not found"));

        mockMvc.perform(get("/bigquery-ai/model/{datasetName}", datasetName))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Dataset not found")));
    }

    @Test
    void testCreateModel_Success() throws Exception {
        String datasetName = "mydataset";
        String modelName = "mymodel";
        String sql = "SELECT * FROM mytable";

        Map<String, String> modelDefinition = new HashMap<>();
        modelDefinition.put("modelName", modelName);
        modelDefinition.put("sql", sql);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(modelDefinition);

        ResponseEntity<Object> mockResponse = ResponseEntity.ok().body("Model created successfully");

        Mockito.when(bigQueryService.createModel(eq(datasetName), any(Map.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/bigquery-ai/model/{datasetName}", datasetName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Model created successfully")));
    }

    @Test
    void testCreateModel_MissingFields() throws Exception {
        String datasetName = "mydataset";
        String sql = "SELECT * FROM mytable";

        Map<String, String> modelDefinition = new HashMap<>();
        modelDefinition.put("sql", sql); // Missing "modelName"

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(modelDefinition);

        mockMvc.perform(post("/bigquery-ai/model/{datasetName}", datasetName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Missing required fields in request: modelName or sql")));
    }

    @Test
    void testCreateModel_BigQueryException() throws Exception {
        String datasetName = "mydataset";
        String modelName = "mymodel";
        String sql = "SELECT * FROM mytable"; 

        Map<String, String> modelDefinition = new HashMap<>();
        modelDefinition.put("modelName", modelName);
        modelDefinition.put("sql", sql);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(modelDefinition);

        Mockito.when(bigQueryService.createModel(eq(datasetName), any(Map.class)))
                .thenThrow(new BigQueryException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "BigQuery error occurred"));

        mockMvc.perform(post("/bigquery-ai/model/{datasetName}", datasetName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("BigQuery Error")));
    }

    @Test
    void testCreateModel_CustomException() throws Exception {
        String datasetName = "mydataset";
        String modelName = "mymodel";
        String sql = "SELECT * FROM mytable"; 

        Map<String, String> modelDefinition = new HashMap<>();
        modelDefinition.put("modelName", modelName);
        modelDefinition.put("sql", sql);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(modelDefinition);

        Mockito.when(bigQueryService.createModel(eq(datasetName), any(Map.class)))
                .thenThrow(new CustomException(HttpStatus.BAD_REQUEST.value(), "Invalid model definition"));

        mockMvc.perform(post("/bigquery-ai/model/{datasetName}", datasetName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest()) 
                .andExpect(content().string(containsString("Invalid model definition")));
    }

    @Test
    void testDeleteModel_Success() throws Exception {
        String datasetName = "mydataset";
        String modelName = "mymodel";

        ResponseEntity<Object> mockResponse = ResponseEntity.ok().body("Model deleted successfully");
        Mockito.when(bigQueryService.deleteModel(eq(datasetName), eq(modelName))).thenReturn(mockResponse);

        mockMvc.perform(delete("/bigquery-ai/model/{datasetName}?modelName={modelName}", 
                            datasetName, modelName))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Model deleted successfully")));
    }

    @Test
    void testDeleteModel_BigQueryException() throws Exception {
        String datasetName = "mydataset";
        String modelName = "mymodel";

        Mockito.when(bigQueryService.deleteModel(eq(datasetName), eq(modelName)))
                .thenThrow(new BigQueryException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "BigQuery error during delete"));

        mockMvc.perform(delete("/bigquery-ai/model/{datasetName}?modelName={modelName}", datasetName, modelName))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("BigQuery Error")));
    }

    @Test
    void testDeleteModel_CustomException_NotFound() throws Exception {
        String datasetName = "mydataset";
        String modelName = "nonexistentmodel";

        Mockito.when(bigQueryService.deleteModel(eq(datasetName), eq(modelName)))
                .thenThrow(new CustomException(HttpStatus.NOT_FOUND.value(), "Model not found"));

        mockMvc.perform(delete("/bigquery-ai/model/{datasetName}?modelName={modelName}", datasetName, modelName))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Model not found"))); 
    }

    @Test
    void testCheckTrainingStatus_Success() throws Exception {
        String jobId = "job123";
        ResponseEntity<Object> mockResponse = ResponseEntity.ok().body("{\"Status\": \"DONE\"}"); // Mocking a successful status response

        Mockito.when(bigQueryService.checkTrainingStatus(eq(jobId))).thenReturn(mockResponse);

        mockMvc.perform(get("/bigquery-ai/checkTraining/{jobId}", jobId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("{\"Status\": \"DONE\"}")));
    }

    @Test
    void testCheckTrainingStatus_JobNotFound() throws Exception {
        String jobId = "nonexistentjob"; 

        Mockito.when(bigQueryService.checkTrainingStatus(eq(jobId)))
                .thenThrow(new CustomException(HttpStatus.NOT_FOUND.value(), "Job not found"));

        mockMvc.perform(get("/bigquery-ai/checkTraining/{jobId}", jobId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Job not found")));
    }

    @Test
    void testCheckTrainingStatus_BigQueryException() throws Exception {
        String jobId = "job123";

        Mockito.when(bigQueryService.checkTrainingStatus(eq(jobId)))
                .thenThrow(new BigQueryException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error accessing BigQuery"));

        mockMvc.perform(get("/bigquery-ai/checkTraining/{jobId}", jobId))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Error accessing BigQuery")));
    }

    @Test
    void testEvaluateModel_Success() throws Exception {
        String datasetName = "mydataset";
        String modelName = "mymodel";
        String sql = "SELECT * FROM mytable";

        Map<String, String> evaluationQuery = new HashMap<>();
        evaluationQuery.put("sql", sql);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(evaluationQuery);

        ResponseEntity<Object> mockResponse = ResponseEntity.ok().body("Evaluation results");

        Mockito.when(bigQueryService.evaluateModel(eq(datasetName), eq(modelName), any(Map.class))).thenReturn(mockResponse);

        mockMvc.perform(get("/bigquery-ai/model/{datasetName}/{modelName}", datasetName, modelName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void testEvaluateModel_MissingSql() throws Exception {
        String datasetName = "mydataset";
        String modelName = "mymodel";

        Map<String, String> evaluationQuery = new HashMap<>();
        // intentionally missing "sql" key

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(evaluationQuery);

        mockMvc.perform(get("/bigquery-ai/model/{datasetName}/{modelName}", datasetName, modelName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Missing required field in request: sql")));
    }
}
