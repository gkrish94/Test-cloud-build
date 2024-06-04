package com.zinkworks.rapp.manager.cloud.storage.api;

import com.google.cloud.storage.StorageException;
import com.zinkworks.rapp.manager.cloud.storage.exception.CustomException;
import com.zinkworks.rapp.manager.cloud.storage.service.CloudStorageService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CloudStorageController.class)
public class CloudStorageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CloudStorageService cloudStorageService;

    @Test
    void testListBuckets_Success() throws Exception {
        ResponseEntity<Object> mockResponse = ResponseEntity.ok().body("[\"bucket1\", \"bucket2\"]"); // Mocked successful response

        Mockito.when(cloudStorageService.listBuckets()).thenReturn(mockResponse);

        mockMvc.perform(get("/cloud-storage/bucket"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("bucket1")))
                .andExpect(content().string(containsString("bucket2")));
    }

    @Test
    void testListBuckets_CustomException() throws Exception {
        Mockito.when(cloudStorageService.listBuckets())
                .thenThrow(new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error listing buckets"));

        mockMvc.perform(get("/cloud-storage/bucket"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Error listing buckets")));
    }

    @Test
    void testListObjectsOfBucket_Success() throws Exception {
        String bucketName = "my-bucket";
        ResponseEntity<Object> mockResponse = ResponseEntity.ok().body("[{\"name\":\"file1.txt\", \"contentType\":\"text/plain\"}]");

        Mockito.when(cloudStorageService.listObjectsOfBucket(eq(bucketName))).thenReturn(mockResponse);

        mockMvc.perform(get("/cloud-storage/bucket/{bucketName}", bucketName))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("file1.txt")))
                .andExpect(content().string(containsString("text/plain")));
    }

    @Test
    void testListObjectsOfBucket_StorageException() throws Exception {
        String bucketName = "nonexistent-bucket"; // Simulate a non-existent bucket

        Mockito.when(cloudStorageService.listObjectsOfBucket(eq(bucketName)))
                .thenThrow(new StorageException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Bucket not found"));

        mockMvc.perform(get("/cloud-storage/bucket/{bucketName}", bucketName))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("StorageException: Bucket not found")));
    }

    @Test
    void testListObjectsOfBucket_CustomException() throws Exception {
        String bucketName = "my-bucket";

        Mockito.when(cloudStorageService.listObjectsOfBucket(eq(bucketName)))
                .thenThrow(new CustomException(HttpStatus.FORBIDDEN.value(), "Permission denied to access bucket"));

        mockMvc.perform(get("/cloud-storage/bucket/{bucketName}", bucketName))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Permission denied to access bucket")));
    }

    @Test
    void testCreateBucket_Success() throws Exception {
        String bucketName = "new-bucket";
        ResponseEntity<Object> mockResponse = ResponseEntity.ok().body("Bucket created successfully."); 

        Mockito.when(cloudStorageService.createBucket(eq(bucketName))).thenReturn(mockResponse);

        mockMvc.perform(post("/cloud-storage/bucket")
                .param("bucketName", bucketName))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bucket created successfully."))); 
    }

    @Test
    void testCreateBucket_CustomException_BucketAlreadyExists() throws Exception {
        String bucketName = "existing-bucket";

        Mockito.when(cloudStorageService.createBucket(eq(bucketName)))
                .thenThrow(new CustomException(HttpStatus.CONFLICT.value(), "Bucket already exists."));

        mockMvc.perform(post("/cloud-storage/bucket")
                .param("bucketName", bucketName))
                .andExpect(status().isConflict()) // 409 Conflict
                .andExpect(content().string(containsString("Bucket already exists.")));
    }

    @Test
    void testCreateBucket_CustomException_InvalidBucketName() throws Exception {
        String bucketName = "invalid_bucket_name"; // Invalid bucket name (contains underscore)

        Mockito.when(cloudStorageService.createBucket(eq(bucketName)))
                .thenThrow(new CustomException(HttpStatus.BAD_REQUEST.value(), "Invalid bucket name format."));

        mockMvc.perform(post("/cloud-storage/bucket")
                .param("bucketName", bucketName))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid bucket name format.")));
    }

    @Test
    void testDownloadFileFromBucket_Success() throws Exception {
        String bucketName = "my-bucket";
        String fileName = "file1.txt";
        byte[] mockFileData = "This is test file content".getBytes(StandardCharsets.UTF_8);
        ResponseEntity<Object> mockResponse = ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(mockFileData);

        Mockito.when(cloudStorageService.downloadFileFromBucket(eq(bucketName), eq(fileName))).thenReturn(mockResponse);

        mockMvc.perform(get("/cloud-storage/bucketFile/{bucketName}?fileName={fileName}", bucketName, fileName))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\""))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes(mockFileData));
    }

    @Test
    void testDownloadFileFromBucket_StorageException_FileNotFound() throws Exception {
        String bucketName = "my-bucket";
        String fileName = "nonexistent-file.txt"; 

        Mockito.when(cloudStorageService.downloadFileFromBucket(eq(bucketName), eq(fileName)))
                .thenThrow(new StorageException(HttpStatus.NOT_FOUND.value(), "File not found."));

        mockMvc.perform(get("/cloud-storage/bucketFile/{bucketName}?fileName={fileName}", bucketName, fileName))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("StorageException: File not found.")));
    }

    @Test
    void testDownloadFileFromBucket_CustomException_PermissionDenied() throws Exception {
        String bucketName = "my-bucket";
        String fileName = "file1.txt";

        Mockito.when(cloudStorageService.downloadFileFromBucket(eq(bucketName), eq(fileName)))
                .thenThrow(new CustomException(HttpStatus.FORBIDDEN.value(), "Permission denied to access file."));

        mockMvc.perform(get("/cloud-storage/bucketFile/{bucketName}?fileName={fileName}", bucketName, fileName))
                .andExpect(status().isForbidden()) 
                .andExpect(content().string(containsString("Permission denied to access file."))); 
    }

    @Test
    void testUploadFileToBucket_Success() throws Exception {
        String bucketName = "my-bucket";
        MockMultipartFile mockFile = new MockMultipartFile("file", "test-file.txt", 
                                                        MediaType.TEXT_PLAIN_VALUE, "Test file content".getBytes());
        ResponseEntity<Object> mockResponse = ResponseEntity.ok().body("File uploaded successfully.");

        Mockito.when(cloudStorageService.uploadFileToBucket(eq(bucketName), eq(mockFile))).thenReturn(mockResponse);

        mockMvc.perform(multipart("/cloud-storage/bucketFile/{bucketName}", bucketName)
                        .file(mockFile)) 
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("File uploaded successfully."))); 
    }

    @Test
    void testUploadFileToBucket_IOException() throws Exception {
        String bucketName = "my-bucket";
        MockMultipartFile mockFile = new MockMultipartFile("file", "test-file.txt", 
                                                        MediaType.TEXT_PLAIN_VALUE, "Test file content".getBytes());

        Mockito.when(cloudStorageService.uploadFileToBucket(eq(bucketName), eq(mockFile)))
                .thenThrow(new IOException("Error reading file data"));

        mockMvc.perform(multipart("/cloud-storage/bucketFile/{bucketName}", bucketName)
                        .file(mockFile)) 
                .andExpect(status().isInternalServerError()) // Usually 500 for IOException
                .andExpect(content().string(containsString("IOException: Error reading file data")));
    }

    @Test
    void testUploadFileToBucket_StorageException() throws Exception {
        String bucketName = "my-bucket";
        MockMultipartFile mockFile = new MockMultipartFile("file", "test-file.txt", 
                                                        MediaType.TEXT_PLAIN_VALUE, "Test file content".getBytes());

        Mockito.when(cloudStorageService.uploadFileToBucket(eq(bucketName), eq(mockFile)))
                .thenThrow(new StorageException(HttpStatus.FORBIDDEN.value(), "Insufficient permissions."));

        mockMvc.perform(multipart("/cloud-storage/bucketFile/{bucketName}", bucketName)
                        .file(mockFile)) 
                .andExpect(status().isForbidden()) // 403 for permission issue
                .andExpect(content().string(containsString("StorageException: Insufficient permissions."))); 
    }

    @Test
    void testUploadFileToBucket_CustomException() throws Exception {
        String bucketName = "my-bucket";
        MockMultipartFile mockFile = new MockMultipartFile("file", "test-file.txt", 
                                                        MediaType.TEXT_PLAIN_VALUE, "Test file content".getBytes());

        Mockito.when(cloudStorageService.uploadFileToBucket(eq(bucketName), eq(mockFile)))
                .thenThrow(new CustomException(HttpStatus.BAD_REQUEST.value(), "Invalid file type.")); 

        mockMvc.perform(multipart("/cloud-storage/bucketFile/{bucketName}", bucketName)
                        .file(mockFile)) 
                .andExpect(status().isBadRequest()) 
                .andExpect(content().string(containsString("Invalid file type."))); 
    }

    @Test
    void testDeleteFileFromBucket_Success() throws Exception {
        String bucketName = "my-bucket";
        String fileName = "file1.txt";
        ResponseEntity<Object> mockResponse = ResponseEntity.ok().body("File deleted successfully.");

        Mockito.when(cloudStorageService.deleteFileFromBucket(eq(bucketName), eq(fileName))).thenReturn(mockResponse);

        mockMvc.perform(delete("/cloud-storage/bucketFile/{bucketName}?fileName={fileName}", bucketName, fileName))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("File deleted successfully."))); 
    }

    @Test
    void testDeleteFileFromBucket_StorageException_FileNotFound() throws Exception {
        String bucketName = "my-bucket";
        String fileName = "nonexistent-file.txt";

        Mockito.when(cloudStorageService.deleteFileFromBucket(eq(bucketName), eq(fileName)))
                .thenThrow(new StorageException(HttpStatus.NOT_FOUND.value(), "File not found.")); 

        mockMvc.perform(delete("/cloud-storage/bucketFile/{bucketName}?fileName={fileName}", bucketName, fileName))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("StorageException: File not found."))); 
    }

    @Test
    void testDeleteFileFromBucket_CustomException_PermissionIssue() throws Exception {
        String bucketName = "my-bucket";
        String fileName = "file1.txt";

        Mockito.when(cloudStorageService.deleteFileFromBucket(eq(bucketName), eq(fileName)))
                .thenThrow(new CustomException(HttpStatus.FORBIDDEN.value(), "No permission to delete file."));

        mockMvc.perform(delete("/cloud-storage/bucketFile/{bucketName}?fileName={fileName}", bucketName, fileName))
                .andExpect(status().isForbidden()) 
                .andExpect(content().string(containsString("No permission to delete file.")));
    }
}