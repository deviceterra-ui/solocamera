package studio.solocamera;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;
import javax.crypto.*;
import javax.crypto.spec.*;

/** Real socket tests; no Android runtime, mocks or external test framework required. */
public final class SecureChannelTest {
    private static final String KEY="00112233445566778899aabbccddeeff";
    interface Test {void run() throws Exception;}
    static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    static void test(String label,Test test) throws Exception {test.run();System.out.println("PASS "+label);}
    public static void main(String[] args) throws Exception {
        test("bidirectional encrypted round trip and ordered multi-packet delivery",()->{
            try(ServerSocket listener=new ServerSocket(0)){
                CompletableFuture<Void> server=CompletableFuture.runAsync(()->{
                    try(SecureChannel c=SecureChannel.server(listener.accept(),KEY)){
                        for(int i=0;i<20;i++){check(new String(c.receive(),StandardCharsets.UTF_8).equals("request-"+i),"Order changed");c.send(("reply-"+i).getBytes(StandardCharsets.UTF_8));}
                    }catch(Exception e){throw new CompletionException(e);}
                });
                try(SecureChannel c=SecureChannel.client(new Socket("127.0.0.1",listener.getLocalPort()),KEY)){
                    for(int i=0;i<20;i++){c.send(("request-"+i).getBytes(StandardCharsets.UTF_8));check(new String(c.receive(),StandardCharsets.UTF_8).equals("reply-"+i),"Reply mismatch");}
                }server.get(10,TimeUnit.SECONDS);
            }
        });
        test("wrong pairing key rejected",()->{
            try(ServerSocket listener=new ServerSocket(0)){
                CompletableFuture<Boolean> rejected=CompletableFuture.supplyAsync(()->{try(Socket s=listener.accept()){SecureChannel.server(s,KEY);return false;}catch(Exception expected){return true;}});
                try(SecureChannel ignored=SecureChannel.client(new Socket("127.0.0.1",listener.getLocalPort()),"ffeeddccbbaa99887766554433221100")){}
                check(rejected.get(10,TimeUnit.SECONDS),"Wrong key accepted");
            }
        });
        test("oversized packet rejected before allocation",()->{
            try(ServerSocket listener=new ServerSocket(0)){
                CompletableFuture<Boolean> rejected=CompletableFuture.supplyAsync(()->{try(Socket s=listener.accept()){SecureChannel.server(s,KEY);return false;}catch(Exception expected){return expected.getMessage()!=null&&expected.getMessage().contains("length");}});
                try(Socket s=new Socket("127.0.0.1",listener.getLocalPort())){byte[] challenge=new byte[32];new DataInputStream(s.getInputStream()).readFully(challenge);new DataOutputStream(s.getOutputStream()).writeInt(Integer.MAX_VALUE);}
                check(rejected.get(10,TimeUnit.SECONDS),"Oversized packet not rejected");
            }
        });
        test("tampered authenticated handshake rejected",()->{
            try(ServerSocket listener=new ServerSocket(0)){
                CompletableFuture<Boolean> rejected=CompletableFuture.supplyAsync(()->{try(Socket s=listener.accept()){SecureChannel.server(s,KEY);return false;}catch(Exception expected){return true;}});
                try(Socket s=new Socket("127.0.0.1",listener.getLocalPort())){byte[] challenge=new byte[32];new DataInputStream(s.getInputStream()).readFully(challenge);byte[] packet=hello(challenge);packet[0]^=1;DataOutputStream out=new DataOutputStream(s.getOutputStream());out.writeInt(packet.length);out.write(packet);out.flush();}
                check(rejected.get(10,TimeUnit.SECONDS),"Tampered packet accepted");
            }
        });
        test("replayed packet rejected by sequence nonce",()->{
            try(ServerSocket listener=new ServerSocket(0)){
                CompletableFuture<Boolean> rejected=CompletableFuture.supplyAsync(()->{try(SecureChannel c=SecureChannel.server(listener.accept(),KEY)){c.receive();return false;}catch(Exception expected){return true;}});
                try(Socket s=new Socket("127.0.0.1",listener.getLocalPort())){byte[] challenge=new byte[32];new DataInputStream(s.getInputStream()).readFully(challenge);byte[] packet=hello(challenge);DataOutputStream out=new DataOutputStream(s.getOutputStream());for(int i=0;i<2;i++){out.writeInt(packet.length);out.write(packet);}out.flush();}
                check(rejected.get(10,TimeUnit.SECONDS),"Replay accepted");
            }
        });
        test("generated pairing keys contain 128 bits and expected encoding",()->{Set<String> keys=new HashSet<>();for(int i=0;i<1000;i++){String k=SecureChannel.newSecret();check(k.matches("[a-f0-9]{32}"),"Invalid key");check(keys.add(k),"Duplicate random key");}});
    }
    private static byte[] hello(byte[] challenge) throws Exception {
        MessageDigest hash=MessageDigest.getInstance("SHA-256");hash.update("SoloCamera-v1".getBytes(StandardCharsets.UTF_8));byte[] secret=new byte[16];for(int i=0;i<16;i++)secret[i]=(byte)Integer.parseInt(KEY.substring(i*2,i*2+2),16);hash.update(secret);hash.update(challenge);
        Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(Arrays.copyOf(hash.digest(),16),"AES"),new GCMParameterSpec(128,ByteBuffer.allocate(12).putInt(2).putLong(0).array()));return c.doFinal("HELLO-v1".getBytes(StandardCharsets.UTF_8));
    }
}
