package csye6225.cloud.noteapp.service;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class MetricsConfig {

    @Value("${publish.metrics:true}")
    private boolean publishMetrics;

    @Value("${metrics.statsd.host:localhost}")
    private String host;

    @Value("${metrics.statsd.port:8125}")
    private int port;

    @Bean
    public StatsDClient statsDClient() {
        return new NonBlockingStatsDClient("", host, port);
    }

}