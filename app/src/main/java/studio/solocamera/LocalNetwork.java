package studio.solocamera;

import android.content.Context;
import android.net.*;
import java.net.*;
import java.util.*;

/** Local preview must use Wi-Fi even when Android prefers cellular for Internet. */
final class LocalNetwork {
    static Socket socket(Context context,String host)throws Exception {
        ConnectivityManager manager=context.getSystemService(ConnectivityManager.class);
        Network fallback=null;InetAddress destination=InetAddress.getByName(host);
        for(Network network:manager.getAllNetworks()){
            NetworkCapabilities caps=manager.getNetworkCapabilities(network);
            if(caps==null||!caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)||caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN))continue;
            fallback=network;LinkProperties links=manager.getLinkProperties(network);
            if(links!=null)for(LinkAddress address:links.getLinkAddresses()){
                if(address.getAddress() instanceof Inet4Address&&NetworkAddress.sameSubnet(address.getAddress().getAddress(),destination.getAddress(),address.getPrefixLength()))return network.getSocketFactory().createSocket();
            }
        }
        if(fallback!=null)return fallback.getSocketFactory().createSocket();
        throw new java.io.IOException("Connect this monitor to the camera phone's hotspot or the same Wi-Fi first. Internet access is not needed.");
    }
    static String addresses(){
        ArrayList<String> result=new ArrayList<>();
        try{ArrayList<NetworkInterface> interfaces=Collections.list(NetworkInterface.getNetworkInterfaces());
            interfaces.sort(Comparator.comparingInt(n->priority(n.getName())));
            for(NetworkInterface network:interfaces){
                String name=network.getName().toLowerCase(Locale.US);
                if(!network.isUp()||network.isLoopback()||network.isPointToPoint()||name.contains("rmnet")||name.contains("ccmni")||name.contains("pdp")||name.contains("tun")||name.contains("usb")||name.contains("rndis"))continue;
                for(InetAddress address:Collections.list(network.getInetAddresses()))if(address instanceof Inet4Address&&address.isSiteLocalAddress()&&!result.contains(address.getHostAddress()))result.add(address.getHostAddress());
            }
        }catch(Exception ignored){}
        return result.isEmpty()?"No local address. Enable the camera hotspot or connect Wi-Fi first.":String.join("\n",result);
    }
    private static int priority(String name){name=name.toLowerCase(Locale.US);return name.startsWith("ap")||name.contains("swlan")?0:name.contains("wlan")||name.contains("wifi")?1:2;}
}
