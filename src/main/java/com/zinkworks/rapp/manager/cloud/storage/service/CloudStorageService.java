/**
 * Interface defining methods for interacting with Google Cloud Storage buckets and objects (blobs).
 * This service provides functionalities for listing buckets, listing objects within a bucket,
 * creating buckets, downloading files, uploading files, and deleting files. 
 */
package com.zinkworks.rapp.manager.cloud.storage.service;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.storage.StorageException;
import com.zinkworks.rapp.manager.cloud.storage.exception.CustomException;

@Service
public interface CloudStorageService {

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
        ResponseEntity<Object> listBuckets() throws CustomException;

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
         *                          itself.
         * @throws CustomException  If an error occurs within the application logic or
         *                          custom error handling
         *                          during blob listing.
         */
        ResponseEntity<Object> listObjectsOfBucket(String bucketName) throws StorageException, CustomException;

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
        ResponseEntity<Object> createBucket(String bucketName) throws CustomException;

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
        ResponseEntity<Object> downloadFileFromBucket(String bucketName, String fileName)
                        throws StorageException, CustomException;

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
         * @throws StorageException If an error occurs while interacting with Google
         *                          Cloud Storage,
         *                          indicating a problem with the storage service
         *                          itself.
         * @throws CustomException  If an error occurs within the application logic or
         *                          custom error handling
         *                          during file upload.
         */
        ResponseEntity<Object> uploadFileToBucket(String bucketName, MultipartFile file)
                        throws IOException, StorageException, CustomException;

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
         * @throws StorageException If an error occurs while interacting with Google
         *                          Cloud Storage,
         *                          indicating a problem with the storage service
         *                          itself.
         * @throws CustomException  If an error occurs within the application logic or
         *                          custom error handling
         *                          during file deletion.
         */
        ResponseEntity<Object> deleteFileFromBucket(String bucketName, String fileName)
                        throws StorageException, CustomException;
}
