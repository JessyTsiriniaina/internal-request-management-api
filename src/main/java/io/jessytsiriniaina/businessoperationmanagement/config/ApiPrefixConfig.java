package io.jessytsiriniaina.businessoperationmanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ApiPrefixConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // Prefix all controllers in ...controller package with /api
        // Actuator/swagger remain at root because they are in different packages
        configurer.addPathPrefix(
                "/api",
                c -> c.getPackageName().startsWith("io.jessytsiriniaina.businessoperationmanagement.controller"));
    }
}
