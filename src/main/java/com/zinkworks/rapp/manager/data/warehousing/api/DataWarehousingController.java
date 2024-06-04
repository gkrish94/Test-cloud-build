/**
 * Spring Boot REST controller class for BigQuery executions on Google Cloud.
 */
package com.zinkworks.rapp.manager.data.warehousing.api;

import java.io.IOException;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.JobException;
import com.zinkworks.rapp.manager.data.warehousing.exception.CustomException;
import com.zinkworks.rapp.manager.data.warehousing.service.DataWarehousingService;

@RestController
@RequestMapping("/data-warehousing")
public class DataWarehousingController {

    @Autowired
    private DataWarehousingService dataWarehousingService;

    /**
     * Retrieves a list of all datasets in the current BigQuery project.
     *
     * @return A ResponseEntity object containing a list of dataset names (on
     *         success) or an error message (on failure).
     * @throws BigQueryException If an error occurs while listing datasets.
     * @throws CustomException  If an error occurs within the application logic or
     *                          custom error handling
     *                          during blob listing.
     */
    @GetMapping("/dataset")
    public ResponseEntity<Object> listDatasets() throws BigQueryException, CustomException {
        return dataWarehousingService.listDatasets();
    }

    /**
     * Gets information about all tables and optionally views within a dataset.
     *
     * This endpoint retrieves a list of tables and optionally views (if supported)
     * present in the specified dataset.
     *
     * @param datasetName The name of the dataset to retrieve information for.
     * @return A ResponseEntity object containing a map with information about the
     *         dataset (on success)
     *         or an error message (on failure). The map contains keys:
     *         - "tables": List of {@link TableReference} objects representing
     *         tables in the dataset.
     * @throws BigQueryException If an error occurs while retrieving dataset
     *                           information.
     * @throws CustomException  If an error occurs within the application logic or
     *                          custom error handling
     *                          during blob listing.
     */
    @GetMapping("/dataset/{datasetName}")
    public ResponseEntity<Object> getDatasetInfo(@PathVariable String datasetName)
            throws BigQueryException, CustomException {
        return dataWarehousingService.getDatasetInfo(datasetName);
    }

    /**
     * Creates a new dataset in the current BigQuery project.
     *
     * @param datasetName The name of the dataset to create.
     * @return A ResponseEntity object containing a success message (on success) or
     *         an error message (on failure).
     * @throws BigQueryException If an error occurs while creating the dataset.
     * @throws CustomException  If an error occurs within the application logic or
     *                          custom error handling
     *                          during blob listing.
     */
    @PostMapping("/dataset/{datasetName}")
    public ResponseEntity<Object> createDataset(@PathVariable String datasetName)
            throws BigQueryException, CustomException {
        return dataWarehousingService.createDataset(datasetName);
    }

    /**
     * Deletes a dataset from the current BigQuery project.
     *
     * @param datasetName The name of the dataset to delete.
     * @return A ResponseEntity object containing a success message (on success) or
     *         an error message (on failure).
     * @throws BigQueryException If an error occurs while deleting the dataset.
     * @throws CustomException  If an error occurs within the application logic or
     *                          custom error handling
     *                          during blob listing.
     */
    @DeleteMapping("/dataset/{datasetName}")
    public ResponseEntity<Object> deleteDataset(@PathVariable String datasetName)
            throws BigQueryException, CustomException {
        return dataWarehousingService.deleteDataset(datasetName);
    }

    /**
     * Retrieves data from a BigQuery table.
     *
     * This method retrieves data from the specified BigQuery table identified by
     * `datasetName` and `tableName`.
     * It performs the following actions:
     *
     * 1. Initializes a BigQuery client using the default instance.
     * 2. Creates a TableId object representing the target table based on path
     * variables.
     * 3. Checks if the table exists in BigQuery.
     * 4. If the table doesn't exist, returns a bad request error message.
     * 5. Retrieves the table data using `listTableData`.
     * 6. Retrieves the table schema using `getTable`.
     * 7. Iterates through each data row and converts it to a map using field names
     * and values.
     * 8. Adds the converted map to a list of results.
     * 9. Checks if the result list is empty and returns an appropriate message.
     * 10. Returns a successful response containing the list of maps representing
     * table data.
     *
     * @param datasetName The name of the BigQuery dataset containing the target
     *                    table.
     *                    Path variable in the request URL.
     * @param tableName   The name of the BigQuery table from which to retrieve
     *                    data.
     *                    Path variable in the request URL.
     * @return ResponseEntity containing a success message and list of data maps on
     *         success,
     *         or an error response with appropriate status code and message on
     *         failure.
     * @throws BigQueryException    If there's an error interacting with BigQuery.
     * @throws JobException         If there's an error during a BigQuery job
     *                              execution (not used here).
     * @throws InterruptedException If the thread is interrupted while waiting for a
     *                              job (not used here).
     * @throws CustomException  If an error occurs within the application logic or
     *                          custom error handling
     *                          during blob listing.
     */
    @GetMapping("/data/{datasetName}")
    public ResponseEntity<Object> getData(@PathVariable String datasetName, @RequestParam String tableName)
            throws BigQueryException, JobException, InterruptedException, CustomException {
        return dataWarehousingService.getData(datasetName, tableName);
    }

    /**
     * Creates a new table in BigQuery based on the provided JSON schema in the
     * request body.
     *
     * This endpoint expects a POST request with the following parameters:
     * - **datasetName** (Path Variable): The name of the dataset to create the
     * table in.
     * - **tableName** (Request Parameter): The desired name for the table.
     * - **requestBody** (Request Body): A JSON object containing the table schema
     * definition.
     * The schema definition should have a "fields" array with each element being an
     * object with the following properties:
     * - "name" (String): The name of the table field.
     * - "type" (String): The data type of the table field.
     *
     * @param datasetName      The name of the dataset to create the table in.
     * @param tableName        The desired name for the table.
     * @param requestBodyBytes The request body containing the JSON schema
     *                         definition (as a byte array).
     * @return A ResponseEntity object containing a success message (on success) or
     *         an error message (on failure).
     * @throws BigQueryException If an error occurs while creating the table in
     *                           BigQuery.
     * @throws JSONException     If the request body cannot be parsed as a valid
     *                           JSON object.
     * @throws CustomException  If an error occurs within the application logic or
     *                          custom error handling
     *                          during blob listing.
     */
    @PostMapping("/data/{datasetName}")
    public ResponseEntity<Object> createTable(@PathVariable String datasetName, @RequestParam String tableName,
            @RequestBody byte[] requestBodyBytes)
            throws BigQueryException, JSONException, CustomException {
        return dataWarehousingService.createTable(datasetName, tableName, requestBodyBytes);
    }

    /**
     * Uploads data from a CSV file to a BigQuery table.
     *
     * This method takes a multipart file containing CSV data, uploads it to the
     * specified BigQuery table,
     * and appends the data to the existing table. It performs the following
     * actions:
     *
     * 1. Checks if the uploaded file is empty.
     * 2. References the target dataset and table based on path variables.
     * 3. Configures a write channel for the BigQuery job with the specified table
     * and format options (CSV).
     * 4. Writes the uploaded CSV data directly to the BigQuery channel.
     * 5. Retrieves the completed job and checks for errors.
     * 6. Returns a success message with the table ID upon successful upload.
     *
     * @param datasetName The name of the BigQuery dataset containing the target
     *                    table.
     *                    Path variable in the request URL.
     * @param tableName   The name of the BigQuery table to which data will be
     *                    appended.
     *                    Path variable in the request URL.
     * @param csvFile     The MultipartFile object representing the uploaded CSV
     *                    data.
     *                    Request part named "file".
     * @return ResponseEntity containing a success message and table ID on success,
     *         or an error response with appropriate status code and message on
     *         failure.
     * @throws BigQueryException If there's an error interacting with BigQuery.
     * @throws IOException       If there's an error processing the uploaded CSV
     *                           file.
     * @throws CustomException  If an error occurs within the application logic or
     *                          custom error handling
     *                          during blob listing.
     */
    @PostMapping("/data/{datasetName}/upload")
    public ResponseEntity<Object> uploadDataToTable(@PathVariable String datasetName, @RequestParam String tableName,
            @RequestPart(name = "file") MultipartFile csvFile)
            throws BigQueryException, IOException, CustomException {
        return dataWarehousingService.uploadDataToTable(datasetName, tableName, csvFile);
    }

    /**
     * Deletes a table from a specified dataset in BigQuery.
     *
     * This endpoint expects a DELETE request with the following path variables:
     * - **datasetName** (Path Variable): The name of the dataset containing the
     * table to delete.
     * - **tableName** (Path Variable): The name of the table to be deleted.
     *
     * The response will be a success message indicating the table was deleted or a
     * message indicating the table was not found.
     *
     * @param datasetName The name of the dataset containing the table to delete.
     * @param tableName   The name of the table to be deleted.
     * @return A ResponseEntity object containing a success or error message.
     * @throws BigQueryException    If an error occurs during BigQuery interaction.
     * @throws JobException         If an error occurs during BigQuery job execution
     *                              (unlikely in this case).
     * @throws InterruptedException If the thread is interrupted while waiting for
     *                              deletion (unlikely in this case).
     * @throws CustomException  If an error occurs within the application logic or
     *                          custom error handling
     *                          during blob listing.
     */
    @DeleteMapping("/data/{datasetName}")
    public ResponseEntity<Object> deleteData(@PathVariable String datasetName, @RequestParam String tableName)
            throws BigQueryException, JobException, InterruptedException, CustomException {
        return dataWarehousingService.deleteData(datasetName, tableName);
    }
}
