package csye6225.cloud.noteapp;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class NoteApp extends SpringBootServletInitializer {

    private static final Logger logger = LoggerFactory.getLogger(NoteApp.class);

    @Override
    protected SpringApplicationBuilder configure(
            SpringApplicationBuilder builder) {
        return builder.sources(NoteApp.class);
    }

    public static void main(String[] args) {

        SpringApplication.run(NoteApp.class, args);

        logger.info("Application started");
    }
}