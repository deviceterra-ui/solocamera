package studio.solocamera;
public final class VideoQualityTest {
    public static void main(String[] args){
        int standard=VideoQuality.bitrate(1920,1080,30,false,64000,100_000_000);
        int high=VideoQuality.bitrate(1920,1080,30,true,64000,100_000_000);
        int uhd=VideoQuality.bitrate(3840,2160,30,true,64000,100_000_000);
        if(high!=standard*2||uhd!=high*4)throw new AssertionError("Quality must scale with bitrate and pixel count");
        if(VideoQuality.bitrate(7680,4320,60,true,100000,80_000_000)!=80_000_000)throw new AssertionError("Encoder maximum");
        if(VideoQuality.bitrate(640,360,30,false,4_000_000,8_000_000)!=4_000_000)throw new AssertionError("Encoder minimum");
        if(!VideoQuality.label(3840,2160).equals("4K UHD")||VideoQuality.label(2560,1440).equals("4K UHD"))throw new AssertionError("Resolution label");
        if(!VideoQuality.label(2560,1440).contains("1440p")||!VideoQuality.label(3072,1728).equals("3K")||!VideoQuality.label(2048,1080).equals("2K DCI"))throw new AssertionError("Readable resolution families");
        System.out.println("PASS quality bitrate scales with resolution, respects encoder bounds and uses exact resolution labels");
        try{VideoQuality.bitrate(0,1080,30,true,1,100);throw new AssertionError("Invalid format accepted");}catch(IllegalArgumentException expected){}
        System.out.println("PASS invalid video format rejected");
    }
}
