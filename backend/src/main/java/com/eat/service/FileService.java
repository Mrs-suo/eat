package com.eat.service;

import com.eat.entity.FileResource;
import com.eat.repository.FileResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private final FileResourceRepository fileResourceRepository;

    @Value("${app.upload.dir:./file-storage}")
    private String uploadDir;

    @Value("${app.upload.url-prefix:/files}")
    private String urlPrefix;

    public FileResource upload(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (file.getContentType() != null && !ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("仅支持 jpg、png、webp、gif 图片");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String newFilename = UUID.randomUUID().toString().replace("-", "") + ext;

        Path targetDir = Paths.get(uploadDir, datePath);
        Files.createDirectories(targetDir);

        Path targetFile = targetDir.resolve(newFilename);
        Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

        String relativePath = datePath + "/" + newFilename;
        String urlPath = urlPrefix + "/" + relativePath;
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(urlPath)
                .toUriString();

        FileResource resource = new FileResource();
        resource.setOriginalName(originalFilename);
        resource.setStoredName(newFilename);
        resource.setContentType(file.getContentType());
        resource.setSize(file.getSize());
        resource.setRelativePath(relativePath);
        resource.setUrl(url);
        return fileResourceRepository.save(resource);
    }

    public void delete(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }

        String relativePath = filePath;
        if (filePath.startsWith(urlPrefix)) {
            relativePath = filePath.substring(urlPrefix.length());
        }
        relativePath = relativePath.replaceFirst("^/+", "");

        Path file = Paths.get(uploadDir, relativePath);
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            // Deleting a file is a best-effort cleanup operation.
        }
    }
}
