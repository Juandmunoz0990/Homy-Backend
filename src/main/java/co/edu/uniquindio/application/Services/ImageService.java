package co.edu.uniquindio.application.Services;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    Map upload(MultipartFile image) throws Exception;
    Map delete(String imageId) throws Exception;
}

