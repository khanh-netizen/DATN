package com.foxstyle.api.controller;

import com.foxstyle.api.dto.response.ApiResponse;
import com.foxstyle.api.entity.User;
import com.foxstyle.api.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LanguageController {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @GetMapping("/language/{code}")
    public ResponseEntity<Map<String, String>> getLanguageDictionary(@PathVariable String code) {
        try {
            String path = "lang/" + code.toLowerCase() + ".json";
            ClassPathResource resource = new ClassPathResource(path);
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            try (InputStream is = resource.getInputStream()) {
                Map<String, String> dictionary = objectMapper.readValue(is, new TypeReference<Map<String, String>>() {});
                return ResponseEntity.ok(dictionary);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/language/save")
    public ResponseEntity<ApiResponse<Void>> saveLanguage(@RequestBody Map<String, String> body, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        String language = body.get("language");
        if (language == null || language.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Ngôn ngữ không hợp lệ"));
        }
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setLanguage(language.trim());
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Lưu ngôn ngữ thành công", null));
    }

    @PostMapping("/theme/save")
    public ResponseEntity<ApiResponse<Void>> saveTheme(@RequestBody Map<String, String> body, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        String theme = body.get("theme");
        if (theme == null || theme.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Theme không hợp lệ"));
        }
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setTheme(theme.trim());
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Lưu theme thành công", null));
    }
}
