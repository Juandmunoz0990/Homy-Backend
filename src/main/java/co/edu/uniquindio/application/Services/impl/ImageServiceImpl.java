package co.edu.uniquindio.application.Services.impl;

import co.edu.uniquindio.application.Services.ImageService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ImageServiceImpl implements ImageService {

    private final Cloudinary cloudinary;

    private final boolean isConfigured;

    public ImageServiceImpl(
            @Value("${CLOUDINARY_CLOUD_NAME:${cloudinary.cloud_name:}}") String cloudName,
            @Value("${CLOUDINARY_API_KEY:${cloudinary.api_key:}}") String apiKey,
            @Value("${CLOUDINARY_API_SECRET:${cloudinary.api_secret:}}") String apiSecret) {
        
        Map<String, String> config = new HashMap<>();
        
        // Verificar si hay configuración válida (no vacía y no "demo")
        boolean hasCloudName = cloudName != null && !cloudName.isEmpty() && !cloudName.equals("demo");
        boolean hasApiKey = apiKey != null && !apiKey.isEmpty() && !apiKey.equals("demo");
        boolean hasApiSecret = apiSecret != null && !apiSecret.isEmpty() && !apiSecret.equals("demo");
        
        this.isConfigured = hasCloudName && hasApiKey && hasApiSecret;
        
        if (this.isConfigured) {
            config.put("cloud_name", cloudName);
            config.put("api_key", apiKey);
            config.put("api_secret", apiSecret);
        } else {
            // Usar valores demo solo para inicializar (no funcionarán)
            config.put("cloud_name", "demo");
            config.put("api_key", "demo");
            config.put("api_secret", "demo");
        }
        
        cloudinary = new Cloudinary(config);
    }

    @Override
    public Map upload(MultipartFile image) throws Exception {
        if (!isConfigured) {
            throw new IllegalStateException(
                "Cloudinary no está configurado. Por favor, configura las variables de entorno: " +
                "CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, CLOUDINARY_API_SECRET en Railway."
            );
        }
        
        File file = convert(image);
        return cloudinary.uploader().upload(file, ObjectUtils.asMap("folder", "homy"));
    }

    @Override
    public Map delete(String imageId) throws Exception {
        return cloudinary.uploader().destroy(imageId, ObjectUtils.emptyMap());
    }

    private File convert(MultipartFile image) throws IOException {
        File file = File.createTempFile(image.getOriginalFilename(), null);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(image.getBytes());
        fos.close();
        return file;
    }
}
