package com.chatapp.util;

import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class FileUtil {
    public static byte[] fileToBytes(File file) throws IOException {
        if (file.length() > 20 * 1024 * 1024) { // 20 MB limit
            throw new IOException("File size exceeds 20 MB");
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
        }
        return baos.toByteArray();
    }

    public static File chooseFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File");
        return fileChooser.showOpenDialog(stage);
    }

    public static byte[] imageToBytes(File imageFile) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(imageFile);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        return baos.toByteArray();
    }

    public static Image bytesToImage(byte[] bytes) {
        if (bytes == null) return null;
        return new Image(new ByteArrayInputStream(bytes));
    }

    public static void saveFile(byte[] fileData, String fileName) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(fileName);
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(fileData);
            }
        }
    }
}