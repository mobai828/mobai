package com.example.blog.service.storage;

import java.io.InputStream;

/**
 * 存储提供者接口（策略模式）
 */
public interface StorageProvider {
    
    /**
     * 上传文件
     */
    String upload(InputStream inputStream, String fileName, String contentType);
    
    /**
     * 删除文件
     */
    void delete(String filePath);
    
    /**
     * 获取文件访问URL
     */
    String getAccessUrl(String filePath);
}
