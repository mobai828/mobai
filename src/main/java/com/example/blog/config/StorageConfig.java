package com.example.blog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "storage")
public class StorageConfig {
    private String location = "./uploads";
    private String cloudProvider = "local";
    private String cloudAccessKey;
    private String cloudSecretKey;
    private String cloudBucketName;
    private String cloudEndpoint;

    // Getters and setters
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCloudProvider() {
        return cloudProvider;
    }

    public void setCloudProvider(String cloudProvider) {
        this.cloudProvider = cloudProvider;
    }

    public String getCloudAccessKey() {
        return cloudAccessKey;
    }

    public void setCloudAccessKey(String cloudAccessKey) {
        this.cloudAccessKey = cloudAccessKey;
    }

    public String getCloudSecretKey() {
        return cloudSecretKey;
    }

    public void setCloudSecretKey(String cloudSecretKey) {
        this.cloudSecretKey = cloudSecretKey;
    }

    public String getCloudBucketName() {
        return cloudBucketName;
    }

    public void setCloudBucketName(String cloudBucketName) {
        this.cloudBucketName = cloudBucketName;
    }

    public String getCloudEndpoint() {
        return cloudEndpoint;
    }

    public void setCloudEndpoint(String cloudEndpoint) {
        this.cloudEndpoint = cloudEndpoint;
    }
}