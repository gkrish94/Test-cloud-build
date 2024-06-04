/**
 * This Java class is the main application class for a Spring Boot backend application with exclusions
 * for specific auto-configurations.
 */
package com.zinkworks;

import com.zinkworks.rappmanager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.mongo.MongoMetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;

@SpringBootApplication(exclude = {
		MongoAutoConfiguration.class,
		MongoDataAutoConfiguration.class,
		MetricsAutoConfiguration.class,
		MongoMetricsAutoConfiguration.class,
		MongoRepositoriesAutoConfiguration.class,
		SqlInitializationAutoConfiguration.class,
})
public class rappmanager {
	public static void main(String[] args) {
		SpringApplication.run(rappmanager.class, args);

		System.out.println("Application started successfully!");
	}
}
