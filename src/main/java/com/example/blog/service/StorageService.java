package com.example.blog.service;

import com.example.blog.config.StorageConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class StorageService {

    @Autowired
    private StorageConfig storageConfig;

    private Path fileStorageLocation;

    @PostConstruct
    public void init() {
        if ("local".equals(storageConfig.getCloudProvider())) {
            this.fileStorageLocation = Paths.get(storageConfig.getLocation())
                    .toAbsolutePath().normalize();
            
            try {
                Files.createDirectories(this.fileStorageLocation);
            } catch (Exception ex) {
                throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
            }
        }
    }

    public String storeFile(MultipartFile file) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        
        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + fileName);
            }
            
            // Generate unique file name
            String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
            
            // Copy file to the target location (Replacing existing file with the same name)
            if ("local".equals(storageConfig.getCloudProvider())) {
                Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
                
                return uniqueFileName;
            } else {
                // Handle cloud storage
                return storeFileToCloud(file, uniqueFileName);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    private String storeFileToCloud(MultipartFile file, String fileName) {
        // 这里需要根据具体的云服务商SDK来实现
        // 以下代码仅为示例框架
        
        try {
            // 以阿里云OSS为例
            if ("aliyun".equals(storageConfig.getCloudProvider())) {
                // 初始化OSS客户端
                // OSS ossClient = new OSSClientBuilder().build(storageConfig.getCloudEndpoint(), 
                //                                              storageConfig.getCloudAccessKey(), 
                //                                              storageConfig.getCloudSecretKey());
                
                // 上传文件
                // ossClient.putObject(storageConfig.getCloudBucketName(), fileName, file.getInputStream());
                
                // 关闭OSSClient
                // ossClient.shutdown();
                
                // 返回文件访问URL
                return "https://" + storageConfig.getCloudBucketName() + "." + storageConfig.getCloudEndpoint() + "/" + fileName;
            }
            // 可以添加其他云服务商的实现
            
            return fileName;
        } catch (Exception ex) {
            throw new RuntimeException("Could not store file " + fileName + " to cloud. Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            if ("local".equals(storageConfig.getCloudProvider())) {
                Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
                Resource resource = new UrlResource(filePath.toUri());
                if (resource.exists()) {
                    return resource;
                } else {
                    throw new RuntimeException("File not found " + fileName);
                }
            } else {
                // Handle cloud storage resource loading
                return loadFileFromCloud(fileName);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found " + fileName, ex);
        }
    }

    private Resource loadFileFromCloud(String fileName) {
        // 这里需要根据具体的云服务商SDK来实现
        // 以下代码仅为示例框架
        
        try {
            // 以阿里云OSS为例
            if ("aliyun".equals(storageConfig.getCloudProvider())) {
                // 初始化OSS客户端
                // OSS ossClient = new OSSClientBuilder().build(storageConfig.getCloudEndpoint(), 
                //                                              storageConfig.getCloudAccessKey(), 
                //                                              storageConfig.getCloudSecretKey());
                
                // 下载文件
                // OSSObject ossObject = ossClient.getObject(storageConfig.getCloudBucketName(), fileName);
                // InputStream inputStream = ossObject.getObjectContent();
                
                // 将文件流转换为Resource
                
                // 关闭OSSClient
                // ossClient.shutdown();
                
                // 返回Resource对象
            }
            // 可以添加其他云服务商的实现
            
            return null;
        } catch (Exception ex) {
            throw new RuntimeException("Could not load file " + fileName + " from cloud.", ex);
        }
    }
}