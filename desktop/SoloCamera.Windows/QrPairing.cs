using System.Buffers.Binary;
using System.Net;
using System.Net.NetworkInformation;
using System.Net.Sockets;
using System.Security.Cryptography;
using System.Text;
namespace SoloCamera.Windows;
// A short-lived LAN rendezvous. The phone key is exchanged only inside AES-GCM.
public sealed class QrPairing:IDisposable {
 readonly TcpListener listener=new(IPAddress.Any,0);readonly CancellationTokenSource stop=new(TimeSpan.FromMinutes(2));readonly Action<string,string> paired;
 public string Secret{get;}=Convert.ToHexString(RandomNumberGenerator.GetBytes(16)).ToLowerInvariant();public int Port{get;}public string Status{get;private set;}="Scan with Solo Camera → Webcam on your phone.";
 public static string[] Addresses()=>NetworkInterface.GetAllNetworkInterfaces().Where(n=>n.OperationalStatus==OperationalStatus.Up&&n.NetworkInterfaceType!=NetworkInterfaceType.Loopback).SelectMany(n=>n.GetIPProperties().UnicastAddresses).Select(a=>a.Address).Where(a=>a.AddressFamily==AddressFamily.InterNetwork&&!IPAddress.IsLoopback(a)&&!a.ToString().StartsWith("169.254.")).Select(a=>a.ToString()).Distinct().ToArray();
 public string Code(string host)=>$"solocamera-desktop:1:{host}:{Port}:{Secret}";
 public QrPairing(Action<string,string> paired){this.paired=paired;listener.Start(4);Port=((IPEndPoint)listener.LocalEndpoint).Port;_=Accept();}
 async Task Accept(){try{while(!stop.IsCancellationRequested){using var client=await listener.AcceptTcpClientAsync(stop.Token);try{client.NoDelay=true;using var timeout=CancellationTokenSource.CreateLinkedTokenSource(stop.Token);timeout.CancelAfter(8000);var token=timeout.Token;var stream=client.GetStream();byte[] challenge=RandomNumberGenerator.GetBytes(32);await stream.WriteAsync(challenge,token);byte[] material=Encoding.UTF8.GetBytes("SoloCamera-v1").Concat(Convert.FromHexString(Secret)).Concat(challenge).ToArray();byte[] key=SHA256.HashData(material)[..16];CryptographicOperations.ZeroMemory(material);using var aes=new AesGcm(key,16);CryptographicOperations.ZeroMemory(key);long sent=0,received=0;
 async Task<byte[]> Read(){byte[] header=new byte[4];await stream.ReadExactlyAsync(header,token);int count=BinaryPrimitives.ReadInt32BigEndian(header);if(count<16||count>1024)throw new IOException("Invalid pairing packet");byte[] data=new byte[count];await stream.ReadExactlyAsync(data,token);return PhoneConnection.Decrypt(aes,data,2,received++);}
 async Task Write(string text){byte[] data=PhoneConnection.Encrypt(aes,Encoding.UTF8.GetBytes(text),1,sent++);byte[] header=new byte[4];BinaryPrimitives.WriteInt32BigEndian(header,data.Length);await stream.WriteAsync(header,token);await stream.WriteAsync(data,token);}
 if(Encoding.UTF8.GetString(await Read())!="HELLO-v1")throw new IOException("Invalid handshake");await Write("APPROVED");string secret=Encoding.UTF8.GetString(await Read());var secretParts=secret.Split(':');if(secretParts.Length>2||secretParts[0].Length!=32||!secretParts[0].All(Uri.IsHexDigit)||(secretParts.Length==2&&(!int.TryParse(secretParts[1],out int screenPort)||screenPort!=48732)))throw new IOException("Invalid camera key");string address=((IPEndPoint)client.Client.RemoteEndPoint!).Address.ToString();await Write("REGISTERED");Status="Phone found. Approve the camera connection on your phone.";listener.Stop();paired(address,secret);return;
 }catch(Exception)when(!stop.IsCancellationRequested){Status="Unable to pair. Check the network and scan this code again.";}}}catch when(stop.IsCancellationRequested){}finally{listener.Stop();if(stop.IsCancellationRequested)Status="Code expired. Close this window and choose Pair by QR again.";}}
 public void Dispose(){stop.Cancel();listener.Stop();}
}

