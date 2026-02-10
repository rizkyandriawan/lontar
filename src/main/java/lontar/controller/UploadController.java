package lontar.controller;

import candi.saas.storage.StorageResult;
import candi.saas.storage.StorageService;
import candi.saas.storage.UploadOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final StorageService storageService;

    public UploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file provided"));
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Only image files are allowed"));
        }

        if (file.getSize() > 10_000_000) {
            return ResponseEntity.badRequest().body(Map.of("error", "File too large (max 10MB)"));
        }

        try {
            StorageResult result = storageService.upload(file, new UploadOptions(
                    "lontar", "images", 10_000_000,
                    Set.of("image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml"),
                    true
            ));
            return ResponseEntity.ok(Map.of("url", result.url()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    @PostMapping("/markdown")
    public ResponseEntity<?> uploadMarkdown(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file provided"));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".md") && !filename.endsWith(".markdown"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Only .md files are allowed"));
        }

        try {
            String content = new String(file.getBytes());
            return ResponseEntity.ok(Map.of("content", content, "filename", filename));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Could not read file"));
        }
    }
}
