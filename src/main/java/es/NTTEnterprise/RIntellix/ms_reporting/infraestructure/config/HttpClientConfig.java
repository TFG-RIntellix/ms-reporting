package es.NTTEnterprise.RIntellix.ms_reporting.infraestructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Declares the synchronous {@link RestClient} beans used to communicate with
 * ms-core-data and the Gemini API.
 */
@Configuration
public class HttpClientConfig {

    public static final String MS_CORE_DATA_CLIENT = "msCoreDataRestClient";
    public static final String GEMINI_CLIENT = "geminiRestClient";

    @Bean(MS_CORE_DATA_CLIENT)
    public RestClient msCoreDataRestClient(
            @Value("${ms-core-data.base-url:http://localhost:8081}") final String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean(GEMINI_CLIENT)
    public RestClient geminiRestClient(
            @Value("${gemini.base-url:https://generativelanguage.googleapis.com}") final String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
