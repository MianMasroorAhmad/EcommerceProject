package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    @Override
    public String uploadImage(String path, MultipartFile file){
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new APIException("Invalid file name");
        }

        String randomUUID = UUID.randomUUID().toString();
        String uniqueFileName = randomUUID.concat(originalFilename.substring(originalFilename.lastIndexOf('.')));
        String filePath = path + File.separator + uniqueFileName;

        File folder = new File(path);
        if (!folder.exists() && !folder.mkdirs()) {
            throw new APIException("Failed to create directory: " + path);
        }

        try{
            Files.copy(file.getInputStream(), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
        }catch (IOException e){
            throw new APIException("Failed to upload image: " + e.getMessage());
        }
        return uniqueFileName;
    }
}
