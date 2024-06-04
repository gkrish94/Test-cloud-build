/**
 * Implementation class for the `CloudStorageService` interface.
 * This class provides concrete implementations for methods that interact with Google Cloud Storage buckets
 * and objects (blobs) using the Google Cloud Storage API client library. It handles tasks like
 * listing buckets, listing objects within a bucket, creating buckets, downloading files, uploading files,
 * and deleting files. 
 */
package com.zinkworks.rapp.manager.cloud.storage.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.google.cloud.storage.StorageOptions;
import com.zinkworks.rapp.manager.cloud.storage.exception.CustomException;

public class CloudStorageServiceImpl implements CloudStorageService {
    /**
     * Injects the Storage client instance for interacting with Google Cloud
     * Storage.
     */
    @Autowired
    private Storage storage = StorageOptions.getDefaultInstance().getService();

    /**
     * Retrieves a list of all buckets within the project associated with the
     * service account credentials
     * used by the application.
     *
     * This method retrieves a paginated list of buckets from Google Cloud Storage
     * and iterates through them
     * to extract their names. The response is a `ResponseEntity` object containing
     * a list of bucket names
     * in JSON format on success, or an error message with appropriate status code
     * on failure.
     *
     * @return A `ResponseEntity` object containing a list of bucket names (on
     *         success)
     *         or an error message (on failure).
     * @throws CustomException If an error occurs during bucket listing due to
     *                         issues within
     *                         the application logic or custom error handling.
     */
    @Override
    public ResponseEntity<Object> listBuckets() throws CustomException {
        try {

            // List all buckets with project ID as prefix
            Page<Bucket> buckets = storage.list(); // Adjust for pagination if needed

            // Prepare the response data (you can customize this)
            List<String> bucketNames = new ArrayList<>();
            for (Bucket bucket : buckets.iterateAll()) { // Use iterator for Page<Bucket>
                bucketNames.add(bucket.getName());
            }

            return ResponseEntity.ok(bucketNames); // Return list of bucket names

        } catch (Exception e) {
            // Handle exceptions during bucket listing
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "ERROR: Failed to list buckets. " + e.getMessage());
        }
    }

    /**
     * Retrieves a list of all data objects (blobs) stored within a specified
     * bucket.
     *
     * This method retrieves information about the blobs within a bucket and returns
     * a `ResponseEntity` object
     * containing a list of maps (in JSON format) on success. Each map represents a
     * data object (blob) and
     * includes details like name, content type, and size. Additionally, creation
     * and update timestamps are
     * included if the corresponding methods are available on the `Blob` class. On
     * failure, it returns a
     * `ResponseEntity` object with an error message and a status code of
     * INTERNAL_SERVER_ERROR.
     *
     * @param bucketName The name of the bucket to retrieve the list of data objects
     *                   (blobs) from.
     * @return A `ResponseEntity` object containing a list of maps with blob
     *         information on success,
     *         or an error message with status code INTERNAL_SERVER_ERROR on
     *         failure.
     * @throws StorageException If an error occurs during data retrieval from Google
     *                          Cloud Storage,
     *                          indicating a problem with the storage service
     *                          itself. (e.g., permission issues, not found)
     * @throws CustomException  If an error occurs within the application logic or
     *                          custom error handling
     *                          during blob listing.
     */
    @Override
    public ResponseEntity<Object> listObjectsOfBucket(String bucketName) throws StorageException, CustomException {
        try {
            // Get the bucket object
            Bucket bucket = storage.get(bucketName);

            // List all blobs within the bucket
            Page<Blob> blobs = bucket.list();

            // Prepare the response data
            List<Map<String, Object>> blobInfoList = new ArrayList<>();
            for (Blob blob : blobs.iterateAll()) {
                // Create a map to store blob information
                Map<String, Object> blobInfoMap = new HashMap<>();
                // Add relevant blob properties to the map
                blobInfoMap.put("name", blob.getName());
                blobInfoMap.put("contentType", blob.getContentType());
                blobInfoList.add(blobInfoMap);
            }

            return ResponseEntity.ok(blobInfoList); // Return list of blob information
        } catch (StorageException e) {
            // Handle exceptions during data retrieval
            throw new StorageException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        } catch (Exception e) {
            // Handle other exceptions
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "ERROR: Failed to list objects in bucket: " + e.getMessage());
        }
    }

    /**
     * Creates a new bucket with the provided name within the project associated
     * with the service account credentials
     * used by the application.
     *
     * This method creates a new bucket using the `bucketName` parameter. On
     * success, it returns a
     * `ResponseEntity` object containing a success message. On failure, it returns
     * a `ResponseEntity` object
     * with an error message and appropriate status code.
     *
     * @param bucketName The name of the bucket to be created.
     * @return A `ResponseEntity` object containing a success message (on success)
     *         or an error message (on failure).
     * @throws CustomException If an error occurs during bucket creation due to
     *                         issues within
     *                         the application logic or custom error handling.
     */
    @Override
    public ResponseEntity<Object> createBucket(String bucketName) throws CustomException {
        try {

            // The name for the new bucket
            System.out.println("Creating bucket " + bucketName);

            // Creates the new bucket
            Bucket bucket = storage.create(BucketInfo.of(bucketName));

            System.out.printf("Bucket %s created.%n", bucket.getName());
            return ResponseEntity.ok("Bucket created successfully."); // Clear success message

        } catch (Exception e) {
            // Handle exceptions during bucket creation (e.g., bucket already exists)
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "ERROR: Failed to create bucket: " + e.getMessage());
        }
    }

    /**
     * Downloads the contents of a specific file (blob) within a bucket.
     *
     * This method retrieves the content of a file (blob) from Google Cloud Storage
     * and returns it
     * as a byte array within a `ResponseEntity` object on success. On failure, it
     * returns a
     * `ResponseEntity` object with an error message and appropriate status code.
     *
     * @param bucketName    The name of the bucket containing the file.
     * @param fileName  The name of the file to download.
     * @return A `ResponseEntity` object containing the file content as a byte array
     *         (on success)
     *         or an error message (on failure).
     * @throws StorageException If an error occurs while accessing the file in
     *                          Google Cloud Storage,
     *                          indicating a problem with the storage service itself
     *                          (e.g., permission issues, not found).
     * @throws CustomException  If an error occurs within the application logic or
     *                          custom error handling
     *                          during file download.
     */
    @Override
    public ResponseEntity<Object> downloadFileFromBucket(String bucketName, String fileName)
            throws StorageException, CustomException {
        try {
            // Construct the BlobId object with bucket name and file name
            BlobId blobId = BlobId.of(bucketName, fileName);

            // Prepare the response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());

            // Download the file content
            Blob blob = storage.get(blobId);
            if (blob == null) {
                // Handle file not found case
                throw new CustomException(HttpStatus.NOT_FOUND.value(), "ERROR: File not found.");
            }

            // Download and return the byte array (success case)
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            blob.downloadTo(byteArrayOutputStream);
            byte[] data = byteArrayOutputStream.toByteArray();
            return ResponseEntity.ok().headers(headers).body(data);
        } catch (StorageException e) {
            // Handle storage-related errors (e.g., permission issues)
            throw new StorageException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        } catch (Exception e) {
            // Handle other exceptions
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "ERROR: Failed to download file from bucket: " + e.getMessage());
        }
    }

    /**
     * Uploads a file to a specified bucket in Google Cloud Storage.
     *
     * This method uploads the provided `file` to the bucket specified by
     * `bucketName`. The method returns a
     * `ResponseEntity` object containing a success message on success, or an error
     * response with a
     * descriptive message and status code on failure.
     *
     * @param bucketName  The name of the bucket to upload the file to.
     * @param file    The file to be uploaded.
     * @return A `ResponseEntity` object containing a success message on success, or
     *         an error response
     *         with a descriptive message and status code on failure.
     * @throws IOException   If an error occurs while reading the file content
     *                          during upload preparation.
     * @throws StorageException If an error occurs during upload due to issues with
     *                          the storage service itself (e.g., permission
     *                          issues).
     * @throws CustomException  If an error occurs within the application logic or
     *                          custom error handling
     *                          during file upload.
     */
    @Override
    public ResponseEntity<Object> uploadFileToBucket(String bucketName, MultipartFile file)
            throws IOException, StorageException, CustomException {

        try {
            // Extract the original filename
            String fileName = file.getOriginalFilename();

            // Construct the BlobId object with bucket name and file name
            BlobId blobId = BlobId.of(bucketName, fileName);

            // Get the bytes from the MultipartFile
            byte[] fileBytes = file.getBytes();

            // Create the BlobInfo object with content type (optional)
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType()) // Set content type from MultipartFile
                    .build();

            // Upload the file content using create with byte array
            storage.create(blobInfo, fileBytes);

            // Return success message
            return ResponseEntity.ok().body("File " + fileName + " uploaded successfully to bucket " + bucketName);
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        } catch (StorageException e) {
            // Handle storage-related errors (e.g., permission issues)
            throw new StorageException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        } catch (Exception e) {
            // Handle other exceptions
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "ERROR: Failed to upload file to bucket: " + e.getMessage());
        }
    }

    /**
     * Deletes a file from a specified bucket in Google Cloud Storage.
     *
     * This method deletes the file identified by `fileName` from the bucket
     * specified by `bucketName`. The method returns a
     * `ResponseEntity` object containing a success message on success, or an error
     * response with a
     * descriptive message and status code on failure.
     *
     * @param bucketName    The name of the bucket containing the file to be
     *                      deleted.
     * @param fileName  The name of the file to be deleted.
     * @return A `ResponseEntity` object containing a success message on success, or
     *         an error response
     *         with a descriptive message and status code on failure.
     * @throws StorageException If an error occurs during deletion due to issues
     *                          with the storage service itself (e.g., permission
     *                          issues, not found).
     * @throws CustomException  If an error occurs within the application logic or
     *                          custom error handling
     *                          during file deletion.
     */
    @Override
    public ResponseEntity<Object> deleteFileFromBucket(String bucketName, String fileName)
            throws StorageException, CustomException {

        try {
            // Construct the BlobId object with bucket name and file name
            BlobId blobId = BlobId.of(bucketName, fileName);

            // Delete the file
            storage.delete(blobId);

            // Return success message
            return ResponseEntity.ok().body("File " + fileName + " deleted successfully from bucket " + bucketName);
        } catch (StorageException e) {
            // Handle storage-related errors (e.g., file not found, permission issues)
            if (e.getCode() == 404) {
                throw new StorageException(HttpStatus.NOT_FOUND.value(), "File not found");
            } else {
                throw new StorageException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Error: Deleting file from bucket failed: " + e.getMessage());
            }
        } catch (Exception e) {
            // Handle other exceptions
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "ERROR: Failed to delete file from bucket: " + e.getMessage());
        }
    }
}
