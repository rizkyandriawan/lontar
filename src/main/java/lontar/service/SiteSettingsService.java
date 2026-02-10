package lontar.service;

import lontar.model.SiteSettings;
import lontar.repository.SiteSettingsRepository;
import org.springframework.stereotype.Service;

@Service
public class SiteSettingsService {

    private final SiteSettingsRepository settingsRepository;

    public SiteSettingsService(SiteSettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public SiteSettings getSettings() {
        return settingsRepository.findById(1L).orElseGet(() -> {
            SiteSettings defaults = new SiteSettings();
            return settingsRepository.save(defaults);
        });
    }

    public SiteSettings updateSettings(SiteSettings settings) {
        settings.setId(1L);
        return settingsRepository.save(settings);
    }
}
