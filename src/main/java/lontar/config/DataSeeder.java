package lontar.config;

import lontar.service.SiteSettingsService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements ApplicationRunner {

    private final SiteSettingsService settingsService;

    public DataSeeder(SiteSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Ensure default site settings exist
        settingsService.getSettings();
    }
}
