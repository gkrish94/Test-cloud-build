/**
 * Interface defining methods for interacting with BigQuery datasets and tables.
 *
 * This interface, `DataWarehousingService`, provides methods for various data warehousing operations on BigQuery.
 *  
 */
package com.zinkworks.rapp.manager.data.warehousing.service;

import java.io.IOException;

import org.json.JSONException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.JobException;
import com.zinkworks.rapp.manager.data.warehousing.exception.CustomException;

public interface DataWarehousingService {

    /**
     * Retrieves a list of available datasets in the BigQuery project.
     *
     * This method retrieves a list of all datasets within the configured BigQuery
     * project.
     * 
     * @throws BigQueryException If an error occurs during interaction with the
     *                           BigQuery API.
     * @throws CustomException   If a custom exception is thrown within the service
     *                           logic.
     * @return A ResponseEntity object containing the list of datasets or an error
     *         response with appropriate HTTP status code.
     */
    ResponseEntity<Object> listDatasets() throws BigQueryException, CustomException;

    /**
     * Gets information about a specific BigQuery dataset.
     *
     * This method retrieves details about a given dataset, including its creation
     * time and schema information.
     * 
     * @param datasetName The name of the dataset to retrieve information for.
     * @throws BigQueryException If an error occurs during interaction with the
     *                           BigQuery API.
     * @throws CustomException   If a custom exception is thrown within the service
     *                           logic.
     * @return A ResponseEntity object containing the dataset information or an
     *         error response with appropriate HTTP status code.
     */
    ResponseEntity<Object> getDatasetInfo(String datasetName) throws BigQueryException, CustomException;

    /**
     * Creates a new BigQuery dataset.
     *
     * This method creates a new dataset with the specified name within the
     * configured BigQuery project.
     * 
     * @param datasetName The name for the new dataset to be created.
     * @throws BigQueryException If an error occurs during interaction with the
     *                           BigQuery API.
     * @throws CustomException   If a custom exception is thrown within the service
     *                           logic.
     * @return A ResponseEntity object with a success message or an error response
     *         with appropriate HTTP status code.
     */
    ResponseEntity<Object> createDataset(String datasetName) throws BigQueryException, CustomException;

    /**
     * Deletes a BigQuery dataset.
     *
     * This method deletes the specified dataset and all its associated tables. Use
     * with caution!
     * 
     * @param datasetName The name of the dataset to be deleted.
     * @throws BigQueryException If an error occurs during interaction with the
     *                           BigQuery API.
     * @throws CustomException   If a custom exception is thrown within the service
     *                           logic.
     * @return A ResponseEntity object with a success message or an error response
     *         with appropriate HTTP status code.
     */
    ResponseEntity<Object> deleteDataset(String datasetName) throws BigQueryException, CustomException;

    /**
     * Retrieves data from a specific BigQuery table.
     *
     * This method executes a query to retrieve data from the specified table. This
     * might involve waiting for the query job to complete.
     * 
     * @param datasetName The name of the dataset containing the table.
     * @param tableName   The name of the table to retrieve data from.
     * @throws BigQueryException    If an error occurs during interaction with the
     *                              BigQuery API.
     * @throws JobException         If a BigQuery job execution error occurs.
     * @throws InterruptedException If the thread is interrupted while waiting for
     *                              the query job to complete.
     * @throws CustomException      If a custom exception is thrown within the
     *                              service logic.
     * @return A ResponseEntity object containing the retrieved data or an error
     *         response with appropriate HTTP status code.
     */
    ResponseEntity<Object> getData(String datasetName, String tableName)
            throws BigQueryException, JobException, InterruptedException, CustomException;

    /**
     * Creates a new BigQuery table from a provided schema and data.
     *
     * This method creates a new table with the specified name in the given dataset.
     * The schema is inferred from the provided request body bytes (typically JSON).
     * 
     * @param datasetName      The name of the dataset to create the table in.
     * @param tableName        The name for the new table to be created.
     * @param requestBodyBytes The byte array representing the schema definition
     *                         (usually JSON).
     * @throws BigQueryException If an error occurs during interaction with the
     *                           BigQuery API.
     * @throws JSONException     If an error occurs while parsing the JSON schema
     *                           from the request body.
     * @throws CustomException   If a custom exception is thrown within the service
     *                           logic.
     * @return A ResponseEntity object with a success message or an error response
     *         with appropriate HTTP status code.
     */
    ResponseEntity<Object> createTable(String datasetName, String tableName, byte[] requestBodyBytes)
            throws BigQueryException, JSONException, CustomException;

    /**
     * Uploads data to an existing BigQuery table from a CSV file.
     *
     * This method uploads data from the provided CSV file to the specified table.
     * 
     * @param datasetName The name of the dataset containing the table.
     * @param tableName   The name of the table to upload data to.
     * @param csvFile     The MultipartFile object representing the CSV file
     *                    containing the data to upload.
     * @throws BigQueryException If an error occurs during interaction with the
     *                           BigQuery API.
     * @throws IOException       If an I/O error occurs while processing the CSV
     *                           file.
     * @throws CustomException   If a custom exception is thrown within the service
     *                           logic.
     * @return A ResponseEntity object with a success message or an error response
     *         with appropriate HTTP status code.
     */
    ResponseEntity<Object> uploadDataToTable(String datasetName, String tableName, MultipartFile csvFile)
            throws BigQueryException, IOException, CustomException;

    /**
     * Deletes data from a specific BigQuery table.
     *
     * This method executes a query to delete data from the specified table. This
     * might involve waiting for the job to complete.
     * 
     * @param datasetName The name of the dataset containing the table.
     * @param tableName   The name of the table to delete data from.
     * @throws BigQueryException    If an error occurs during interaction with the
     *                              BigQuery API.
     * @throws JobException         If a BigQuery job execution error occurs.
     * @throws InterruptedException If the thread is interrupted while waiting for
     *                              the job to complete.
     * @throws CustomException      If a custom exception is thrown within the
     *                              service logic.
     * @return A ResponseEntity object with a success message or an error response
     *         with appropriate HTTP status code.
     */
    ResponseEntity<Object> deleteData(String datasetName, String tableName)
            throws BigQueryException, JobException, InterruptedException, CustomException;
}
