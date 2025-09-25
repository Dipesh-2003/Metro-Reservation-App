// Create New File: src/main/java/com/aurionpro/app/service/implementation/FileUploadServiceImpl.java

package com.aurionpro.app.service.implementation;

import com.aurionpro.app.service.FileUploadService;
import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        // Upload the file to Cloudinary and get the result map
        Map<?, ?> uploadResult = cloudinary.uploader().upload(
            file.getBytes(),
            Map.of("public_id", UUID.randomUUID().toString())
        );

        // Extract the secure URL from the result map
        return (String) uploadResult.get("secure_url");
    }
}