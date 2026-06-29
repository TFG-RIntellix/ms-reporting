package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Declares the synchronous {@link RestClient} beans used to communicate with
 * ms-core-data and the Gemini API.
 * 
 * @author Lucía Fernández Mancebo
 * @Date 29-06-2026
 */
@Configuration
public class HttpClientConfig {

    /**
     * Qualifier name for the ms-core-data rest client bean.
     */
    public static final String MS_CORE_DATA_CLIENT = "msCoreDataRestClient";

    /**
     * Qualifier name for the Gemini API rest client bean.
     */
    public static final String GEMINI_CLIENT = "geminiRestClient";

    /**
     * Declares the RestClient bean for communicating with ms-core-data.
     *
     * @param baseUrl the configured base url of ms-core-data
     * @return the rest client instance
     */
    @Bean(MS_CORE_DATA_CLIENT)
    public RestClient msCoreDataRestClient(
            @Value("${ms-core-data.base-url:http://localhost:8081}") final String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * Declares the RestClient bean for communicating with the Gemini API.
     *
     * @param baseUrl the configured base url of the Gemini API
     * @return the rest client instance
     */
    @Bean(GEMINI_CLIENT)
    public RestClient geminiRestClient(
            @Value("${gemini.base-url:https://generativelanguage.googleapis.com}") final String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
