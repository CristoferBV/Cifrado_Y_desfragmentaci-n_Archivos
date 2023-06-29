package Funcionamiento;

import java.io.*;
import java.net.*;

public class ServidorSecundario3 {
    private static final int SERVER_PORT = 8400;
    private static final String FILE_PATH = "C:/Users/Cristofer/Downloads/UNA/Carreras UNA/Ingeniería en Sistemas/Tercer Año/Sistemas Operativos/Tareas/Tarea 3/Cifrado_Fragmentacion_Archivos/fragmento3.txt";

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);

            System.out.println("Esperando conexión del ServidorPrincipal...");
            Socket serverPrincipalSocket = serverSocket.accept();
            System.out.println("ServidorPrincipal conectado.");

            // Recibir fragmento del ServidorPrincipal
            InputStream inputStream = serverPrincipalSocket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            int fragmentLength = dataInputStream.readInt();
            byte[] fragment = new byte[fragmentLength];
            dataInputStream.readFully(fragment, 0, fragmentLength);

            // Guardar fragmento en el disco
            saveFragmentToFile(fragment, FILE_PATH);

            System.out.println("Fragmento recibido y guardado correctamente.");

            serverPrincipalSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveFragmentToFile(byte[] fragment, String filePath) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            fileOutputStream.write(fragment);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
