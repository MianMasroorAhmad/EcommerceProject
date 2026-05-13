package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    @Override
    public String uploadImage(String path, MultipartFile file){
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename(), "File name cannot be null");
        if (originalFilename.isEmpty()) {
            throw new APIException("Invalid file name");
        }

        String uniqueFileName = UUID.randomUUID() + originalFilename.substring(originalFilename.lastIndexOf('.'));
        Path uploadDir = Paths.get(path);
        Path filePath = uploadDir.resolve(uniqueFileName);

        try {
            Files.createDirectories(uploadDir);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new APIException("Failed to upload image: " + e.getMessage());
        }
        return uniqueFileName;
    }
}
