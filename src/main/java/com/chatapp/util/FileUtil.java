package com.chatapp.util;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

// this is our FileUtil class, a handy toolbox for working with files and images in our ChatApp
public class FileUtil {
    // this method converts a file into a byte array, useful for sending files over the network
    public static byte[] fileToBytes(File file) throws IOException {
        // check if the file is too big (over 15 MB); we set a limit to keep things manageable
        if (file.length() > 15 * 1024 * 1024) { // 15 MB limit
            // throw an error if the file's too large so the user knows what's up
            throw new IOException("File size exceeds 15 MB");
        }
        // create a stream to collect bytes as we read the file
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // use try-with-resources to automatically close the file input stream
        try (FileInputStream fis = new FileInputStream(file)) {
            // set up a small buffer (1KB) to read the file in chunks
            byte[] buffer = new byte[1024];
            // we'll store how many bytes we read each time
            int bytesRead;
            // keep reading chunks until we hit the end of the file (-1 means end)
            while ((bytesRead = fis.read(buffer)) != -1) {
                // write the chunk we just read into our byte array stream
                baos.write(buffer, 0, bytesRead);
            }
        }
        // convert the collected bytes into an array and return it
        return baos.toByteArray();
    }

    // this method opens a file chooser dialog so the user can pick a file
    public static File chooseFile(Stage stage) {
        // create a new file chooser dialog
        FileChooser fileChooser = new FileChooser();
        // set a friendly title for the dialog window
        fileChooser.setTitle("Select File");
        // show the dialog and return the file the user selects (or null if they cancel)
        return fileChooser.showOpenDialog(stage);
    }

    // this method turns an image file into a byte array, specifically as a PNG
    public static byte[] imageToBytes(File imageFile) throws IOException {
        // read the image file into a BufferedImage object for processing
        BufferedImage bufferedImage = ImageIO.read(imageFile);
        // create a stream to collect the image bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // write the image as a PNG to our byte stream
        ImageIO.write(bufferedImage, "png", baos);
        // return the bytes of the PNG image
        return baos.toByteArray();
    }

    // this method converts a byte array back into a JavaFX Image for display
    public static Image bytesToImage(byte[] bytes) {
        // if the byte array is null, return null (no image to display)
        if (bytes == null) return null;
        // create an input stream from the bytes and use it to make a JavaFX Image
        return new Image(new ByteArrayInputStream(bytes));
    }

    // this method lets the user save a file from a byte array, like downloading an attachment
    public static void saveFile(byte[] fileData, String fileName) throws IOException {
        // create a file chooser dialog for saving the file
        FileChooser fileChooser = new FileChooser();
        // suggest the original file name for convenience
        fileChooser.setInitialFileName(fileName);
        // show the save dialog and get the file the user wants to save to
        File file = fileChooser.showSaveDialog(null);
        // if the user picked a file (didn't cancel)...
        if (file != null) {
            // use try-with-resources to ensure the output stream closes properly
            try (FileOutputStream fos = new FileOutputStream(file)) {
                // write the byte array to the chosen file
                fos.write(fileData);
            }
        }
    }
}