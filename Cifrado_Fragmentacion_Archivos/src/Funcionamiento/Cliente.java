package Funcionamiento;

import java.io.*;
import java.net.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Cliente {
    private static final String SERVER_HOST = "192.168.0.4";
    private static final int SERVER_PORT = 8000;
    private static final String FILE_PATH = "C:/Users/Cristofer/Downloads/UNA/Carreras UNA/Ingeniería en Sistemas/Tercer Año/Sistemas Operativos/Tareas/Tarea 3/Cifrado_Fragmentacion_Archivos/FragmentosUnidos.txt";
    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final String ENCRYPTION_KEY = "MiClaveSecreta123456@01K"; // Clave de cifrado predefinida con longitud válida (16 bytes)

    public static void main(String[] args) {
        try {
            // Conexión con el servidor principal
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);

            // Selección del archivo a enviar
            File fileToSend = chooseFile();
            if (fileToSend == null) {
                System.out.println("No se seleccionó ningún archivo.");
                return;
            }

            // Cifrado del archivo seleccionado
            byte[] encryptedFileBytes = encryptFile(fileToSend);

            // Envío del archivo cifrado al servidor principal
            OutputStream outputStream = socket.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeInt(encryptedFileBytes.length);
            dataOutputStream.write(encryptedFileBytes, 0, encryptedFileBytes.length);
            dataOutputStream.flush();
            System.out.println("Archivo cifrado enviado al servidor principal.");

            // Recepción de la respuesta del servidor principal
            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            String serverResponse = dataInputStream.readUTF();
            System.out.println("Respuesta del servidor principal: " + serverResponse);

            // Verificar si la respuesta es correcta y solicitar el archivo "FragmentosUnidos.txt"
            if (serverResponse.equals("Fragmentos distribuidos correctamente. Puedes solicitar nuevamente el archivo si lo deseas.")) {
                // Solicitar confirmación por consola
                System.out.println("¿Desea solicitar el archivo 'FragmentosUnidos.txt'? (S/N)");
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String confirmation = br.readLine();

                if (confirmation.equalsIgnoreCase("S")) {
                    // Enviar la solicitud al servidor
                    dataOutputStream.writeUTF("Solicitar archivo");
                    dataOutputStream.flush();
                    System.out.println("Solicitud de archivo enviada al servidor principal.");

                    // Recibir el tamaño del archivo "FragmentosUnidos.txt"
                    int fragmentosUnidosSize = dataInputStream.readInt();
                    byte[] fragmentosUnidos = new byte[fragmentosUnidosSize];
                    dataInputStream.readFully(fragmentosUnidos, 0, fragmentosUnidosSize);
                    //System.out.println("Tamaño del fragmento:" + fragmentosUnidosSize);

                    // Guardar el archivo "FragmentosUnidos.txt" en el disco
                    saveFile(fragmentosUnidos, FILE_PATH);
                    System.out.println("Archivo 'FragmentosUnidos.txt' recibido y guardado correctamente.");
                } else {
                    System.out.println("No se ha solicitado el archivo 'FragmentosUnidos.txt'.");
                }
            }

            // Cerrar la conexión
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static File chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar archivo");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivos de texto", "txt");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }

        return null;
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

    private static byte[] encryptFile(File file) throws IOException {
        byte[] fileBytes = getFileBytes(file);
        try {
            SecretKeySpec secretKey = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), ENCRYPTION_ALGORITHM);
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(fileBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
