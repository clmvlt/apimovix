package bzh.stack.apimovix.service.picture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import bzh.stack.apimovix.enums.PictureENUM;
import bzh.stack.apimovix.model.Anomalie;
import bzh.stack.apimovix.model.Command;
import bzh.stack.apimovix.model.Pharmacy;
import bzh.stack.apimovix.model.PharmacyInfos;
import bzh.stack.apimovix.model.Picture.PharmacyInfosPicture;

@Service
public class PictureService {

    @Value("${app.upload.dir:D:/3_PROJET/Movix/_apimovix/uploads}")
    private String uploadDir;

    private String cleanBase64String(String base64Image) {
        if (base64Image.contains("base64,")) {
            return base64Image.split("base64,")[1];
        }
        return base64Image;
    }

    public String saveAnomalieImage(Anomalie anomalie, String base64Image) {
        String subDir = anomalie.getId().toString();
        return saveBase64Image(PictureENUM.Anomalie, base64Image, subDir, true);
    }

    public String savePharmacyImage(Pharmacy pharmacy, String base64Image) {
        String subDir = pharmacy.getCip();
        return saveBase64Image(PictureENUM.Pharmacy, base64Image, subDir, false);
    }

    public String saveCommandImage(Command command, String base64Image) {
        String subDir = command.getId().toString();
        return saveBase64Image(PictureENUM.Command, base64Image, subDir, true);
    }

    public String savePharmacyInfosImage(PharmacyInfos pharmacyInfos, String base64Image) {
        String subDir = pharmacyInfos.getPharmacy().getCip() + File.separator + pharmacyInfos.getId();
        return saveBase64Image(PictureENUM.PharmacyInfos, base64Image, subDir, false);
    }

    public String saveBase64Image(PictureENUM type, String base64Image, String subDirectory, boolean saveCreatedAt) {
        String path = type.toString().toLowerCase();

        if (saveCreatedAt) {
            LocalDate now = LocalDate.now();
            String year = String.valueOf(now.getYear());
            String month = String.format("%02d", now.getMonthValue());
            String day = String.format("%02d", now.getDayOfMonth());
            path = path + File.separator + year + File.separator + month + File.separator + day;
        }

        if (subDirectory != null) {
            path = path + File.separator + subDirectory;
        }

        String fullPath = uploadDir + File.separator + path;
        File directory = new File(fullPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = UUID.randomUUID().toString() + ".jpg";
        String filePath = fullPath + File.separator + fileName;

        try {
            String cleanedBase64 = cleanBase64String(base64Image);
            byte[] imageBytes = Base64.getDecoder().decode(cleanedBase64);
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(imageBytes);
            }
            return path + File.separator + fileName;
        } catch (Exception e) {
            return null;
        }
    }
    
    public boolean deleteImage(String filePath) {
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

    // public File findImageFile(String fileName, String subDirectory) {
    //     String fullPath = uploadDir + File.separator + subDirectory + File.separator + fileName;
    //     File file = new File(fullPath);
    //     return file.exists() ? file : null;
    // }

    // public String findImagesPath(String fileName, String subDirectory) {
    //     return uploadDir + File.separator + subDirectory + File.separator + fileName;
    // }

    public File findImageFile(String imagePath) {
        String fullPath = uploadDir + File.separator + imagePath;
        File file = new File(fullPath);
        return file.exists() ? file : null;
    }

    public String copyPharmacyInfosImageToPharmacyImage(PharmacyInfosPicture pharmacyInfosPicture, Pharmacy pharmacy) {
        try {
            // Chemin source de l'image PharmacyInfos
            String sourcePath = uploadDir + File.separator + pharmacyInfosPicture.getName();
            File sourceFile = new File(sourcePath);
            
            if (!sourceFile.exists()) {
                return null;
            }

            // Créer le répertoire de destination pour l'image Pharmacy
            String pharmacyImagePath = PictureENUM.Pharmacy.toString().toLowerCase() + File.separator + pharmacy.getCip();
            String fullPharmacyPath = uploadDir + File.separator + pharmacyImagePath;
            File pharmacyDirectory = new File(fullPharmacyPath);
            if (!pharmacyDirectory.exists()) {
                pharmacyDirectory.mkdirs();
            }

            // Générer un nouveau nom de fichier
            String newFileName = UUID.randomUUID().toString() + ".jpg";
            String destinationPath = fullPharmacyPath + File.separator + newFileName;
            File destinationFile = new File(destinationPath);

            // Copier le fichier
            Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            return pharmacyImagePath + File.separator + newFileName;
        } catch (IOException e) {
            return null;
        }
    }
} 