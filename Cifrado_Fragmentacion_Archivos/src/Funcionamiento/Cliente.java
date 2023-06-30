package Funcionamiento;

import java.io.*;
import java.net.*;

public class Cliente {
    private static final String SERVER_HOST = "192.168.0.4";
    private static final int SERVER_PORT = 8000;
    private static final String FILE_PATH = "C:/Users/Cristofer/Downloads/UNA/Carreras UNA/Ingeniería en Sistemas/Tercer Año/Sistemas Operativos/Tareas/Tarea 3/Cifrado_Fragmentacion_Archivos/FragmentosUnidos.txt";

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
            OutputStream outputStream2 = socket.getOutputStream();
            DataOutputStream dataOutputStream2 = new DataOutputStream(outputStream2);
            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            String serverResponse = dataInputStream.readUTF();
            System.out.println("Respuesta del servidor principal: " + serverResponse);

            // Verificar si la respuesta es correcta y solicitar el archivo "FragmentosUnidos.txt"
            if (serverResponse.equals("Fragmentos distribuidos correctamente. Puedes solicitar nuevamente el archivo si lo deseas.")) {
                // Solicitar el archivo "FragmentosUnidos.txt"
                dataOutputStream2.writeUTF("Solicitar archivo");
                dataOutputStream2.flush();
                System.out.println("Solicitud de archivo enviada al servidor principal.");

                // Recibir el tamaño del archivo "FragmentosUnidos.txt"
                int fragmentosUnidosSize = dataInputStream.readInt();
                byte[] fragmentosUnidos = new byte[fragmentosUnidosSize];
                dataInputStream.readFully(fragmentosUnidos, 0, fragmentosUnidosSize);
                //System.out.println("Tamaño del fragmento:" + fragmentosUnidosSize);

                // Guardar el archivo "FragmentosUnidos.txt" en el disco
                saveFile(fragmentosUnidos, FILE_PATH);
                System.out.println("Archivo 'FragmentosUnidos.txt' recibido y guardado correctamente.");
            }

            // Cerrar la conexión
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] getFileBytes(File file) throws IOException {
        byte[] fileBytes = null;
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            fileBytes = new byte[(int) file.length()];
            fileInputStream.read(fileBytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return fileBytes;
    }

    private static void saveFile(byte[] data, String filePath) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
            fileOutputStream.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
