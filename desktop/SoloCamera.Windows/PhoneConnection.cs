using System.Buffers.Binary;
using System.Net;
using System.Net.Sockets;
using System.Security.Cryptography;
using System.Text;
using System.Text.Json;

namespace SoloCamera.Windows;

public sealed record CameraFrame(byte[] Jpeg,int Rotation,long Sequence,DateTime At,long SourceUs=0);
public sealed record LiveAudioFrame(long Sequence,long SourceUs,int Rate,byte[] Pcm,DateTime At);
/// <summary>
/// Owns one authenticated phone feed. Video keeps only the latest frame while audio
/// uses a short bounded queue so a slow OBS consumer cannot build unlimited latency.
/// </summary>
public sealed class PhoneConnection : IDisposable {
    public const int MaxPacket=2_000_000;
    private CancellationTokenSource? cancellation;private TcpClient? client;private Task? task;
    private TaskCompletionSource<bool> frameChanged=new(TaskCreationOptions.RunContinuationsAsynchronously);
    private readonly object audioGate=new();private readonly Queue<LiveAudioFrame> audio=new();private long audioSequence;
    public LiveAudioFrame[] AudioAfter(long after){lock(audioGate)return audio.Where(x=>x.Sequence>after&&(DateTime.UtcNow-x.At).TotalSeconds<1).ToArray();}
    public void AcceptAudio(byte[] data){if(data.Length<15||data.Length>19213||(data.Length-13)%2!=0)throw new IOException("Invalid live audio packet");long time=BinaryPrimitives.ReadInt64BigEndian(data.AsSpan(1,8));int rate=BinaryPrimitives.ReadInt32BigEndian(data.AsSpan(9,4));if(rate!=48000||time<0)throw new IOException("Unsupported live audio format");lock(audioGate){audio.Enqueue(new(++audioSequence,time,rate,data[13..],DateTime.UtcNow));while(audio.Count>16)audio.Dequeue();}}
    private CameraFrame? latest;private long frames;private int rotation;private string status="Not connected";
    public string Status=>Volatile.Read(ref status);public CameraFrame? Latest=>Volatile.Read(ref latest);public long Frames=>Interlocked.Read(ref frames);
    public bool Running=>task is {IsCompleted:false};
    public void Connect(string address,string secret,int port=48731){
        if(secret.Contains(':')){var parts=secret.Split(':');if(parts.Length!=2||!int.TryParse(parts[1],out port))throw new ArgumentException("Invalid screen port");secret=parts[0];}
        if(Running)throw new InvalidOperationException("Disconnect this camera before reconnecting.");
        if(!IPAddress.TryParse(address,out var ip)||ip.AddressFamily!=AddressFamily.InterNetwork)throw new ArgumentException("Enter the phone’s IPv4 address.");
        if(secret.Length!=32||!secret.All(Uri.IsHexDigit))throw new ArgumentException("Enter the complete 32-character pairing key from the phone.");
        if(port<1||port>65535)throw new ArgumentException("Invalid camera port.");
        cancellation?.Dispose();cancellation=new();latest=null;frames=0;rotation=0;lock(audioGate){audio.Clear();}status="Connecting…";var stop=cancellation.Token;task=Task.Run(()=>Run(address,port,secret,stop));
    }
    private async Task Run(string address,int port,string secret,CancellationToken stop){
        try{client=new TcpClient{NoDelay=true};await client.ConnectAsync(address,port,stop).AsTask().WaitAsync(TimeSpan.FromSeconds(8),stop);using var stream=client.GetStream();byte[] challenge=new byte[32];await ReadExact(stream,challenge,stop,10);
            byte[] derivation=Encoding.UTF8.GetBytes("SoloCamera-v1").Concat(Convert.FromHexString(secret)).Concat(challenge).ToArray();byte[] key=SHA256.HashData(derivation)[..16];CryptographicOperations.ZeroMemory(derivation);using var cipher=new AesGcm(key,16);using var receiver=new AesGcm(key,16);CryptographicOperations.ZeroMemory(key);long sent=0,received=0;using var sendGate=new SemaphoreSlim(1);
            async Task Send(byte[] body){await sendGate.WaitAsync(stop);try{byte[] payload=Encrypt(cipher,body,2,sent++);byte[] framed=new byte[payload.Length+4];BinaryPrimitives.WriteInt32BigEndian(framed,payload.Length);payload.CopyTo(framed,4);await stream.WriteAsync(framed,stop);}finally{sendGate.Release();}}            async Task<byte[]> Receive(int timeout){byte[] size=new byte[4];await ReadExact(stream,size,stop,timeout);int count=BinaryPrimitives.ReadInt32BigEndian(size);if(count<16||count>MaxPacket)throw new IOException("Invalid encrypted packet length");byte[] payload=new byte[count];await ReadExact(stream,payload,stop,timeout);return Decrypt(receiver,payload,1,received++);}
            await Send(Encoding.UTF8.GetBytes("HELLO-v1"));status="Approve this connection on the phone";if(Encoding.UTF8.GetString(await Receive(35))!="APPROVED")throw new IOException("The phone did not approve pairing");await Send(Encoding.UTF8.GetBytes("\u0001{\"type\":\"previewFlow\",\"version\":1}"));status="Connected · waiting for camera";
            using var linked=CancellationTokenSource.CreateLinkedTokenSource(stop);var keepAlive=Task.Run(async()=>{try{while(!linked.IsCancellationRequested){await Send(new byte[]{0});await Task.Delay(2000,linked.Token);}}catch{client?.Close();}},linked.Token);
            try{while(!stop.IsCancellationRequested){byte[] data=await Receive(15);if(data.Length==0)throw new IOException("Empty camera packet");if(data[0]==5){AcceptAudio(data);}else if(data[0]==2||data[0]==4||data[0]==6){bool screen=data[0]==6;if(screen&&data.Length<13)throw new IOException("Invalid screen frame");bool paced=data[0]==4;long sourceUs=screen?BinaryPrimitives.ReadInt64BigEndian(data.AsSpan(1,8)):0;if(paced&&data.Length<10)throw new IOException("Invalid live preview packet");long id=paced?BinaryPrimitives.ReadInt64BigEndian(data.AsSpan(1,8)):-1;byte[] jpeg=data[(paced||screen?9:1)..];if(jpeg.Length<4||jpeg[0]!=0xff||jpeg[1]!=0xd8)throw new IOException("Invalid camera JPEG");long sequence=Interlocked.Increment(ref frames);Volatile.Write(ref latest,new CameraFrame(jpeg,screen?0:rotation,sequence,DateTime.UtcNow,sourceUs));Interlocked.Exchange(ref frameChanged,new(TaskCreationOptions.RunContinuationsAsynchronously)).TrySetResult(true);status=screen?"Live screen demo · audio available in OBS":paced?"Live · low latency":"Live · legacy sender";if(paced)await Send(Encoding.UTF8.GetBytes("\u0001{\"type\":\"previewAck\",\"sequence\":"+id+"}"));}else if(data[0]==1){using var json=JsonDocument.Parse(data.AsMemory(1));if(json.RootElement.TryGetProperty("rotation",out var value)&&value.TryGetInt32(out int degrees))rotation=((degrees%360)+360)%360;}else if(data[0]!=0)throw new IOException("Unknown camera packet");}}finally{linked.Cancel();await keepAlive;}
        }catch(Exception e){status=stop.IsCancellationRequested?"Disconnected":"Disconnected · "+e.Message;}finally{client?.Dispose();client=null;Volatile.Write(ref latest,null);lock(audioGate)audio.Clear();}
    }
    public async Task<CameraFrame?> WaitForNewFrame(long after,CancellationToken stop){var changed=Volatile.Read(ref frameChanged).Task;var frame=Latest;if(frame!=null&&frame.Sequence!=after)return frame;try{await changed.WaitAsync(TimeSpan.FromMilliseconds(250),stop);}catch(TimeoutException){return null;}frame=Latest;return frame?.Sequence!=after?frame:null;}    public static byte[] Encrypt(AesGcm cipher,byte[] body,int direction,long sequence){byte[] nonce=new byte[12];BinaryPrimitives.WriteInt32BigEndian(nonce,direction);BinaryPrimitives.WriteInt64BigEndian(nonce.AsSpan(4),sequence);byte[] result=new byte[body.Length+16];cipher.Encrypt(nonce,body,result.AsSpan(0,body.Length),result.AsSpan(body.Length,16));return result;}
    public static byte[] Decrypt(AesGcm cipher,byte[] data,int direction,long sequence){byte[] nonce=new byte[12];BinaryPrimitives.WriteInt32BigEndian(nonce,direction);BinaryPrimitives.WriteInt64BigEndian(nonce.AsSpan(4),sequence);byte[] result=new byte[data.Length-16];cipher.Decrypt(nonce,data.AsSpan(0,result.Length),data.AsSpan(result.Length,16),result);return result;}
    private static async Task ReadExact(Stream stream,byte[] data,CancellationToken stop,int seconds){using var timeout=CancellationTokenSource.CreateLinkedTokenSource(stop);timeout.CancelAfter(TimeSpan.FromSeconds(seconds));await stream.ReadExactlyAsync(data,timeout.Token);}
    public void Disconnect(){status="Disconnected";cancellation?.Cancel();client?.Close();latest=null;lock(audioGate)audio.Clear();}
    public async Task WaitStopped(){if(task!=null)await task;}
    public void Dispose(){Disconnect();}
}





