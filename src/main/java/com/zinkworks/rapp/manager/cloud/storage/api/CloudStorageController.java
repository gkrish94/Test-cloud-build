/**
 * Spring Boot REST controller class for interacting with Google Cloud Storage buckets.
 * This controller provides endpoints for managing buckets and objects (blobs) within them.
 * It utilizes the `CloudStorageService` to perform operations on Google Cloud Storage.
 */
package com.zinkworks.rapp.manager.cloud.storage.api;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.storage.StorageException;
import com.zinkworks.rapp.manager.cloud.storage.exception.CustomException;
import com.zinkworks.rapp.manager.cloud.storage.service.CloudStorageService;

@RestController
@RequestMapping("/cloud-storage")
public class CloudStorageController {

    @Autowired
    private CloudStorageService cloudStorageService;

    /**
     * Lists all buckets within the project associated with the service account
     * credentials
     * used by the application.
     *
     * This method retrieves a paginated list of buckets from the Google Cloud
     * Storage service
     * and iterates through them to extract their names. The response is a
     * `ResponseEntity` object
     * containing a list of bucket names in JSON format on success, or an error
     * message with
     * appropriate status code on failure.
     *
     * @return A `ResponseEntity` object containing a list of bucket names (on
     *         success)
     *         or an error message (on failure).
     * @throws CustomException If an error occurs during bucket listing due to
     *                         issues within
     *                         the application logic or custom error handling.
     */
    @GetMapping("/bucket")
    public ResponseEntity<Object> listBuckets() throws CustomException {
        return cloudStorageService.listBuckets();
    }

    /**
     * Retrieves a list of all data objects (blobs) stored within a specified
     * bucket.
     *
     * This request method allows access to information about the blobs within a
     * bucket. It returns
     * a `ResponseEntity` object containing a list of maps (in JSON format) on
     * success. Each map
     * represents a data object (blob) and includes details like name, content type,
     * and size.
     * Additionally, creation and update timestamps are included if the
     * corresponding methods are
     * available on the `Blob` class. On failure, it returns a `ResponseEntity`
     * object with an error
     * message and a status code of INTERNAL_SERVER_ERROR.
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
     *                          itself.
     * @throws CustomException  If an error occurs within the application logic or
     *                          custom error handling
     *                          during blob listing.
     */
    @GetMapping("/bucket/{bucketName}")
    public ResponseEntity<Object> listObjectsOfBucket(@PathVariable String bucketName)
            throws StorageException, CustomException {
        return cloudStorageService.listObjectsOfBucket(bucketName);
    }

    /**
     * Creates a new bucket with the provided name within the project associated
     * with the service account credentials
     * used by the application.
     *
     * This method delegates the bucket creation task to the `CloudStorageService`.
     * It returns a
     * `ResponseEntity` object containing a success message upon successful
     * creation, or an error message
     * with appropriate status code on failure.
     *
     * @param bucketName The name of the bucket to be created.
     * @return A `ResponseEntity` object containing a success message (on success)
     *         or an error message (on failure).
     * @throws CustomException If an error occurs during bucket creation due to
     *                         issues within
     *                         the application logic or custom error handling.
     */
    @PostMapping("/bucket")
    public ResponseEntity<Object> createBucket(@RequestParam String bucketName) throws CustomException {
        return cloudStorageService.createBucket(bucketName);
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
     *                          indicating a problem with the storage service
     *                          itself.
     * @throws CustomException  If an error occurs within the application logic or
     *                          custom error handling
     *                          during file download.
     */
    @GetMapping("/bucketFile/{bucketName}")
    public ResponseEntity<Object> downloadFileFromBucket(@PathVariable String bucketName, @RequestParam String fileName)
            throws StorageException, CustomException {
        return cloudStorageService.downloadFileFromBucket(bucketName, fileName);
    }

    /**
     * Uploads a file to a specified bucket in Google Cloud Storage.
     *
     * This endpoint allows users to upload files to a bucket. It takes the bucket
     * name as a path variable
     * and the file to be uploaded as a request parameter named "file". The method
     * returns a
     * `ResponseEntity` object containing a success message on success, or an error
     * response with a
     * descriptive message and status code on failure.
     *
     * @param bucketName  The name of the bucket to upload the file to. (Path
     *                    Variable)
     * @param file    The file to be uploaded. (Request Parameter named "file")
     * @return A `ResponseEntity` object containing a success message on success, or
     *         an error response
     *         with a descriptive message and status code on failure.
     * @throws IOException   If an error occurs while reading the file content
     *                          during upload preparation.
     * @throws StorageException If an error occurs while interacting with Google
     *                          Cloud Storage,
     *                          indicating a problem with the storage service
     *                          itself.
     * @throws CustomException  If an error occurs within the application logic or
     *                          custom error handling
     *                          during file upload.
     */
    @PostMapping("/bucketFile/{bucketName}")
    public ResponseEntity<Object> uploadFileToBucket(@PathVariable String bucketName,
            @RequestParam("file") MultipartFile file)
            throws IOException, StorageException, CustomException {
        return cloudStorageService.uploadFileToBucket(bucketName, file);
    }

    /**
     * Deletes a file from a specified bucket in Google Cloud Storage.
     *
     * This endpoint allows users to delete a file from a bucket. It takes the
     * bucket name as a path variable
     * and the file name to be deleted as a request parameter named "fileName". The
     * method returns a
     * `ResponseEntity` object containing a success message on success, or an error
     * response with a
     * descriptive message and status code on failure.
     *
     * @param bucketName    The name of the bucket containing the file to be
     *                      deleted. (Path Variable)
     * @param fileName  The name of the file to be deleted. (Request Parameter named
     *                      "fileName")
     * @return A `ResponseEntity` object containing a success message on success, or
     *         an error response
     *         with a descriptive message and status code on failure.
     * @throws StorageException If an error occurs while interacting with Google
     *                          Cloud Storage,
     *                          indicating a problem with the storage service
     *                          itself.
     * @throws CustomException  If an error occurs within the application logic or
     *                          custom error handling
     *                          during file deletion.
     */
    @DeleteMapping("/bucketFile/{bucketName}")
    public ResponseEntity<Object> deleteFileFromBucket(@PathVariable String bucketName, @RequestParam String fileName)
            throws StorageException, CustomException {
        return cloudStorageService.deleteFileFromBucket(bucketName, fileName);
    }
}
