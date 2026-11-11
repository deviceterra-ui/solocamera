package studio.solocamera;

import android.os.*;
import org.json.JSONObject;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.function.Consumer;

/** Separate port allows the camera-monitor demo to keep using its own pairing server. */
final class ScreenLivePeer implements AutoCloseable {
    static final int PORT=48732;
    final PeerLink peer;final String key;
    volatile String status="Waiting for Windows",approvalAddress;volatile Consumer<Boolean> approval;
    private volatile boolean closed;private volatile String authorizedDesktop;
    ScreenLivePeer(){byte[] random=new byte[16];new SecureRandom().nextBytes(random);StringBuilder text=new StringBuilder();for(byte b:random)text.append(String.format(java.util.Locale.US,"%02x",b&255));key=text.toString();peer=new PeerLink(new PeerLink.Listener(){public void approval(String address,Consumer<Boolean> answer){if(closed){answer.accept(false);return;}if(address.equals(authorizedDesktop)){answer.accept(true);return;}approvalAddress=address;approval=answer;status="Approve Windows on the Screen recording page";}public void connected(){approval=null;approvalAddress=null;status="Live to Windows · screen + selected audio";}public void disconnected(String reason){status=reason;approval=null;approvalAddress=null;}public void message(JSONObject m){}public void frame(byte[] jpeg){}});peer.host(key,PORT);}
    void answer(boolean allow){Consumer<Boolean> pending=approval;approval=null;approvalAddress=null;if(pending!=null)pending.accept(allow);}
    void register(String[] target){status="Connecting to Windows…";new Thread(()->{try(Socket socket=new Socket()){socket.connect(new InetSocketAddress(target[0],Integer.parseInt(target[1])),7000);try(SecureChannel channel=SecureChannel.client(socket,target[2])){socket.setSoTimeout(8000);if(!"APPROVED".equals(new String(channel.receive(),StandardCharsets.UTF_8)))throw new Exception("Windows rejected pairing");if(closed)return;authorizedDesktop=target[0];channel.send((key+":"+PORT).getBytes(StandardCharsets.UTF_8));if(!"REGISTERED".equals(new String(channel.receive(),StandardCharsets.UTF_8)))throw new Exception("Windows did not register");status="Windows found · waiting for connection";}}catch(Exception e){authorizedDesktop=null;status="Windows pairing failed. Check Wi-Fi/hotspot and scan a fresh QR code.";}},"screen-desktop-pairing").start();}
    @Override public void close(){closed=true;answer(false);peer.close();authorizedDesktop=null;}
}
