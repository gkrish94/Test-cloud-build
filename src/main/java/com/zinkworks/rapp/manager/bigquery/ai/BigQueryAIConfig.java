/**
 * This class configures beans related to BigQuery AI services within the
 * application context.
 */
package com.zinkworks.rapp.manager.bigquery.ai;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zinkworks.rapp.manager.bigquery.ai.service.BigQueryService;
import com.zinkworks.rapp.manager.bigquery.ai.service.BigQueryServiceImpl;

/**
 * This class configures beans related to BigQuery AI services within the
 * application context.
 */
@Configuration
public class BigQueryAIConfig {

    /**
     * Creates and returns a bean of type `BigQueryService`. This bean is used to
     * interact
     * with BigQuery services within the application.
     * 
     * @return A bean of type `BigQueryService`.
     */
    @Bean
    public BigQueryService bigQueryService() {
        return new BigQueryServiceImpl();
    }
}
