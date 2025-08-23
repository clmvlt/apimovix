package bzh.stack.apimovix.service.update;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FileService {

    @Value("${app.upload.dir:/home/user/spring_boot_test/uploads}")
    private String uploadDir;

    public String saveApkFile(byte[] apkBytes, String version) {
        String path = "apk" + File.separator + version.replace(".", File.separator);

        String fullPath = uploadDir + File.separator + path;
        File directory = new File(fullPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = "apk_" + version + "_" + UUID.randomUUID().toString() + ".apk";
        String filePath = fullPath + File.separator + fileName;

        try {
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(apkBytes);
            }
            return path + File.separator + fileName;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean deleteFile(String filePath) {
        File file = new File(uploadDir + File.separator + filePath);
        if (file.exists() && file.delete()) {
            File parentDir = file.getParentFile();
            if (parentDir != null && parentDir.isDirectory() && parentDir.list().length == 0) {
                parentDir.delete();
            }
            return true;
        }
        return false;
    }

    public File findFile(String filePath) {
        String fullPath = uploadDir + File.separator + filePath;
        File file = new File(fullPath);
        return file.exists() ? file : null;
    }
}
