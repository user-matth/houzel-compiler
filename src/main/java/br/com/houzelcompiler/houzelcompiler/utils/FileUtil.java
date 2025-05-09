package br.com.houzelcompiler.houzelcompiler.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;

public class FileUtil {

    public static void createDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public static File convertMultipartFileToFile(MultipartFile multipartFile, String uploadDir) throws Exception {
        String fileName = multipartFile.getOriginalFilename();
        File convFile = new File(uploadDir + File.separator + fileName);
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(multipartFile.getBytes());
        }
        return convFile;
    }

    public static void deleteFile(File file) {
        if (file.exists()) {
            file.delete();
        }
    }
}
