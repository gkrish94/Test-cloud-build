/**
 * Configuration class for the Data Warehousing service.
 * 
 * This class provides a bean definition for the DataWarehousingService interface.
 * The bean is created using the DataWarehousingServiceImpl implementation class. 
*/
package com.zinkworks.rapp.manager.data.warehousing;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zinkworks.rapp.manager.data.warehousing.service.DataWarehousingService;
import com.zinkworks.rapp.manager.data.warehousing.service.DataWarehousingServiceImpl;

@Configuration
public class DataWarehousingConfig {

    /**
     * Creates a bean of type DataWarehousingService.
     * 
     * This method creates a bean instance of the DataWarehousingService interface.
     * It is implemented by the DataWarehousingServiceImpl class, which provides
     * concrete implementations for interacting with BigQuery datasets and tables.
     * 
     * @return A bean instance of type DataWarehousingService.
     */
    @Bean
    public DataWarehousingService dataWarehousingService() {
        return new DataWarehousingServiceImpl();
    }
}