package Funcionamiento;

import java.io.*;
import java.net.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class ServidorPrincipal {
    private static final int PUERTO_SERVIDOR = 8000;
    private static final int PUERTO_SERVIDOR_SECUNDARIO1 = 8200; 
    private static final int PUERTO_SERVIDOR_SECUNDARIO2 = 8300; 
    private static final int PUERTO_SERVIDOR_SECUNDARIO3 = 8400; 
    private static final int TAMANNO_FRAGMENTO = 1024;

    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final String LLAVE_CIFRADA = "MiClaveSecreta123456@01K"; 

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PUERTO_SERVIDOR);

            System.out.println("Esperando conexión del cliente...");
            Socket clienteSocket = serverSocket.accept();
            System.out.println("Cliente conectado.");

            // Recibir archivo cifrado del cliente
            InputStream inputStream = clienteSocket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            int tamannoArchivoCifrado = dataInputStream.readInt();
            byte[] archivoCifrado = new byte[tamannoArchivoCifrado];
            dataInputStream.readFully(archivoCifrado, 0, tamannoArchivoCifrado);

            System.out.println("Tamaño del archivo cifrado: " + 
                    archivoCifrado.length + " bytes");

            // Descifrar archivo
            byte[] archivoDescifrado = descifrarArchivo(archivoCifrado);
            System.out.println("Tamaño del archivo descifrado: " + 
                    archivoDescifrado.length + " bytes");

            byte[][] fragmentoArchivo = desfragmentarArchivo(archivoDescifrado);
            System.out.println("Número de fragmentos: " + 
                    fragmentoArchivo.length);

            enviarFragServidor(fragmentoArchivo[0], 
                    "192.168.0.4", PUERTO_SERVIDOR_SECUNDARIO1);
            enviarFragServidor(fragmentoArchivo[1], 
                    "192.168.0.4", PUERTO_SERVIDOR_SECUNDARIO2);
            enviarFragServidor(fragmentoArchivo[2], 
                    "192.168.0.4", PUERTO_SERVIDOR_SECUNDARIO3);

            
            OutputStream additionalOutputStream =
                    clienteSocket.getOutputStream();
            DataOutputStream additionalDataOutputStream =
                    new DataOutputStream(additionalOutputStream);
            InputStream additionalInputStream = clienteSocket.getInputStream();
            DataInputStream additionalDataInputStream =
                    new DataInputStream(additionalInputStream);

            
            additionalDataOutputStream.writeUTF("Fragmentos distribuidos "
                    + "correctamente. Puedes solicitar nuevamente el "
                    + "archivo si lo deseas.");
            additionalDataOutputStream.flush();

           
            String solicitudCliente = additionalDataInputStream.readUTF();
            if (solicitudCliente.equals("Solicitar archivo")) {
                
                byte[] archivoCombinado = unirFragmentos(fragmentoArchivo);

                enviarArchCliente(archivoCombinado, additionalDataOutputStream);
            } else {
                System.out.println("Pasó un error");
            }

            clienteSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[][] desfragmentarArchivo(byte[] archivoDescifrado) {
        int numfragmentos = 3; 
        byte[][] archivoFragmento = new byte[numfragmentos][];
        int tamannoFrafmento = (int) Math.ceil((double) 
                archivoDescifrado.length / numfragmentos);
        int offset = 0;

        for (int i = 0; i < numfragmentos; i++) {
            int longFragmento = Math.min(tamannoFrafmento, 
                    archivoDescifrado.length - offset);
            archivoFragmento[i] = new byte[longFragmento];
            System.arraycopy(archivoDescifrado, offset, 
                    archivoFragmento[i], 0, longFragmento);
            offset += longFragmento;
        }

        return archivoFragmento;
    }

    private static void enviarFragServidor(byte[] fragmento, String 
            nombreServidor, int puertoServidor) throws Exception {
        // Establecer conexión con el servidor secundario
        Socket socket = new Socket(nombreServidor, puertoServidor);

        // Enviar fragmento al servidor secundario
        OutputStream outputStream = socket.getOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.writeInt(fragmento.length);
        dataOutputStream.write(fragmento, 0, fragmento.length);
        dataOutputStream.flush();

        socket.close();
    }

    private static byte[] unirFragmentos(byte[][] fragments) {
        int totalLength = 0;
        for (byte[] fragment : fragments) {
            totalLength += fragment.length;
        }

        byte[] mergedFile = new byte[totalLength];
        int offset = 0;

        for (byte[] fragment : fragments) {
            System.arraycopy(fragment, 0, mergedFile, offset, fragment.length);
            offset += fragment.length;
        }

        return mergedFile;
    }

    private static byte[] descifrarArchivo(byte[] 
            encryptedBytes) throws IOException {
        try {
            SecretKeySpec secretKey = new 
        SecretKeySpec(LLAVE_CIFRADA.getBytes(), ENCRYPTION_ALGORITHM);
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void enviarArchCliente(byte[] fileBytes, 
            DataOutputStream dataOutputStream) throws IOException {
        
        FileOutputStream fileOutputStream = 
                new FileOutputStream("FragmentosUnidos.txt");
        fileOutputStream.write(fileBytes);
        fileOutputStream.close();

        File file = new File("FragmentosUnidos.txt");
        FileInputStream fileInputStream = new FileInputStream(file);

        // Enviar el archivo al cliente
        dataOutputStream.writeInt((int) file.length());
        byte[] buffer = new byte[TAMANNO_FRAGMENTO];
        int bytesRead;
        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            dataOutputStream.write(buffer, 0, bytesRead);
        }
        dataOutputStream.flush();

        fileInputStream.close();

        // Eliminar el archivo temporal
        file.delete();
    }
}
