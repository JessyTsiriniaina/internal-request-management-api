package io.jessytsiriniaina.internalrequestmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class InternalRequestManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(InternalRequestManagementApplication.class, args);
    }

}
