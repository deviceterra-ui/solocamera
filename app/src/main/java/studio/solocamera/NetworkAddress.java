package studio.solocamera;

final class NetworkAddress {
    static boolean sameSubnet(byte[] local,byte[] remote,int prefix){
        if(local.length!=4||remote.length!=4||prefix<0||prefix>32)return false;
        for(int i=0;i<4;i++){int bits=Math.min(8,Math.max(0,prefix-i*8));int mask=(0xff<<(8-bits))&0xff;if((local[i]&mask)!=(remote[i]&mask))return false;}
        return true;
    }
}
