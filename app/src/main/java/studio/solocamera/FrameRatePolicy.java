package studio.solocamera;

final class FrameRatePolicy {
    static boolean allowed(int fps,int ceiling,boolean autoRange,boolean manual,long maxDuration){
        return fps>=10&&fps<=ceiling&&(autoRange||(manual&&maxDuration>0&&1_000_000_000L/fps<=maxDuration));
    }
}
