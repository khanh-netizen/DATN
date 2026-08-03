package com.foxstyle.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {
    private static final Set<String> FOLDERS = Set.of(
            "image_san_pham", "image_banner", "image_bai_viet", "image_quan_tri", "video");
    private static final Set<String> IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif", "image/svg+xml");
    private static final Set<String> VIDEO_TYPES = Set.of("video/mp4", "video/webm", "video/quicktime");

    @Value("${app.media.root:../../image}")
    private String mediaRoot;

    @Value("${app.media.public-url:http://localhost:8080/media}")
    private String publicUrl;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public Map<String, String> upload(@RequestParam("file") MultipartFile file,
                                      @RequestParam("folder") String folder) throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("Tệp tải lên đang trống");
        if (!FOLDERS.contains(folder)) throw new IllegalArgumentException("Thư mục media không hợp lệ");

        String contentType = file.getContentType() == null ? "" : file.getContentType();
        boolean isVideo = folder.equals("video");
        if (isVideo ? !VIDEO_TYPES.contains(contentType) : !IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(isVideo ? "Chỉ chấp nhận tệp video" : "Chỉ chấp nhận tệp hình ảnh");
        }
        long maxSize = isVideo ? 100L * 1024 * 1024 : 10L * 1024 * 1024;
        if (file.getSize() > maxSize) throw new IllegalArgumentException("Tệp tải lên vượt quá dung lượng cho phép");

        String original = file.getOriginalFilename() == null ? "media" : file.getOriginalFilename();
        String extension = original.lastIndexOf('.') >= 0 ? original.substring(original.lastIndexOf('.')).toLowerCase() : "";
        String baseName = original.lastIndexOf('.') >= 0 ? original.substring(0, original.lastIndexOf('.')) : original;
        baseName = Normalizer.normalize(baseName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replaceAll("[^a-zA-Z0-9_-]+", "-")
                .replaceAll("^-+|-+$", "").toLowerCase();
        if (baseName.isBlank()) baseName = "media";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String filename = baseName + "-" + timestamp + "-" + UUID.randomUUID().toString().substring(0, 8) + extension;

        Path root = Path.of(mediaRoot).toAbsolutePath().normalize();
        Path directory = root.resolve(folder).normalize();
        if (!directory.startsWith(root)) throw new IllegalArgumentException("Đường dẫn lưu media không hợp lệ");
        Files.createDirectories(directory);
        Files.copy(file.getInputStream(), directory.resolve(filename), StandardCopyOption.REPLACE_EXISTING);

        return Map.of("url", publicUrl.replaceAll("/$", "") + "/" + folder + "/" + filename,
                "filename", filename, "folder", folder);
    }
}
