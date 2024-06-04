/**
 * Configuration class for Cloud Storage integration.
 * 
 * This class provides a bean definition for the `CloudStorageService` interface. 
 * The `cloudStorageService` method creates and returns an instance of the `CloudStorageServiceImpl` class,
 * which implements the functionalities for interacting with Google Cloud Storage buckets and objects.
 */
package com.zinkworks.rapp.manager.cloud.storage;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zinkworks.rapp.manager.cloud.storage.service.CloudStorageService;
import com.zinkworks.rapp.manager.cloud.storage.service.CloudStorageServiceImpl;

@Configuration
public class CloudStorageConfig {

    /**
     * Creates and returns a bean of type `CloudStorageService`.
     * 
     * This method instantiates and returns an implementation of the
     * `CloudStorageService` interface.
     * In this case, it returns a new instance of `CloudStorageServiceImpl`. This
     * bean can be
     * autowired by other components in your application to utilize functionalities
     * for interacting with Google Cloud Storage.
     *
     * @return An instance of `CloudStorageService` (implementation:
     *         CloudStorageServiceImpl).
     */
    @Bean
    public CloudStorageService cloudStorageService() {
        return new CloudStorageServiceImpl();
    }
}
