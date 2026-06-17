package com.eat.controller;

import com.eat.entity.FileResource;
import com.eat.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            FileResource resource = fileService.upload(file);
            return ResponseEntity.ok(Map.of(
                    "id", resource.getId(),
                    "url", resource.getUrl(),
                    "relativePath", resource.getRelativePath(),
                    "originalName", resource.getOriginalName(),
                    "contentType", resource.getContentType(),
                    "size", resource.getSize()
            ));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "上传失败: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
