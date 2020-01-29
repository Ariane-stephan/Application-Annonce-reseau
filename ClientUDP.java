import javax.crypto.Cipher;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import java.util.Base64;


public class ClientUDP extends Thread {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    private Socket socket;
    private int port;
    private byte[] privateKey;

    public ClientUDP(Socket s, byte[] privateKey){
        socket = s;
        port = s.getLocalPort();
        this.privateKey = privateKey;
    }

    @Override
    public void run() {
        DatagramSocket serverSocket = null;
        try {
            serverSocket = new DatagramSocket( port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        byte[] receiveData = new byte[1024];
        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                serverSocket.receive(receivePacket);

            } catch (IOException e) {
                e.printStackTrace();
            }
            String sentence = new String(receivePacket.getData(), 0, receivePacket.getLength());
            try {
                String msg = decrypt(sentence.getBytes()).toString();
                String[] messageTableau = msg.split("\\s+",2);
                System.out.println(ANSI_YELLOW + "Message de "+ messageTableau[0]+ ": " + messageTableau[1]+ ANSI_RESET);

            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

    public String decrypt(byte[] inputData) throws Exception {

        PrivateKey key = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKey));

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] encryptedBytes = Base64.getDecoder().decode(inputData);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        return new String(decryptedBytes);
    }
}