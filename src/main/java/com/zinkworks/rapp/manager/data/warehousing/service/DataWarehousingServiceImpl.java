/**
 * Implementation of the `DataWarehousingService` interface for interacting with BigQuery datasets and tables.
 *
 * This class provides concrete implementations for the methods defined in the `DataWarehousingService` interface.
 * It interacts with the BigQuery API to perform various data warehousing operations on datasets and tables.
 */
package com.zinkworks.rapp.manager.data.warehousing.service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import com.google.api.gax.paging.Page;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQuery.TableListOption;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.FormatOptions;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobException;
import com.google.cloud.bigquery.JobId;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardSQLTypeName;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableDataWriteChannel;
import com.google.cloud.bigquery.TableDefinition;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableInfo;
import com.google.cloud.bigquery.TableResult;
import com.google.cloud.bigquery.WriteChannelConfiguration;
import com.zinkworks.rapp.manager.data.warehousing.exception.CustomException;

public class DataWarehousingServiceImpl implements DataWarehousingService {

    // Initialize BigQuery client (injected via dependency injection)
    @Autowired
    private BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();

    /**
     * Retrieves a list of available datasets in the configured BigQuery project.
     *
     * This method uses the BigQuery client to list all datasets within the project
     * associated with the configured credentials.
     * It iterates over the results and builds a list of dataset names, which is
     * then returned in the response body.
     *
     * @throws BigQueryException If an error occurs during interaction with the
     *                           BigQuery API.
     * @throws CustomException   If a custom exception is thrown during logic
     *                           processing.
     * @return A ResponseEntity object containing the list of datasets or an error
     *         response with appropriate HTTP status code.
     */
    @Override
    public ResponseEntity<Object> listDatasets() throws BigQueryException, CustomException {
        try {
            List<String> datasetNames = new ArrayList<>();
            bigquery.listDatasets().iterateAll()
                    .forEach(dataset -> datasetNames.add(dataset.getDatasetId().toString()));

            // Return the list of dataset names
            return ResponseEntity.ok(datasetNames);
        } catch (BigQueryException e) {
            // Handle exceptions during dataset listing
            throw new BigQueryException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "ERROR: Failed to list datasets. " + e.getMessage());
        } catch (Exception e) {
            // Handle generic exceptions
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "ERROR: Failed to list datasets. " + e.getMessage());
        }
    }

    /**
     * Gets information about a specific BigQuery dataset.
     *
     * This method retrieves details about a given dataset, including its creation
     * time and schema information (if available).
     * It first gets a reference to the dataset using the dataset name and project
     * ID. Then, it uses the BigQuery client
     * to list tables within that dataset and builds a list of table names. This
     * list is returned in the response body.
     *
     * @param datasetName The name of the dataset to retrieve information for.
     * @throws BigQueryException If an error occurs during interaction with the
     *                           BigQuery API.
     * @throws CustomException   If a custom exception is thrown during logic
     *                           processing.
     * @return A ResponseEntity object containing the dataset information (table
     *         names) or an error response with appropriate HTTP status code.
     */
    @Override
    public ResponseEntity<Object> getDatasetInfo(String datasetName) throws BigQueryException, CustomException {
        try {
            // Get the dataset reference
            DatasetId datasetId = DatasetId.of(bigquery.getOptions().getProjectId(), datasetName);

            Page<Table> tables = bigquery.listTables(datasetId, TableListOption.pageSize(100));
            List<String> tableNames = new ArrayList<>();
            tables.iterateAll().forEach(table -> tableNames.add(table.getTableId().getTable()));

            return ResponseEntity.ok(tableNames);
        } catch (BigQueryException e) {
            throw new BigQueryException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "ERROR: Failed to get dataset info: " + e.getMessage());
        } catch (Exception e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "ERROR: Failed to get dataset info: " + e.getMessage());
        }
    }

    /**
     * Creates a new BigQuery dataset.
     *
     * This method creates a new dataset with the specified name within the
     * configured BigQuery project.
     * It builds a `DatasetInfo` object with the provided name and sets the location
     * attribute (optional).
     * Then, it uses the BigQuery client to create the dataset.
     *
     * @param datasetName The name for the new dataset to be created.
     * @throws BigQueryException If an error occurs during interaction with the
     *                           BigQuery API.
     * @throws CustomException   If a custom exception is thrown during logic
     *                           processing.
     * @return A ResponseEntity object with a success message or an error response
     *         with appropriate HTTP status code.
     */
    @Override
    public ResponseEntity<Object> createDataset(String datasetName) throws BigQueryException, CustomException {
        try {
            DatasetInfo datasetInfo = DatasetInfo.newBuilder(datasetName).setLocation("US").build();
            bigquery.create(datasetInfo);
            return ResponseEntity.ok("Successfully created dataset: " + datasetName);
        } catch (BigQueryException e) {
            throw new BigQueryException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "ERROR: Failed to create dataset: " + e.getMessage());
        } catch (Exception e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "ERROR: Failed to create dataset: " + e.getMessage());
        }
    }

    /**
     * Deletes a BigQuery dataset.
     *
     * This method attempts to delete the dataset with the specified name.
     * It first checks if the dataset exists using the BigQuery client. If it
     * exists, the deletion is performed.
     * A successful deletion results in a success message in the response body. If
     * the dataset is not found,
     * a custom exception is thrown with a specific HTTP status code to indicate the
     * error.
     *
     * @param datasetName The name of the dataset to be deleted.
     * @throws BigQueryException If an error occurs during interaction with the
     *                           BigQuery API.
     * @throws CustomException   If a custom exception is thrown during logic
     *                           processing, including the case where the dataset is
     *                           not found.
     * @return A ResponseEntity object with a success message or an error response
     *         with appropriate HTTP status code.
     */
    @Override
    public ResponseEntity<Object> deleteDataset(@PathVariable String datasetName)
            throws BigQueryException, CustomException {
        try {
            if (bigquery.getDataset(datasetName) != null) {
                DatasetId datasetId = DatasetId.of(bigquery.getOptions().getProjectId(), datasetName);
                bigquery.delete(datasetId);
                return ResponseEntity.ok("Successfully deleted dataset: " + datasetName);
            }
            throw new CustomException(HttpStatus.NOT_FOUND.value(), "ERROR: dataset not found: " + datasetName);
        } catch (BigQueryException e) {
            throw new BigQueryException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "ERROR: Failed to delete dataset: " + e.getMessage());
        } catch (Exception e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "ERROR: Failed to delete dataset: " + e.getMessage());
        }
    }

    /**
     * Retrieves data from a specific BigQuery table.
     *
     * This method executes a query to retrieve data from the specified table. It
     * first checks if the table exists.
     * If it exists, it builds a BigQuery client, identifies the table reference,
     * and uses the client to list table data.
     * The schema and results are then processed to convert them into a list of maps
     * containing column names and corresponding values.
     * An empty list is returned if no data is found in the table. Any errors during
     * BigQuery interaction or data processing
     * are caught and re-thrown as specific exceptions with informative messages.
     *
     * @param datasetName The name of the dataset containing the table.
     * @param tableName   The name of the table to retrieve data from.
     * @throws BigQueryException    If an error occurs during interaction with the
     *                              BigQuery API.
     * @throws JobException         If a BigQuery job execution error occurs (less
     *                              likely in this case).
     * @throws InterruptedException If the thread is interrupted while waiting for
     *                              the job to complete (less likely in this case).
     * @throws CustomException      If a custom exception is thrown during logic
     *                              processing, including the case where the table
     *                              is not found or data processing fails.
     * @return A ResponseEntity object containing the retrieved data (list of maps)
     *         or an error response with appropriate HTTP status code.
     */
    @Override
    public ResponseEntity<Object> getData(String datasetName, String tableName)
            throws BigQueryException, JobException, InterruptedException, CustomException {
        try {

            // Identify the table itself
            TableId tableId = TableId.of(datasetName, tableName);

            // Check if table exists
            if (bigquery.getTable(tableId) == null) {
                throw new CustomException(HttpStatus.NOT_FOUND.value(), "ERROR: Table not found: " + tableName);
            }

            // Get Table Data
            TableResult result = bigquery.listTableData(tableId);
            Schema schema = bigquery.getTable(tableId).getDefinition().getSchema();

            // Convert results to a list of maps
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (FieldValueList row : result.iterateAll()) {
                @SuppressWarnings("null")
                List<Field> fields = schema.getFields(); // Get schema from TableResult
                Map<String, Object> resultMap = new HashMap<>();
                for (int i = 0; i < fields.size(); i++) {
                    resultMap.put(fields.get(i).getName(), row.get(i).getValue());
                }
                resultList.add(resultMap);
            }

            // Check if resultList is empty before returning data
            if (resultList.isEmpty()) {
                throw new CustomException(HttpStatus.NOT_FOUND.value(), "ERROR: No data found in table: " + tableName);
            } else {
                return ResponseEntity.ok(resultList); // Return the list of results
            }
        } catch (BigQueryException e) {
            // Handle exceptions during BigQuery query execution
            throw new BigQueryException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "ERROR: Failed to get data from table: " + e.getMessage());
        } catch (Exception e) {
            // Handle generic exceptions
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "ERROR: Failed to get data from table: " + e.getMessage());
        }
    }

    /**
     * Creates a new BigQuery table from a provided schema and data.
     *
     * This method extracts the schema from the request body (assumed to be JSON
     * format). It then iterates over the schema
     * array (if it exists) and builds a list of `Field` objects representing the
     * table schema. These fields are used to
     * construct a `Schema` object. Subsequently, a `TableDefinition` is created
     * based on the schema.
     * With the table definition and dataset and table references, a `TableInfo`
     * object is built and used to create the table
     * in BigQuery. A success message is returned in the response body upon
     * successful creation.
     *
     * @param datasetName      The name of the dataset to create the table in.
     * @param tableName        The name for the new table to be created.
     * @param requestBodyBytes The byte array representing the schema definition
     *                         (usually JSON).
     * @throws BigQueryException If an error occurs during interaction with the
     *                           BigQuery API.
     * @throws JSONException     If an error occurs while parsing the JSON schema
     *                           from the request body.
     * @throws CustomException   If a custom exception is thrown during logic
     *                           processing, including invalid schema format or
     *                           table creation failure.
     * @return A ResponseEntity object with a success message or an error response
     *         with appropriate HTTP status code.
     */
    @Override
    public ResponseEntity<Object> createTable(String datasetName, String tableName, byte[] requestBodyBytes)
            throws BigQueryException, JSONException, CustomException {
        try {
            // Extract schema from request body
            JSONObject requestSchema = new JSONObject(new String(requestBodyBytes)); // Parse the byte array into a
                                                                                     // JSONObject
            List<Field> fields = new ArrayList<>();
            JSONArray schemaArray = requestSchema.getJSONArray("fields"); // Remove this line if not used
            for (int i = 0; i < schemaArray.length(); i++) { // Remove this block if not used
                JSONObject fieldJson = schemaArray.getJSONObject(i);
                String fieldName = fieldJson.getString("name");
                String fieldType = fieldJson.getString("type");
                fields.add(Field.of(fieldName, StandardSQLTypeName.valueOf(fieldType.toUpperCase()))); // Assuming
                                                                                                       // conversion
                                                                                                       // logic
            }

            // Construct the schema based on extracted fields (assuming Field.of(String,
            // StandardSQLTypeName) exists)
            Schema schema = Schema.of(fields.stream()
                    .map(field -> Field.of(field.getName(), field.getType()))
                    .collect(Collectors.toList()));

            // Construct the table definition
            TableDefinition tableDefinition = StandardTableDefinition.of(schema);

            // Reference the dataset and table
            TableId tableId = TableId.of(datasetName, tableName);
            TableInfo tableInfo = TableInfo.newBuilder(tableId, tableDefinition).build();

            // Create the table
            bigquery.create(tableInfo);
            return ResponseEntity.ok("Successfully created table: " + tableId);
        } catch (BigQueryException e) {
            throw new BigQueryException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "ERROR: Failed to create table: " + e.getMessage());
        } catch (JSONException e) { // Handle potential JSON parsing errors
            throw new JSONException("ERROR: Failed to parse JSON: " + e.getMessage());
        } catch (Exception e) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(),
                    "ERROR: Failed to create table: " + e.getMessage());
        }
    }

    /**
     * Uploads data to an existing BigQuery table from a CSV file.
     *
     * This method uploads data from the provided CSV file to the specified table.
     * It first checks if the CSV file is empty
     * or missing. Then, it verifies if the target table exists. A unique job ID is
     * generated, and a `TableDataWriteChannel`
     * is created to write the CSV data directly to the BigQuery channel. The
     * channel is then closed, and the job associated
     * with the channel is retrieved and waited for completion. Any errors during
     * BigQuery interaction, data processing,
     * or job execution are caught and re-thrown as specific exceptions with
     * informative messages.
     *
     * @param datasetName The name of the dataset containing the table.
     * @param tableName   The name of the table to upload data to.
     * @param csvFile     The MultipartFile object representing the CSV file
     *                    containing the data to upload.
     * @throws BigQueryException If an error occurs during interaction with the
     *                           BigQuery API.
     * @throws IOException       If an I/O error occurs while processing the CSV
     *                           file or interacting with BigQuery channels.
     * @throws CustomException   If a custom exception is thrown during logic
     *                           processing, including the case where the table is
     *                           not found or data upload fails.
     * @return A ResponseEntity object with a success message or an error response
     *         with appropriate HTTP status code.
     */
    @Override
    public ResponseEntity<Object> uploadDataToTable(String datasetName, String tableName, MultipartFile csvFile)
            throws BigQueryException, IOException, CustomException {

        try {
            // Check if CSV file is empty or missing
            if (csvFile.isEmpty()) {
                throw new CustomException(HttpStatus.BAD_REQUEST.value(), "ERROR: Empty CSV file uploaded");
            }

            // Reference the dataset and table
            TableId tableId = TableId.of(datasetName, tableName);

            if (bigquery.getTable(tableId) == null) {
                throw new CustomException(HttpStatus.NOT_FOUND.value(), "ERROR: Table not found: " + tableName);
            }

            // The location must be specified; other fields can be auto-detected.
            String jobName = "jobId_" + UUID.randomUUID().toString();
            JobId jobId = JobId.newBuilder().setLocation("us").setJob(jobName).build();

            try (TableDataWriteChannel writer = bigquery.writer(jobId,
                    WriteChannelConfiguration.newBuilder(tableId)
                            .setFormatOptions(FormatOptions.csv())
                            .build())) {
                writer.write(ByteBuffer.wrap(csvFile.getBytes())); // Write uploaded CSV data directly to the channel
            }

            // Get the Job created by the TableDataWriteChannel and wait for it to complete.
            Job job = bigquery.getJob(jobId);
            Job completedJob = job.waitFor();

            // Check for errors
            if (completedJob == null) {
                throw new IOException("Job not executed since it no longer exists.");
            } else if (completedJob.getStatus().getError() != null) {
                throw new IOException(
                        "BigQuery was unable to load data: \n"
                                + completedJob.getStatus().getError());
            }

            return ResponseEntity.ok("Successfully appended data from CSV to table: " + tableId);
        } catch (BigQueryException | InterruptedException e) {
            throw new BigQueryException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "ERROR: Failed to upload data to table: " + e.getMessage());
        } catch (IOException e) {
            throw new IOException("ERROR: Failed to upload data to table: " + e.getMessage());
        } catch (Exception e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "ERROR: Failed to upload data to table: " + e.getMessage());
        }
    }

    /**
     * Deletes data from a specific BigQuery table.
     *
     * This method attempts to delete the entire table. It first builds a reference
     * to the table using the dataset and table names.
     * Then, it uses the BigQuery client to delete the table. A successful deletion
     * results in a success message in the response body.
     * If the table is not found, a custom exception is thrown with a specific HTTP
     * status code to indicate the error.
     *
     * @param datasetName The name of the dataset containing the table.
     * @param tableName   The name of the table to delete data from.
     * @throws BigQueryException    If an error occurs during interaction with the
     *                              BigQuery API.
     * @throws JobException         If a BigQuery job execution error occurs (less
     *                              likely in this case).
     * @throws InterruptedException If the thread is interrupted while waiting for
     *                              the job to complete (less likely in this case).
     * @throws CustomException      If a custom exception is thrown during logic
     *                              processing, including the case where the table
     *                              is not found or deletion fails.
     * @return A ResponseEntity object with a success message or an error response
     *         with appropriate HTTP status code.
     */
    @Override
    public ResponseEntity<Object> deleteData(String datasetName, String tableName)
            throws BigQueryException, JobException, InterruptedException, CustomException {
        try {
            if (bigquery.delete(TableId.of(datasetName, tableName)) == true) {
                return ResponseEntity.ok("Table deleted successfully: " + tableName);
            } else {
                throw new CustomException(HttpStatus.NOT_FOUND.value(), "ERROR: Table not found: " + tableName);
            }
        } catch (BigQueryException e) {
            // Handle exceptions during BigQuery query execution
            throw new BigQueryException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "ERROR: Failed to delete table: " + e.getMessage());
        } catch (Exception e) {
            // Handle generic exceptions
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "ERROR: Failed to delete table: " + e.getMessage());
        }
    }
}
