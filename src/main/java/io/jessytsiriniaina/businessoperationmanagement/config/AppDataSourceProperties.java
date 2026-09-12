package io.jessytsiriniaina.businessoperationmanagement.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "spring.datasource")
public record AppDataSourceProperties(
        @NotBlank(message = "Datasource URL must not be blank")
        @Pattern(regexp = "^jdbc:postgresql://.+", message = "Datasource URL must start with jdbc:postgresql://")
        String url,

        @NotBlank(message = "Datasource username must not be blank")
        String username,

        @NotBlank(message = "Datasource password must not be blank")
        String password,

        @NotBlank(message = "Datasource driver class name must not be blank")
        String driverClassName
) {
}
