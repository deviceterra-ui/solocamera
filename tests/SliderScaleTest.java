package studio.solocamera;

public final class SliderScaleTest {
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    private static void near(double a,double b,double tolerance){check(Math.abs(a-b)<=tolerance,"Expected "+b+", got "+a);}
    public static void main(String[] args){
        near(SliderScale.value(0,1000,100,6400,true),100,.0001);
        near(SliderScale.value(1000,1000,100,6400,true),6400,.0001);
        near(SliderScale.value(500,1000,100,6400,true),800,.0001);
        System.out.println("PASS ISO logarithmic endpoints and equal-stop midpoint");
        for(int step=0;step<=1000;step++){
            double value=SliderScale.value(step,1000,1,10,true);
            check(value>=1&&value<=10,"Zoom out of range");
            check(SliderScale.progress(value,1000,1,10,true)==step,"Slider confirmation moved thumb");
        }
        System.out.println("PASS zoom round trip preserves thumb position across all 1001 steps");
        double previous=0;
        for(int step=0;step<=1000;step++){double v=SliderScale.value(step,1000,0,10,false);check(v>=previous&&v>=0&&v<=10,"Focus scale not bounded/monotonic");previous=v;}
        near(SliderScale.value(500,1000,-6,6,false),0,.0001);
        System.out.println("PASS focus monotonicity and signed exposure midpoint");
        near(SliderScale.value(-100,1000,100,800,true),100,.0001);
        near(SliderScale.value(2000,1000,100,800,true),800,.0001);
        check(SliderScale.progress(Double.NaN,1000,100,800,true)==0,"Invalid metadata should select minimum");
        check(SliderScale.progress(9000,1000,100,800,true)==1000,"Metadata outside max should clamp");
        System.out.println("PASS out-of-range and missing metadata cannot escape hardware limits");
        near(SliderScale.value(700,1000,1.8,1.8,false),1.8,.0001);
        check(SliderScale.progress(1.8,1000,1.8,1.8,false)==0,"Fixed aperture has no adjustment travel");
        System.out.println("PASS fixed aperture maps without division by zero");
        for(int step=0;step<=1000;step++){
            double denominator=SliderScale.value(step,1000,31,10000,true);
            long exposure=Math.round(1e9/denominator);
            check(exposure>=100000&&exposure<=33333333,"Shutter escaped the 30 fps exposure budget");
        }
        System.out.println("PASS shutter slider remains inside validated recording exposure range");
        boolean rejected=false;try{SliderScale.value(0,1000,0,10,true);}catch(IllegalArgumentException expected){rejected=true;}
        check(rejected,"Logarithmic zero bound accepted");
        rejected=false;try{SliderScale.progress(1,0,1,10,true);}catch(IllegalArgumentException expected){rejected=true;}
        check(rejected,"Empty slider range accepted");
        System.out.println("PASS invalid slider configuration fails explicitly");
    }
}
