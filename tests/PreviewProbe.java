package studio.solocamera;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.*;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
public class PreviewProbe {
 public static void main(String[] args)throws Exception{
  var doc=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(args[0]));
  var nodes=doc.getElementsByTagName("node");String key=null;
  for(int i=0;i<nodes.getLength();i++){var n=(org.w3c.dom.Element)nodes.item(i);var m=Pattern.compile("Key: ([a-fA-F0-9]{32})").matcher(n.getAttribute("text"));if(m.find())key=m.group(1);}
  if(key==null)throw new Exception("Pairing details missing");
  Socket socket=new Socket();socket.connect(new InetSocketAddress(args[1],Integer.parseInt(args[2])),7000);
  try(SecureChannel c=SecureChannel.client(socket,key)){
   socket.setSoTimeout(35000);System.out.println("Authenticated; waiting for camera approval");
   if(!new String(c.receive(),StandardCharsets.UTF_8).equals("APPROVED"))throw new Exception("Not approved");
   c.ready();c.send(("\u0001{\"type\":\"command\",\"action\":\"state\"}").getBytes(StandardCharsets.UTF_8));
   int frames=0,states=0;long end=System.currentTimeMillis()+6000;
   while(System.currentTimeMillis()<end){byte[] packet=c.receive();if(packet[0]==1)states++;if(packet[0]==2){var image=ImageIO.read(new ByteArrayInputStream(packet,1,packet.length-1));if(image==null)throw new Exception("Invalid JPEG");frames++;}c.send(new byte[]{0});}
   if(frames<5||states==0)throw new Exception("Missing preview or state: "+frames+" / "+states);
   System.out.println("PASS live encrypted preview: "+frames+" decoded JPEG frames, "+states+" state packets in 6 seconds");
  }
 }
}
