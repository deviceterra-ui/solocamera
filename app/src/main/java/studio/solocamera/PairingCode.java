package studio.solocamera;

/** Versioned, bounded local-network pairing payload. No arbitrary URLs are opened. */
final class PairingCode {
    static String encode(String host,String key) { validate(host,key);return "solocamera:1:"+host+":"+key; }
    static String[] decode(String value) {
        if(value==null||value.length()>128)throw new IllegalArgumentException("Not a Solo Camera code");
        String[] parts=value.split(":",-1);
        if(parts.length!=4||!parts[0].equals("solocamera")||!parts[1].equals("1"))throw new IllegalArgumentException("Not a Solo Camera code");
        validate(parts[2],parts[3]);return new String[]{parts[2],parts[3]};
    }
    private static void validate(String host,String key) {
        if(!key.matches("[a-fA-F0-9]{32}"))throw new IllegalArgumentException("Invalid pairing key");
        String[] octets=host.split("\\.",-1);
        if(octets.length!=4)throw new IllegalArgumentException("Invalid camera address");
        for(String octet:octets)if(!octet.matches("[0-9]{1,3}")||Integer.parseInt(octet)>255)throw new IllegalArgumentException("Invalid camera address");
        int first=Integer.parseInt(octets[0]);
        if(first==0||first==127||first>=224)throw new IllegalArgumentException("Invalid camera address");
    }
}
