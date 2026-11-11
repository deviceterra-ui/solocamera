package studio.solocamera;

import com.google.zxing.*;
import com.google.zxing.common.*;
import com.google.zxing.qrcode.*;

public class PairingCodeTest {
    public static void main(String[] args)throws Exception{
        String key="0123456789abcdef0123456789abcdef";
        String payload=PairingCode.encode("192.168.43.1",key);
        BitMatrix qr=new QRCodeWriter().encode(payload,BarcodeFormat.QR_CODE,480,480);
        int[] pixels=new int[480*480];for(int y=0;y<480;y++)for(int x=0;x<480;x++)pixels[y*480+x]=qr.get(x,y)?0xff000000:0xffffffff;
        String scanned=new QRCodeReader().decode(new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(480,480,pixels)))).getText();
        String[] decoded=PairingCode.decode(scanned);
        if(!decoded[0].equals("192.168.43.1")||!decoded[1].equals(key))throw new AssertionError("Round trip");
        System.out.println("PASS QR image encodes and decodes exact camera address and secret");
        String[] invalid={"https://example.com","solocamera:2:192.168.1.1:"+key,"solocamera:1:999.1.1.1:"+key,"solocamera:1:127.0.0.1:"+key,"solocamera:1:192.168.1.1:short","solocamera:1:192.168.1.1:"+key+":extra","x".repeat(129)};
        for(String value:invalid){try{PairingCode.decode(value);throw new AssertionError("Accepted invalid payload");}catch(IllegalArgumentException expected){}}
        System.out.println("PASS unrelated, malformed, unsupported and oversized QR payloads rejected");
    }
}
