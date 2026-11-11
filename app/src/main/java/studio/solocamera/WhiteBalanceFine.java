package studio.solocamera;

/** Relative adjustment around a captured camera white point; not a calibrated Kelvin scale. */
final class WhiteBalanceFine {
    static float[] gains(float red,float greenEven,float greenOdd,float blue,double amount){
        if(!Double.isFinite(amount)||amount < -100||amount > 100)throw new IllegalArgumentException("White balance fine adjustment must be -100 to +100");
        float[] base={red,greenEven,greenOdd,blue};
        for(float value:base)if(!Float.isFinite(value)||value<=0)throw new IllegalArgumentException("Invalid baseline color gains");
        if(amount==0)return base;
        double scale=Math.pow(2,amount/100.0);
        // Stay within Android's guaranteed gain interval, or the baseline's already-applied gain.
        base[0]=(float)Math.max(1,Math.min(Math.max(3,red),red*scale));
        base[3]=(float)Math.max(1,Math.min(Math.max(3,blue),blue/scale));
        return base;
    }
}
