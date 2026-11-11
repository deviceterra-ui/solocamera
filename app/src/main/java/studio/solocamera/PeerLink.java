package studio.solocamera;

import org.json.JSONObject;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/** Single peer, bounded control queue, latest-only preview; socket writes never block the camera. */
public final class PeerLink implements AutoCloseable {
    public interface Listener {
        void approval(String address, java.util.function.Consumer<Boolean> answer);
        void connected(); void disconnected(String reason);
        void message(JSONObject message); void frame(byte[] jpeg);
    }
    public static final int PORT=48731;
    private final Listener listener;
    private final BlockingQueue<byte[]> messages=new ArrayBlockingQueue<>(64);
    private final AtomicReference<byte[]> latestFrame=new AtomicReference<>();
    private final BlockingQueue<byte[]> liveAudio=new ArrayBlockingQueue<>(10);
    private final PreviewWindow previewWindow=new PreviewWindow();private volatile boolean pacedPreview;
    private final Semaphore outgoing=new Semaphore(0);
    private volatile boolean closed;
    private volatile SecureChannel channel;
    private volatile Socket pending;
    private volatile ServerSocket server;
    public interface SocketProvider { Socket create(String host) throws Exception; }
    private final SocketProvider sockets;
    public PeerLink(Listener listener) { this(listener,host->new Socket()); }
    public PeerLink(Listener listener,SocketProvider sockets) { this.listener=listener;this.sockets=sockets; }
    public boolean connected() { return channel!=null && !closed; }
    public void host(String secret) {
        host(secret,PORT);
    }
    public void host(String secret,int port) {
        new Thread(()->{
            try {
                server=new ServerSocket(); server.setReuseAddress(true); server.bind(new InetSocketAddress(port));
                while(!closed) {
                    Socket socket=server.accept(); socket.setTcpNoDelay(true); pending=socket;
                    try {
                        SecureChannel c=SecureChannel.server(socket,secret);
                        CountDownLatch latch=new CountDownLatch(1); boolean[] allowed={false};
                        listener.approval(socket.getInetAddress().getHostAddress(),yes->{allowed[0]=yes;latch.countDown();});
                        if(!latch.await(30,TimeUnit.SECONDS)||!allowed[0]||closed) { c.close(); continue; }
                        c.send("APPROVED".getBytes(StandardCharsets.UTF_8)); run(c,true);
                    } catch(Exception e) { try {socket.close();} catch(Exception ignored) {} }
                    finally { pending=null; }
                }
            } catch(Exception e) { if(!closed) listener.disconnected("Host failed: "+e.getMessage()); }
            finally {try {if(server!=null)server.close();}catch(Exception ignored){}}
        },"solo-host").start();
    }
    public void join(String host,String secret) {
        new Thread(()->{
            try {
                Socket socket=sockets.create(host); pending=socket;socket.setTcpNoDelay(true);
                if(closed){socket.close();return;}
                socket.connect(new InetSocketAddress(host,PORT),7000);
                SecureChannel c=SecureChannel.client(socket,secret);
                socket.setSoTimeout(35000);
                if(!"APPROVED".equals(new String(c.receive(),StandardCharsets.UTF_8))) throw new Exception("Pairing rejected");
                run(c,false);
            } catch(Exception e) { if(!closed) listener.disconnected("Connection failed. Join the camera hotspot, scan its current code, then approve on camera. "+e.getMessage()); }
            finally { if(pending!=null) try {pending.close();} catch(Exception ignored) {} pending=null; }
        },"solo-join").start();
    }
    private void run(SecureChannel c,boolean host) throws Exception {
        if(closed){c.close();return;}
        c.ready(); messages.clear();liveAudio.clear(); latestFrame.set(null);previewWindow.reset();pacedPreview=false; channel=c;
        Thread writer=new Thread(()->{
            try {
                long heartbeat=System.currentTimeMillis(),frameSequence=0;
                while(!closed && channel==c) {
                    outgoing.tryAcquire(500,TimeUnit.MILLISECONDS);outgoing.drainPermits();
                    for(int i=0;i<8;i++){byte[] msg=messages.poll();if(msg==null)break;c.send(msg);}
                    for(int i=0;i<5;i++){byte[] audio=liveAudio.poll();if(audio==null)break;c.send(audio);}if(!liveAudio.isEmpty())outgoing.release();
                    if(!messages.isEmpty())outgoing.release();
                    if(pacedPreview&&previewWindow.expired(System.nanoTime()))throw new java.io.IOException("Preview acknowledgment timed out");
                    if(!pacedPreview||!previewWindow.waiting()){
                        byte[] frame=latestFrame.getAndSet(null);
                        if(frame!=null){if(pacedPreview&&frame[0]!=6){long id=frameSequence++;previewWindow.acquire(id,System.nanoTime());byte[] packet=new byte[frame.length+8];packet[0]=4;java.nio.ByteBuffer.wrap(packet,1,8).putLong(id);System.arraycopy(frame,1,packet,9,frame.length-1);c.send(packet);}else c.send(frame);}
                    }
                    if(System.currentTimeMillis()-heartbeat>2000) { c.send(new byte[]{0}); heartbeat=System.currentTimeMillis(); }
                }
            } catch(Exception e) { try {c.close();} catch(Exception ignored) {} }
        },"solo-send"); writer.start();if(!host){JSONObject flow=new JSONObject();flow.put("type","previewFlow");flow.put("version",1);send(flow);} listener.connected();
        try {
            while(!closed) {
                byte[] data=c.receive(); if(data.length==0) throw new Exception("Empty packet");
                if(data[0]==1){JSONObject message=new JSONObject(new String(data,1,data.length-1,StandardCharsets.UTF_8));String type=message.optString("type");if(host&&type.equals("previewFlow")&&message.optInt("version")==1){pacedPreview=true;outgoing.release();}else if(host&&type.equals("previewAck")){if(previewWindow.acknowledge(message.optLong("sequence",-2))&&latestFrame.get()!=null)outgoing.release();}else listener.message(message);}
                else if(data[0]==2) listener.frame(java.util.Arrays.copyOfRange(data,1,data.length));
                else if(data[0]==4){if(data.length<10)throw new Exception("Invalid live preview packet");long sequence=java.nio.ByteBuffer.wrap(data,1,8).getLong();listener.frame(java.util.Arrays.copyOfRange(data,9,data.length));JSONObject ack=new JSONObject();ack.put("type","previewAck");ack.put("sequence",sequence);if(!send(ack))throw new Exception("Cannot acknowledge live preview");}
                else if(data[0]!=0) throw new Exception("Unknown packet type");
            }
        } finally {
            channel=null; c.close(); writer.interrupt(); writer.join(1000);
            if(!closed) listener.disconnected("Disconnected. Camera recording continues while the camera app stays open. Reconnect manually.");
        }
    }
    public boolean send(JSONObject json) {
        if(!connected()) return false;
        byte[] body=json.toString().getBytes(StandardCharsets.UTF_8),data=new byte[body.length+1];
        data[0]=1;System.arraycopy(body,0,data,1,body.length);
        if(!messages.offer(data)) { close(); listener.disconnected("Control queue full; reconnect to reconcile state."); return false; }
        outgoing.release();return true;
    }
    public void frame(byte[] jpeg) {
        if(!connected()||jpeg.length>SecureChannel.MAX_PACKET-25) return;
        byte[] data=new byte[jpeg.length+1];data[0]=2;System.arraycopy(jpeg,0,data,1,jpeg.length);if(latestFrame.getAndSet(data)==null)outgoing.release();
    }
    public void screenFrame(byte[] jpeg,long timestampUs){if(!connected()||jpeg.length>SecureChannel.MAX_PACKET-25)return;byte[] data=new byte[jpeg.length+9];data[0]=6;java.nio.ByteBuffer.wrap(data,1,8).putLong(timestampUs);System.arraycopy(jpeg,0,data,9,jpeg.length);if(latestFrame.getAndSet(data)==null)outgoing.release();}
    public void audio(byte[] pcm,long timestampUs){if(!connected()||pcm.length>19200||pcm.length%2!=0)return;byte[] data=new byte[pcm.length+13];data[0]=5;java.nio.ByteBuffer.wrap(data,1,12).putLong(timestampUs).putInt(48000);System.arraycopy(pcm,0,data,13,pcm.length);if(!liveAudio.offer(data)){liveAudio.poll();liveAudio.offer(data);}outgoing.release();}
    @Override public void close() {
        closed=true;
        try {if(server!=null) server.close();} catch(Exception ignored) {}
        try {if(channel!=null) channel.close();} catch(Exception ignored) {}
        try {if(pending!=null) pending.close();} catch(Exception ignored) {}
        channel=null; messages.clear();liveAudio.clear();latestFrame.set(null);
    }
}



