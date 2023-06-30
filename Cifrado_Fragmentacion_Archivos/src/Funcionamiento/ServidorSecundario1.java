package Funcionamiento;

import java.io.*;
import java.net.*;

public class ServidorSecundario1 {
    private static final int SERVER_PORT = 8200; // Puerto para la conexión con el servidor secundario 1
    private static final String FRAGMENT_FILE_PATH = "C:/Users/Cristofer/Downloads/UNA/Carreras UNA/Ingeniería en Sistemas/Tercer Año/Sistemas Operativos/Tareas/Tarea 3/Cifrado_Fragmentacion_Archivos/fragmento1.txt"; // Ruta del archivo "fragmento1.txt"

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);

            System.out.println("Servidor secundario 1 esperando conexión...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Cliente conectado.");

                // Recibir fragmento del archivo del cliente
                InputStream inputStream = socket.getInputStream();
                DataInputStream dataInputStream = new DataInputStream(inputStream);
                int fragmentLength = dataInputStream.readInt();
                byte[] fragment = new byte[fragmentLength];
                dataInputStream.readFully(fragment, 0, fragmentLength);

                // Guardar fragmento en un archivo
                saveFragmentToFile(fragment, FRAGMENT_FILE_PATH);

                System.out.println("Fragmento guardado en: " + FRAGMENT_FILE_PATH);

                socket.close();
                serverSocket.close();
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveFragmentToFile(byte[] fragment, String filePath) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
            fileOutputStream.write(fragment);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
