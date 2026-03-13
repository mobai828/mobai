package com.example.blog.service.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 本地文件存储实现
 */
@Component
public class LocalStorageProvider implements StorageProvider {

    private static final Logger logger = LoggerFactory.getLogger(LocalStorageProvider.class);

    @Value("${storage.location:./uploads}")
    private String storageLocation;

    @Override
    public String upload(InputStream inputStream, String fileName, String contentType) {
        try {
            Path uploadPath = Paths.get(storageLocation);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            
            logger.info("文件上传成功: {}", filePath);
            return fileName;
        } catch (IOException e) {
            logger.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public void delete(String filePath) {
        try {
            Path path = Paths.get(storageLocation, filePath);
            Files.deleteIfExists(path);
            logger.info("文件删除成功: {}", path);
        } catch (IOException e) {
            logger.error("文件删除失败", e);
        }
    }

    @Override
    public String getAccessUrl(String filePath) {
        return "/uploads/" + filePath;
    }
}
