package com.zinkworks.rapp.manager.data.warehousing.api;

import com.google.cloud.bigquery.BigQueryException;
import com.zinkworks.rapp.manager.data.warehousing.exception.CustomException;
import com.zinkworks.rapp.manager.data.warehousing.service.DataWarehousingService;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DataWarehousingController.class)
public class DataWarehousingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DataWarehousingService dataWarehousingService;

    @Test
    void testListDatasets_Success() throws Exception {
        ResponseEntity<Object> mockResponse = ResponseEntity.ok().body("[\"dataset1\", \"dataset2\"]"); 

        Mockito.when(dataWarehousingService.listDatasets()).thenReturn(mockResponse);

        mockMvc.perform(get("/data-warehousing/dataset"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("dataset1")))
                .andExpect(content().string(containsString("dataset2")));
    }

    @Test
    void testListDatasets_BigQueryException() throws Exception {
        Mockito.when(dataWarehousingService.listDatasets())
                .thenThrow(new BigQueryException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "ERROR: Failed to list datasets."));

        mockMvc.perform(get("/data-warehousing/dataset"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("ERROR: Failed to list datasets.")));
    }

    @Test
    void testGetDatasetInfo_Success() throws Exception {
        String datasetName = "mydataset";
        ResponseEntity<Object> mockResponse = ResponseEntity.ok().body("[\"table1\", \"table2\"]"); 

        Mockito.when(dataWarehousingService.getDatasetInfo(eq(datasetName))).thenReturn(mockResponse);

        mockMvc.perform(get("/data-warehousing/dataset/{datasetName}", datasetName))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("table1")))
                .andExpect(content().string(containsString("table2")));
    }

    @Test
    void testGetDatasetInfo_BigQueryException() throws Exception {
        String datasetName = "nonexistentdataset"; 

        Mockito.when(dataWarehousingService.getDatasetInfo(eq(datasetName)))
                .thenThrow(new BigQueryException(HttpStatus.NOT_FOUND.value(), "ERROR: Failed to get dataset info:"));

        mockMvc.perform(get("/data-warehousing/dataset/{datasetName}", datasetName))
                .andExpect(status().isInternalServerError()) 
                .andExpect(content().string(containsString("ERROR: Failed to get dataset info:"))); 
    }

    @Test
    void testGetDatasetInfo_CustomException() throws Exception {
        String datasetName = "mydataset";

        Mockito.when(dataWarehousingService.getDatasetInfo(eq(datasetName)))
                .thenThrow(new CustomException(HttpStatus.FORBIDDEN.value(), "Permission denied to access dataset"));

        mockMvc.perform(get("/data-warehousing/dataset/{datasetName}", datasetName))
                .andExpect(status().isForbidden()) 
                .andExpect(content().string(containsString("Permission denied to access dataset"))); 
    }

    @Test
    void testCreateDataset_Success() throws Exception {
        String datasetName = "newdataset";
        ResponseEntity<Object> mockResponse = ResponseEntity.ok().body("Successfully created dataset: " + datasetName);

        Mockito.when(dataWarehousingService.createDataset(eq(datasetName))).thenReturn(mockResponse);

        mockMvc.perform(post("/data-warehousing/dataset/{datasetName}", datasetName))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Successfully created dataset: newdataset"))); 
    }

    @Test
    void testCreateDataset_BigQueryException() throws Exception {
        String datasetName = "newdataset";

        Mockito.when(dataWarehousingService.createDataset(eq(datasetName)))
                .thenThrow(new BigQueryException(HttpStatus.CONFLICT.value(), "ERROR: Failed to create dataset:"));

        mockMvc.perform(post("/data-warehousing/dataset/{datasetName}", datasetName))
                .andExpect(status().isInternalServerError()) // 500
                .andExpect(content().string(containsString("ERROR: Failed to create dataset:")));
    }

    @Test
    void testCreateDataset_CustomException() throws Exception {
        String datasetName = "invalid_dataset"; 

        Mockito.when(dataWarehousingService.createDataset(eq(datasetName)))
                .thenThrow(new CustomException(HttpStatus.BAD_REQUEST.value(), "Invalid dataset name format"));

        mockMvc.perform(post("/data-warehousing/dataset/{datasetName}", datasetName))
                .andExpect(status().isBadRequest()) // 400 
                .andExpect(content().string(containsString("Invalid dataset name format")));
    }

    @Test
    void testDeleteDataset_Success() throws Exception {
        String datasetName = "datasetToDelete";
        ResponseEntity<Object> mockResponse = ResponseEntity.ok().body("Successfully deleted dataset: " + datasetName);

        Mockito.when(dataWarehousingService.deleteDataset(eq(datasetName))).thenReturn(mockResponse);

        mockMvc.perform(delete("/data-warehousing/dataset/{datasetName}", datasetName))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Successfully deleted dataset: datasetToDelete")));
    }

    @Test
    void testDeleteDataset_BigQueryException() throws Exception {
        String datasetName = "datasetToDelete";

        Mockito.when(dataWarehousingService.deleteDataset(eq(datasetName)))
                .thenThrow(new BigQueryException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "ERROR: Failed to delete dataset:"));

        mockMvc.perform(delete("/data-warehousing/dataset/{datasetName}", datasetName))
                .andExpect(status().isInternalServerError()) 
                .andExpect(content().string(containsString("ERROR: Failed to delete dataset:")));
    }

    @Test
    void testDeleteDataset_CustomException_NotFound() throws Exception {
        String datasetName = "nonexistentdataset";

        Mockito.when(dataWarehousingService.deleteDataset(eq(datasetName)))
                .thenThrow(new CustomException(HttpStatus.NOT_FOUND.value(), "Dataset not found"));

        mockMvc.perform(delete("/data-warehousing/dataset/{datasetName}", datasetName))
                .andExpect(status().isNotFound()) 
                .andExpect(content().string(containsString("Dataset not found")));
    }

    @Test
    void testGetData_Success() throws Exception {
        String datasetName = "mydataset";
        String tableName = "mytable";
        ResponseEntity<Object> mockResponse = ResponseEntity.ok().body("[{\"col1\":\"data1\", \"col2\":123}]");

        Mockito.when(dataWarehousingService.getData(eq(datasetName), eq(tableName))).thenReturn(mockResponse);

        mockMvc.perform(get("/data-warehousing/data/{datasetName}?tableName={tableName}", datasetName, tableName))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("col1")))
                .andExpect(content().string(containsString("data1")))
                .andExpect(content().string(containsString("col2")))
                .andExpect(content().string(containsString("123")));
    }

    @Test
    void testGetData_BigQueryException() throws Exception {
        String datasetName = "mydataset";
        String tableName = "nonexistenttable"; 

        Mockito.when(dataWarehousingService.getData(eq(datasetName), eq(tableName)))
                .thenThrow(new BigQueryException(HttpStatus.NOT_FOUND.value(), "ERROR: Failed to get data from table:")); 

        mockMvc.perform(get("/data-warehousing/data/{datasetName}?tableName={tableName}", datasetName, tableName))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("ERROR: Failed to get data from table:"))); 
    }

    @Test
    void testGetData_CustomException_NoData() throws Exception {
        String datasetName = "mydataset";
        String tableName = "emptytable";

        Mockito.when(dataWarehousingService.getData(eq(datasetName), eq(tableName)))
                .thenThrow(new CustomException(HttpStatus.NOT_FOUND.value(), "No data found in table"));

        mockMvc.perform(get("/data-warehousing/data/{datasetName}?tableName={tableName}", datasetName, tableName))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("No data found in table")));
    }

    @Test
    void testCreateTable_Success() throws Exception {
        String datasetName = "mydataset";
        String tableName = "newtable";
        String schemaJson = "{\"fields\": [{\"name\": \"id\", \"type\": \"INTEGER\"}, {\"name\": \"name\", \"type\": \"STRING\"}]}";
        ResponseEntity<Object> mockResponse = ResponseEntity.ok().body("Successfully created table: ");

        Mockito.when(dataWarehousingService.createTable(eq(datasetName), eq(tableName), any(byte[].class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/data-warehousing/data/{datasetName}?tableName={tableName}", datasetName, tableName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(schemaJson.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Successfully created table: "))); 
    }

    @Test
    void testCreateTable_BigQueryException() throws Exception {
        String datasetName = "mydataset";
        String tableName = "newtable";
        String schemaJson = "{\"fields\": [{\"name\": \"id\", \"type\": \"INTEGER\"}, {\"name\": \"name\", \"type\": \"STRING\"}]}"; 

        Mockito.when(dataWarehousingService.createTable(eq(datasetName), eq(tableName), any(byte[].class)))
                .thenThrow(new BigQueryException(HttpStatus.BAD_REQUEST.value(), "ERROR: Failed to create table:"));

        mockMvc.perform(post("/data-warehousing/data/{datasetName}?tableName={tableName}", datasetName, tableName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(schemaJson.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isInternalServerError()) 
                .andExpect(content().string(containsString("ERROR: Failed to create table:"))); 
    }

    @Test
    void testCreateTable_CustomException() throws Exception {
        String datasetName = "mydataset";
        String tableName = "newtable";
        String schemaJson = "{\"fields\": [{\"name\": \"id\", \"type\": \"INTEGER\"}, {\"name\": \"name\", \"type\": \"STRING\"}]}"; 

        Mockito.when(dataWarehousingService.createTable(eq(datasetName), eq(tableName), any(byte[].class)))
                .thenThrow(new CustomException(HttpStatus.CONFLICT.value(), "Table already exists"));

        mockMvc.perform(post("/data-warehousing/data/{datasetName}?tableName={tableName}", datasetName, tableName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(schemaJson.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isConflict()) // 409 Conflict
                .andExpect(content().string(containsString("Table already exists")));
    }

    @Test
    void testUploadDataToTable_Success() throws Exception {
        String datasetName = "mydataset";
        String tableName = "mytable";
        MockMultipartFile csvFile = new MockMultipartFile("file", "data.csv", MediaType.TEXT_PLAIN_VALUE,
                "id,name\n1,John\n2,Jane".getBytes());
        ResponseEntity<Object> mockResponse = ResponseEntity.ok().body("Successfully appended data from CSV to table: ");

        Mockito.when(dataWarehousingService.uploadDataToTable(eq(datasetName), eq(tableName), eq(csvFile)))
                .thenReturn(mockResponse);

        mockMvc.perform(multipart("/data-warehousing/data/{datasetName}/upload?tableName={tableName}", datasetName, tableName)
                .file(csvFile))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Successfully appended data from CSV to table: "))); 
    }

    @Test
    void testUploadDataToTable_BigQueryException() throws Exception {
        String datasetName = "mydataset";
        String tableName = "mytable";
        MockMultipartFile csvFile = new MockMultipartFile("file", "data.csv", MediaType.TEXT_PLAIN_VALUE, 
                                                            "id,name\n1,John\n2,Jane".getBytes());

        Mockito.when(dataWarehousingService.uploadDataToTable(eq(datasetName), eq(tableName), eq(csvFile)))
                .thenThrow(new BigQueryException(HttpStatus.BAD_REQUEST.value(), "ERROR: Failed to upload data to table:"));

        mockMvc.perform(multipart("/data-warehousing/data/{datasetName}/upload?tableName={tableName}", datasetName, tableName)
                .file(csvFile))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("ERROR: Failed to upload data to table:")));
    }

    @Test
    void testUploadDataToTable_IOException() throws Exception {
        String datasetName = "mydataset";
        String tableName = "mytable";
        MockMultipartFile csvFile = new MockMultipartFile("file", "data.csv", MediaType.TEXT_PLAIN_VALUE, 
                                                        "id,name\n1,John\n2,Jane".getBytes());

        Mockito.when(dataWarehousingService.uploadDataToTable(eq(datasetName), eq(tableName), eq(csvFile)))
                .thenThrow(new IOException("ERROR: Failed to upload data to table:")); 

        mockMvc.perform(multipart("/data-warehousing/data/{datasetName}/upload?tableName={tableName}", datasetName, tableName)
                .file(csvFile))
                .andExpect(status().isInternalServerError()) 
                .andExpect(content().string(containsString("ERROR: Failed to upload data to table:")));
    }

    @Test
    void testUploadDataToTable_CustomException_TableNotFound() throws Exception {
        String datasetName = "mydataset";
        String tableName = "nonexistenttable";
        MockMultipartFile csvFile = new MockMultipartFile("file", "data.csv", MediaType.TEXT_PLAIN_VALUE,
                "id,name\n1,John\n2,Jane".getBytes());

        Mockito.when(dataWarehousingService.uploadDataToTable(eq(datasetName), eq(tableName), eq(csvFile)))
                .thenThrow(new CustomException(HttpStatus.NOT_FOUND.value(), "Table not found for upload"));

        mockMvc.perform(multipart("/data-warehousing/data/{datasetName}/upload?tableName={tableName}", datasetName, tableName)
                .file(csvFile))
                .andExpect(status().isNotFound()) 
                .andExpect(content().string(containsString("Table not found for upload")));
    }

    @Test
    void testDeleteData_Success() throws Exception {
        String datasetName = "mydataset";
        String tableName = "tableToDelete";
        ResponseEntity<Object> mockResponse = ResponseEntity.ok().body("Table deleted successfully: " + tableName);

        Mockito.when(dataWarehousingService.deleteData(eq(datasetName), eq(tableName))).thenReturn(mockResponse);

        mockMvc.perform(delete("/data-warehousing/data/{datasetName}?tableName={tableName}", datasetName, tableName))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Table deleted successfully: tableToDelete")));
    }

    @Test
    void testDeleteData_BigQueryException() throws Exception {
        String datasetName = "mydataset";
        String tableName = "tableToDelete";

        Mockito.when(dataWarehousingService.deleteData(eq(datasetName), eq(tableName)))
                .thenThrow(new BigQueryException(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                                                    "ERROR: Failed to delete table:")); 

        mockMvc.perform(delete("/data-warehousing/data/{datasetName}?tableName={tableName}", datasetName, tableName))
                .andExpect(status().isInternalServerError()) 
                .andExpect(content().string(containsString("ERROR: Failed to delete table:"))); 
    }

    @Test
    void testDeleteData_CustomException_TableNotFound() throws Exception {
        String datasetName = "mydataset";
        String tableName = "nonexistenttable";

        Mockito.when(dataWarehousingService.deleteData(eq(datasetName), eq(tableName)))
                .thenThrow(new CustomException(HttpStatus.NOT_FOUND.value(), "Table not found for deletion")); 

        mockMvc.perform(delete("/data-warehousing/data/{datasetName}?tableName={tableName}", datasetName, tableName))
                .andExpect(status().isNotFound()) 
                .andExpect(content().string(containsString("Table not found for deletion"))); 
    }
}