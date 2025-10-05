package com.aurionpro.app.service.implementation;

import com.aurionpro.app.dto.CloudinarySignatureResponse; // <-- Add
import com.aurionpro.app.exception.InvalidOperationException; // <-- Add
import com.aurionpro.app.service.FileUploadService;
import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays; // <-- Add
import java.util.List; // <-- Add
import java.util.Map;
import java.util.TreeMap; // <-- Add
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private final Cloudinary cloudinary;
    
    //  allowed file types
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("image/jpeg", "image/png");
    //  max file size (2MB) 
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;


    @Override
    public String uploadFile(MultipartFile file) throws IOException {
       
        if (file.isEmpty()) {
            throw new InvalidOperationException("File cannot be empty.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidOperationException("File size cannot exceed 2 MB.");
        }
        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new InvalidOperationException("Invalid file type. Only PNG and JPEG are allowed.");
        }

        Map<?, ?> uploadResult = cloudinary.uploader().upload(
            file.getBytes(),
            Map.of("public_id", UUID.randomUUID().toString())
        );

        return (String) uploadResult.get("secure_url");
    }
    
    @Override
    public CloudinarySignatureResponse generateUploadSignature() {
        Map<String, Object> paramsToSign = new TreeMap<>(); 
        long timestamp = System.currentTimeMillis() / 1000L;
        paramsToSign.put("timestamp", timestamp);

        // Generate the signature using Cloudinary's API secret
        String signature = cloudinary.apiSignRequest(paramsToSign, cloudinary.config.apiSecret);

        // Return the signature and other necessary details to the client
        return new CloudinarySignatureResponse(
            signature,
            String.valueOf(timestamp),
            cloudinary.config.apiKey,
            cloudinary.config.cloudName
        );
    }
}