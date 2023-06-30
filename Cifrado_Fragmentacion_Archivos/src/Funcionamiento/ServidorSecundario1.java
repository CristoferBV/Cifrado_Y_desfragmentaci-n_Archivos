package Funcionamiento;

import java.io.*;
import java.net.*;

public class ServidorSecundario1 {
    private static final int SERVER_PORT = 8200; 
    private static final String RUTA_FRAGMENTO = 
            "C:/Users/Cristofer/Downloads/UNA/Carreras UNA"
            + "/Ingeniería en Sistemas/Tercer Año/Sistemas Operativos"
            + "/Tareas/Tarea 3/Cifrado_Fragmentacion_Archivos/fragmento1.txt"; 

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);

            System.out.println("Servidor secundario 1 esperando conexión...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Cliente conectado.");

                // Recibe el fragmento del archivo del cliente
                InputStream inputStream = socket.getInputStream();
                DataInputStream dataInputStream = 
                        new DataInputStream(inputStream);
                int longfragmento = dataInputStream.readInt();
                byte[] fragmento = new byte[longfragmento];
                dataInputStream.readFully(fragmento, 0, longfragmento);

                
                guardarFragmentoArchivo(fragmento, RUTA_FRAGMENTO);

                System.out.println("Fragmento guardado en: " + RUTA_FRAGMENTO);

                socket.close();
                //serverSocket.close();
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void guardarFragmentoArchivo(byte[] fragment, 
            String filePath) {
        try (FileOutputStream fileOutputStream = 
                new FileOutputStream(filePath)) {
            fileOutputStream.write(fragment);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
