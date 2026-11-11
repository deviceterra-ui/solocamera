package studio.solocamera;
public final class CreatorSettingsTest {
    static void check(boolean valid){if(!valid)throw new AssertionError();}
    public static void main(String[] args){
        for(int fps=10;fps<=30;fps++)check(FrameRatePolicy.allowed(fps,30,false,true,100_000_000));
        check(!FrameRatePolicy.allowed(24,30,false,false,100_000_000));
        check(FrameRatePolicy.allowed(25,30,true,false,0));
        check(!FrameRatePolicy.allowed(10,30,false,true,50_000_000));
        check(!FrameRatePolicy.allowed(31,30,true,true,100_000_000));
        check(!FrameRatePolicy.allowed(0,30,true,true,100_000_000));
        System.out.println("PASS FPS boundaries, custom manual timing and automatic range requirements");
        for(int look=1;look<CreatorLooks.NAMES.length;look++)for(int channel=0;channel<3;channel++)for(int amount:new int[]{0,25,70,100}){
            float[] curve=CreatorLooks.curve(look,channel,amount,32);float previous=-1;
            for(int i=0;i<curve.length;i+=2){check(curve[i]>=0&&curve[i]<=1);check(curve[i+1]>=previous&&curve[i+1]<=1);previous=curve[i+1];}
            check(curve[0]==0&&curve[curve.length-2]==1);
        }
        float[] warmRed=CreatorLooks.curve(3,0,100,32),warmBlue=CreatorLooks.curve(3,2,100,32);
        check(warmRed[31]>warmBlue[31]);
        for(int channel=0;channel<3;channel++)check(java.util.Arrays.equals(CreatorLooks.curve(1,channel,0,32),CreatorLooks.curve(5,channel,0,32)));
        boolean rejected=false;try{CreatorLooks.curve(2,0,Double.NaN,32);}catch(IllegalArgumentException expected){rejected=true;}check(rejected);
        System.out.println("PASS creator curves stay monotonic and bounded, blend to neutral, tint correctly and reject invalid intensity");
    }
}
