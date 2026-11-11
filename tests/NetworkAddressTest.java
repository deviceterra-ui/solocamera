package studio.solocamera;
public class NetworkAddressTest {
    public static void main(String[] args){
        byte[] hotspot={(byte)192,(byte)168,43,1};
        if(!NetworkAddress.sameSubnet(hotspot,new byte[]{(byte)192,(byte)168,43,22},24))throw new AssertionError("Hotspot client must match");
        if(NetworkAddress.sameSubnet(hotspot,new byte[]{10,1,1,1},24))throw new AssertionError("Cellular address must not match");
        if(!NetworkAddress.sameSubnet(new byte[]{10,0,1,1},new byte[]{10,0,1,3},30))throw new AssertionError("Non octet prefix");
        if(NetworkAddress.sameSubnet(new byte[]{10,0,1,1},new byte[]{10,0,1,4},30))throw new AssertionError("Outside subnet");
        if(NetworkAddress.sameSubnet(hotspot,new byte[16],24)||NetworkAddress.sameSubnet(hotspot,hotspot,33))throw new AssertionError("Invalid address");
        System.out.println("PASS hotspot Wi-Fi subnet matching, non-octet masks and invalid inputs");
    }
}
