package studio.solocamera;

/** Small per-channel tone curves applied by the camera ISP to preview and recording. */
final class CreatorLooks {
    static final String[] NAMES={"Natural","Cinema","Vlog","Warm","Cool","Fade","Golden hour","Teal & orange","Moody","Pastel","Bleach","Vintage"};
    static float[] curve(int look,int channel,double strength,int points){
        if(look<1||look>=NAMES.length||channel<0||channel>2||!Double.isFinite(strength)||strength<0||strength>100||points<4)throw new IllegalArgumentException("Invalid look");
        float[] result=new float[points*2];double last=0;
        for(int i=0;i<points;i++){
            double x=(double)i/(points-1),base=x<=.0031308?12.92*x:1.055*Math.pow(x,1/2.4)-.055,y=base;
            switch(look){
                case 1:y=.025+.95*(base*base*(3-2*base));if(channel==0)y+=.025*base*(1-base);if(channel==2)y-=.025*base*(1-base);break;
                case 2:y=Math.pow(base,.9);break;
                case 3:y=base+(channel==0?.16:channel==2?-.16:0)*base*(1-base);break;
                case 4:y=base+(channel==2?.14:channel==0?-.14:0)*base*(1-base);break;
                case 5:y=.075+.85*base;break;
                case 6:y=base+(channel==0?.17:channel==2?-.18:0)*base*(1-base);break;
                case 7:y=base+(channel==0?.18:channel==1?.015:channel==2?-.17:0)*base*(1-base);break;
                case 8:y=base+.17*base*(1-base);break;
                case 9:y=base-.18*base*(1-base);break;
                case 10:y=Math.pow(base,1.08);break;
                case 11:y=.025+.95*(base*base*(3-2*base))+(channel==0?.08:channel==2?-.08:0)*base*(1-base);break;
                default:break;
            }
            y=Math.max(last,Math.max(0,Math.min(1,base+(y-base)*strength/100)));last=y;
            result[2*i]=(float)x;result[2*i+1]=(float)y;
        }
        return result;
    }
}
