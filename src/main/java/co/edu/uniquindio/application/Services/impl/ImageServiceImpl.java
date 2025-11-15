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

    public ImageServiceImpl(
            @Value("${cloudinary.cloud_name:}") String cloudName,
            @Value("${cloudinary.api_key:}") String apiKey,
            @Value("${cloudinary.api_secret:}") String apiSecret) {
        
        // Si no hay configuración, usar valores por defecto (deshabilitar uploads)
        Map<String, String> config = new HashMap<>();
        
        // Usar variables de entorno o valores por defecto
        String finalCloudName = cloudName != null && !cloudName.isEmpty() ? cloudName : "demo";
        String finalApiKey = apiKey != null && !apiKey.isEmpty() ? apiKey : "demo";
        String finalApiSecret = apiSecret != null && !apiSecret.isEmpty() ? apiSecret : "demo";
        
        config.put("cloud_name", finalCloudName);
        config.put("api_key", finalApiKey);
        config.put("api_secret", finalApiSecret);
        
        cloudinary = new Cloudinary(config);
    }

    @Override
    public Map upload(MultipartFile image) throws Exception {
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
