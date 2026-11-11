package studio.solocamera;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/** Authenticated, ordered encrypted packets. A fresh server challenge derives a connection key. */
public final class SecureChannel implements Closeable {
    public static final int MAX_PACKET = 2_000_000;
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final SecretKeySpec key;
    private final int sendDirection, receiveDirection;
    private long sent, received;

    public static String newSecret() {
        byte[] bytes = new byte[16]; new SecureRandom().nextBytes(bytes);
        StringBuilder s = new StringBuilder(); for (byte b : bytes) s.append(String.format("%02x", b & 255));
        return s.toString();
    }
    private SecureChannel(Socket socket, String secret, byte[] challenge, boolean server) throws Exception {
        if (!secret.matches("[a-fA-F0-9]{32}")) throw new IOException("Pairing key must be 32 hexadecimal characters");
        this.socket = socket;
        in = new DataInputStream(socket.getInputStream()); out = new DataOutputStream(socket.getOutputStream());
        MessageDigest hash = MessageDigest.getInstance("SHA-256");
        hash.update("SoloCamera-v1".getBytes(StandardCharsets.UTF_8));
        byte[] secretBytes = new byte[16];
        for (int i=0;i<16;i++) secretBytes[i]=(byte)Integer.parseInt(secret.substring(i*2,i*2+2),16);
        hash.update(secretBytes); hash.update(challenge);
        key = new SecretKeySpec(Arrays.copyOf(hash.digest(),16), "AES");
        sendDirection = server ? 1 : 2; receiveDirection = server ? 2 : 1;
    }
    public static SecureChannel server(Socket socket, String secret) throws Exception {
        socket.setSoTimeout(10000);
        byte[] challenge = new byte[32]; new SecureRandom().nextBytes(challenge);
        socket.getOutputStream().write(challenge); socket.getOutputStream().flush();
        SecureChannel c = new SecureChannel(socket, secret, challenge, true);
        if (!Arrays.equals(c.receive(), "HELLO-v1".getBytes(StandardCharsets.UTF_8))) throw new IOException("Invalid handshake");
        return c;
    }
    public static SecureChannel client(Socket socket, String secret) throws Exception {
        socket.setSoTimeout(10000);
        byte[] challenge = new byte[32]; new DataInputStream(socket.getInputStream()).readFully(challenge);
        SecureChannel c = new SecureChannel(socket, secret, challenge, false);
        c.send("HELLO-v1".getBytes(StandardCharsets.UTF_8)); return c;
    }
    private byte[] nonce(int direction, long sequence) {
        return ByteBuffer.allocate(12).putInt(direction).putLong(sequence).array();
    }
    public synchronized void send(byte[] data) throws Exception {
        if (data.length > MAX_PACKET-16) throw new IOException("Packet too large");
        Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE,key,new GCMParameterSpec(128,nonce(sendDirection,sent)));
        byte[] packet=cipher.doFinal(data);
        byte[] framed=new byte[packet.length+4];ByteBuffer.wrap(framed).putInt(packet.length).put(packet);out.write(framed);out.flush();sent++;
    }
    public byte[] receive() throws Exception {
        int length=in.readInt();
        if(length<16 || length>MAX_PACKET) throw new IOException("Invalid packet length");
        byte[] packet=new byte[length]; in.readFully(packet);
        Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE,key,new GCMParameterSpec(128,nonce(receiveDirection,received)));
        byte[] data=cipher.doFinal(packet); received++; return data;
    }
    public void ready() throws IOException { socket.setSoTimeout(15000); }
    @Override public void close() throws IOException { socket.close(); }
}

