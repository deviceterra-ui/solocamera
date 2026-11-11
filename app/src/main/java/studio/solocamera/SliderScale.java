package studio.solocamera;

/** Hardware-range mapping used by the camera sliders; independent of Android for testing. */
public final class SliderScale {
    private SliderScale() {}
    public static double value(int progress,int steps,double min,double max,boolean logarithmic){
        validate(steps,min,max,logarithmic);
        double t=Math.max(0,Math.min(steps,progress))/(double)steps;
        return logarithmic?min*Math.pow(max/min,t):min+(max-min)*t;
    }
    public static int progress(double value,int steps,double min,double max,boolean logarithmic){
        validate(steps,min,max,logarithmic);
        if(max==min)return 0;
        double v=Double.isFinite(value)?Math.max(min,Math.min(max,value)):min;
        double t=logarithmic?Math.log(v/min)/Math.log(max/min):(v-min)/(max-min);
        return (int)Math.round(t*steps);
    }
    private static void validate(int steps,double min,double max,boolean log){
        if(steps<1||!Double.isFinite(min)||!Double.isFinite(max)||max<min||(log&&min<=0))throw new IllegalArgumentException("Invalid slider range");
    }
}
