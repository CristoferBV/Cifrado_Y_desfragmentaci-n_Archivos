package Funcionamiento;

import java.io.*;
import java.net.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Cliente {
    private static final String DIRECCION_IP = "192.168.0.4";
    private static final int PUERTO_SERVIDOR = 8000;
    private static final String RUTA_ARCHIVO = "C:/Users/Cristofer/Downloads/"
            + "UNA/Carreras UNA/Ingeniería en Sistemas/Tercer Año"
            + "/Sistemas Operativos/Tareas/Tarea 3/"
            + "Cifrado_Fragmentacion_Archivos/FragmentosUnidos.txt";
    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final String ENCRYPTION_KEY = "MiClaveSecreta123456@01K";

    public static void main(String[] args) {
        try {
            // Conexión con el servidor principal
            Socket socket = new Socket(DIRECCION_IP, PUERTO_SERVIDOR);

            // Selección del archivo a enviar
            File archivoEnviar = elegirArchivo();
            if (archivoEnviar == null) {
                System.out.println("No se seleccionó ningún archivo.");
                return;
            }

            byte[] bytesArchivoCifrado = cifrarArchivo(archivoEnviar);

            
            OutputStream outputStream = socket.getOutputStream();
            DataOutputStream dataOutputStream = 
                    new DataOutputStream(outputStream);
            dataOutputStream.writeInt(bytesArchivoCifrado.length);
            dataOutputStream.write(bytesArchivoCifrado, 0, 
                    bytesArchivoCifrado.length);
            dataOutputStream.flush();
            System.out.println("Archivo cifrado enviado al servidor "
                    + "principal.");

            
            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            String respuestaServidor = dataInputStream.readUTF();
            System.out.println("Respuesta del servidor principal: " 
                    + respuestaServidor);

            
            if (respuestaServidor.equals("Fragmentos distribuidos "
                    + "correctamente. Puedes solicitar nuevamente el "
                    + "archivo si lo deseas.")) {
                
                System.out.println("¿Desea solicitar el "
                        + "archivo 'FragmentosUnidos.txt'? (S/N)");
                BufferedReader br = 
                        new BufferedReader(new InputStreamReader(System.in));
                String confirmacion = br.readLine();

                if (confirmacion.equalsIgnoreCase("S")) {
                    
                    dataOutputStream.writeUTF("Solicitar archivo");
                    dataOutputStream.flush();
                    System.out.println("Solicitud de "
                            + "archivo enviada al servidor principal.");

                    int fragmentosUnidosSize = dataInputStream.readInt();
                    byte[] fragmentosUnidos = new byte[fragmentosUnidosSize];
                    dataInputStream.readFully(fragmentosUnidos, 0, 
                            fragmentosUnidosSize);       
                    
                    guardarArchivo(fragmentosUnidos, RUTA_ARCHIVO);
                    System.out.println("Archivo 'FragmentosUnidos.txt' "
                            + "recibido y guardado correctamente.");
                } else {
                    System.out.println("No se ha solicitado el archivo "
                            + "'FragmentosUnidos.txt'.");
                }
            }

            // Cerrar la conexión
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static File elegirArchivo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar archivo");
        FileNameExtensionFilter filter = 
                new FileNameExtensionFilter("Archivos de texto", "txt");
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

    private static void guardarArchivo(byte[] data, 
            String filePath) throws IOException {
        try (FileOutputStream fileOutputStream = 
                new FileOutputStream(filePath)) {
            fileOutputStream.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] cifrarArchivo(File file) throws IOException {
        byte[] fileBytes = getFileBytes(file);
        try {
            SecretKeySpec secretKey = 
                    new SecretKeySpec(ENCRYPTION_KEY.getBytes(), 
                            ENCRYPTION_ALGORITHM);
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(fileBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
