package lontar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"lontar", "candi.runtime", "candi.auth.core", "candi.web", "candi.ui", "candi.data"})
public class LontarApplication {
    public static void main(String[] args) {
        SpringApplication.run(LontarApplication.class, args);
    }
}
