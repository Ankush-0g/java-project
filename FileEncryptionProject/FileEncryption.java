import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileEncryption {

    private static final int KEY = 5;

    public static void main(String[] args) {

        String inputFile = "inpt.txt";
        String encryptedFile = "encrypted.dat";
        String decryptedFile = "decrypted.txt";

        try {
            encryptFile(inputFile, encryptedFile);
            System.out.println("Encryption Successful!");

            decryptFile(encryptedFile, decryptedFile);
            System.out.println("Decryption Successful!");

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void encryptFile(String input, String output) throws IOException {

        FileInputStream fis = new FileInputStream(input);
        FileOutputStream fos = new FileOutputStream(output);

        int data;

        while ((data = fis.read()) != -1) {
            fos.write(data ^ KEY);
        }

        fis.close();
        fos.close();
    }

    static void decryptFile(String input, String output) throws IOException {

        FileInputStream fis = new FileInputStream(input);
        FileOutputStream fos = new FileOutputStream(output);

        int data;

        while ((data = fis.read()) != -1) {
            fos.write(data ^ KEY);
        }

        fis.close();
        fos.close();
    }
}