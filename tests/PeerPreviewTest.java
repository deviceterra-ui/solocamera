package studio.solocamera;

import org.json.JSONObject;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Real encrypted sockets exercise the preview writer, without an Android UI. */
public final class PeerPreviewTest {
    public static void main(String[] args)throws Exception{
        CountDownLatch connected=new CountDownLatch(2),frames=new CountDownLatch(25);AtomicInteger received=new AtomicInteger();
        PeerLink.Listener listener=new PeerLink.Listener(){
            public void approval(String address,java.util.function.Consumer<Boolean> reply){reply.accept(true);}
            public void connected(){connected.countDown();}
            public void disconnected(String reason){}
            public void message(JSONObject message){}
            public void frame(byte[] data){if(data.length!=1024)throw new AssertionError("Corrupt preview payload");received.incrementAndGet();frames.countDown();}
        };
        try(PeerLink host=new PeerLink(listener);PeerLink monitor=new PeerLink(listener)){
            String key=SecureChannel.newSecret();host.host(key);Thread.sleep(150);monitor.join("127.0.0.1",key);
            if(!connected.await(5,TimeUnit.SECONDS))throw new AssertionError("Peer handshake failed");
            for(int i=0;i<50;i++){host.frame(new byte[1024]);Thread.sleep(20);}
            if(!frames.await(2,TimeUnit.SECONDS))throw new AssertionError("Preview writer still throttled: "+received.get());
            System.out.println("PASS preview wake-up transfers "+received.get()+" frames from 50 frames produced at 50 fps over encrypted sockets");
        }
    }
}
