package studio.solocamera;

final class VideoQuality {
    static int bitrate(int width,int height,int fps,boolean high,int minimum,int maximum){
        if(width<=0||height<=0||fps<=0||minimum<=0||maximum<minimum)throw new IllegalArgumentException("Invalid video format");
        long estimate=Math.round((double)width*height*fps*(high?.28:.14));
        return (int)Math.max(minimum,Math.min(maximum,Math.max(2_000_000L,estimate)));
    }
    static String label(int width,int height){
        if(width==3840&&height==2160)return "4K UHD";
        if(width==7680&&height==4320)return "8K UHD";
        if(width==2560&&height==1440)return "1440p / 2.5K";
        if(width==2048&&height==1080)return "2K DCI";
        if(width==2880&&height==1620)return "3K";
        if(width==3072&&height==1728)return "3K";
        if(width==4096&&height==2160)return "4K DCI";
        if(width==1920&&height==1080)return "1080p";
        if(width==1280&&height==720)return "720p";
        return width+" x "+height;
    }
}
