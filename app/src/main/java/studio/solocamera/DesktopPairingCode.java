package studio.solocamera;
/** Desktop rendezvous codes are data, never browser URLs. */
final class DesktopPairingCode {
 static String[] decode(String raw){if(raw==null||raw.length()>150)throw new IllegalArgumentException("Invalid desktop code");String[] p=raw.split(":",-1);if(p.length!=5||!p[0].equals("solocamera-desktop")||!p[1].equals("1"))throw new IllegalArgumentException("Invalid desktop code");PairingCode.encode(p[2],p[4]);if(!p[3].matches("[0-9]{1,5}"))throw new IllegalArgumentException("Invalid port");int port=Integer.parseInt(p[3]);if(port<1||port>65535)throw new IllegalArgumentException("Invalid port");return new String[]{p[2],p[3],p[4]};}
}
