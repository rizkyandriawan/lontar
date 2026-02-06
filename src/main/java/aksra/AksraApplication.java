package aksra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"aksra", "candi.runtime"})
public class AksraApplication {
    public static void main(String[] args) {
        SpringApplication.run(AksraApplication.class, args);
    }
}
