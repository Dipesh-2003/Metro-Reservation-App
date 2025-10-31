package com.aurionpro.app.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.app.dto.CloudinarySignatureResponse;

public interface FileUploadService {
    String uploadFile(MultipartFile file) throws IOException;
    
    CloudinarySignatureResponse generateUploadSignature();
}