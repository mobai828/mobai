package com.example.blog.service;

import com.example.blog.service.storage.LocalStorageProvider;
import com.example.blog.service.storage.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件存储服务
 */
@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Value("${storage.cloud-provider:local}")
    private String cloudProvider;

    @Autowired
    private LocalStorageProvider localStorageProvider;

    /**
     * 上传文件
     */
    public String upload(MultipartFile file, String subDir) throws IOException {
        // 验证文件
        validateFile(file);
        
        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String newFileName = subDir + "/" + UUID.randomUUID().toString() + extension;
        
        // 获取存储提供者
        StorageProvider provider = getStorageProvider();
        
        // 上传文件
        String filePath = provider.upload(file.getInputStream(), newFileName, file.getContentType());
        
        return provider.getAccessUrl(filePath);
    }

    /**
     * 验证文件
     */
    public void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("文件大小不能超过10MB");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new RuntimeException("不支持的文件类型，仅支持 JPG/PNG/GIF/WEBP");
        }
    }

    /**
     * 删除文件
     */
    public void delete(String fileUrl) {
        StorageProvider provider = getStorageProvider();
        provider.delete(fileUrl);
    }

    private StorageProvider getStorageProvider() {
        // 根据配置返回对应的存储提供者
        // 目前只实现本地存储，后续可扩展云存储
        return localStorageProvider;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return ".jpg";
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot) : ".jpg";
    }
}
