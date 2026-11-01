package com.adler.todolist.web.cors;

import org.springframework.boot.webmvc.autoconfigure.WebMvcRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import tools.jackson.databind.ObjectMapper;

/** Troca o processador de CORS dos controllers pelo que responde no contrato JSON. */
@Configuration
public class CorsProcessorConfig {

    @Bean
    public WebMvcRegistrations contractCorsRegistrations(ObjectMapper objectMapper) {
        return new WebMvcRegistrations() {
            @Override
            public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
                RequestMappingHandlerMapping mapping = new RequestMappingHandlerMapping();
                mapping.setCorsProcessor(new ContractCorsProcessor(objectMapper));
                return mapping;
            }
        };
    }
}
