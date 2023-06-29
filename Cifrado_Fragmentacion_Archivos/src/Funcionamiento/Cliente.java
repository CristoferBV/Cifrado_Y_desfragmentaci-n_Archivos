package Funcionamiento;

import java.io.*;
import java.net.*;

/**
 *
 * author Cristofer
 */
public class Cliente {
    private static final String SERVER_HOST = "192.168.0.5";
    private static final int SERVER_PORT = 8000;

    public static void main(String[] args) {
        try {
            // Conexión con el servidor principal
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);

            // Archivo a enviar
            File fileToSend = new File("archivo.txt");
            byte[] fileBytes = getFileBytes(fileToSend);

            // Envío del archivo cifrado al servidor principal
            OutputStream outputStream = socket.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeInt(fileBytes.length);
            dataOutputStream.write(fileBytes, 0, fileBytes.length);
            dataOutputStream.flush();
            System.out.println("Archivo enviado al servidor principal.");

            // Recepción de la respuesta del servidor principal
            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            String serverResponse = dataInputStream.readUTF();
            System.out.println("Respuesta del servidor principal: " + serverResponse);

            // Cerrar la conexión
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] getFileBytes(File file) throws IOException {
        int fileLength = (int) file.length();
        byte[] fileBytes = new byte[fileLength];
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        bufferedInputStream.read(fileBytes, 0, fileLength);
        bufferedInputStream.close();
        return fileBytes;
    }
}
