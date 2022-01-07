package ch.resrc.testing.products.spring_boot.app;

import ch.resrc.testing.products.spring_boot.app_config.TestingConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({TestingConfig.class})
public class TestingApp {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .logStartupInfo(false)
                .sources(TestingConfig.class)
                .run(args);
    }
}
