package Funcionamiento;

import java.io.*;
import java.net.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServidorPrincipal {
    private static final int SERVER_PORT = 8000;
    private static final int SERVER_SECUNDARIO1_PORT = 8200; // Puerto para la conexión con el servidor secundario 1
    private static final int SERVER_SECUNDARIO2_PORT = 8300; // Puerto para la conexión con el servidor secundario 2
    private static final int SERVER_SECUNDARIO3_PORT = 8400; // Puerto para la conexión con el servidor secundario 3
    private static final int FRAGMENT_SIZE = 1024;

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);

            System.out.println("Esperando conexión del cliente...");
            Socket clientSocket = serverSocket.accept();
            System.out.println("Cliente conectado.");

            // Recibir archivo cifrado del cliente
            InputStream inputStream = clientSocket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            int encryptedFileSize = dataInputStream.readInt();
            byte[] encryptedFile = new byte[encryptedFileSize];
            dataInputStream.readFully(encryptedFile, 0, encryptedFileSize);

            System.out.println("Tamaño del archivo cifrado: " + encryptedFile.length + " bytes");

            // Desfragmentar archivo en fragmentos
            byte[][] fileFragments = defragmentFile(encryptedFile);
            System.out.println("Número de fragmentos: " + fileFragments.length);

            // Enviar fragmentos a los servidores secundarios
            sendFragmentToServer(fileFragments[0], "192.168.0.5", SERVER_SECUNDARIO1_PORT);
            sendFragmentToServer(fileFragments[1], "192.168.0.5", SERVER_SECUNDARIO2_PORT);
            sendFragmentToServer(fileFragments[2], "192.168.0.5", SERVER_SECUNDARIO3_PORT);

            // Nuevo flujo de salida y entrada para comunicación adicional con el cliente
            OutputStream additionalOutputStream = clientSocket.getOutputStream();
            DataOutputStream additionalDataOutputStream = new DataOutputStream(additionalOutputStream);
            InputStream additionalInputStream = clientSocket.getInputStream();
            DataInputStream additionalDataInputStream = new DataInputStream(additionalInputStream);

            // Notificar al cliente que los fragmentos se han distribuido correctamente
            additionalDataOutputStream.writeUTF("Fragmentos distribuidos correctamente. Puedes solicitar nuevamente el archivo si lo deseas.");
            additionalDataOutputStream.flush();

            clientSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    private static byte[][] defragmentFile(byte[] encryptedFile) {
        int numFragments = 3; // Número de fragmentos deseado (en este caso, 3)
        byte[][] fileFragments = new byte[numFragments][];
        int fragmentSize = (int) Math.ceil((double) encryptedFile.length / numFragments);
        int offset = 0;

        for (int i = 0; i < numFragments; i++) {
            int fragmentLength = Math.min(fragmentSize, encryptedFile.length - offset);
            fileFragments[i] = new byte[fragmentLength];
            System.arraycopy(encryptedFile, offset, fileFragments[i], 0, fragmentLength);
            offset += fragmentLength;
        }

        return fileFragments;
    }


    private static void sendFragmentToServer(byte[] fragment, String serverName, int serverPort) throws Exception {
        // Establecer conexión con el servidor secundario
        Socket socket = new Socket(serverName, serverPort);

        // Enviar fragmento al servidor secundario
        OutputStream outputStream = socket.getOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.writeInt(fragment.length);
        dataOutputStream.write(fragment, 0, fragment.length);
        dataOutputStream.flush();

        socket.close();
    }

    private static byte[] getFragmentFromServer(String serverName, int serverPort) throws Exception {
        // Establecer conexión con el servidor secundario
        Socket socket = new Socket(serverName, serverPort);

        // Recibir fragmento del servidor secundario
        InputStream inputStream = socket.getInputStream();
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        int fragmentLength = dataInputStream.readInt();
        byte[] fragment = new byte[fragmentLength];
        dataInputStream.readFully(fragment, 0, fragmentLength);

        socket.close();

        return fragment;
    }
    
    private static byte[] mergeFragments(byte[][] fragments) {
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
}
