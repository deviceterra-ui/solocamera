package studio.solocamera;
public final class EditTimingTest {
    static void check(boolean condition){if(!condition)throw new AssertionError();}
    public static void main(String[] args){
        check(EditTiming.duration(1000,11000,2)==5000);
        check(EditTiming.duration(1000,11000,.25)==40000);
        check(EditTiming.sourceTime(1000,11000,2,2500)==6000);
        check(EditTiming.sourceTime(1000,11000,2,999999)==11000);
        check(EditTiming.sourceTime(1000,11000,.5,-100)==1000);
        check(EditTiming.canSplit(1000,3000,1200));check(!EditTiming.canSplit(1000,3000,1199));check(!EditTiming.canSplit(1000,3000,2801));
        for(double speed:new double[]{.25,.5,1,1.5,2,4})for(int position=0;position<=10000;position+=25){long length=EditTiming.duration(0,10000,speed);long mapped=EditTiming.sourceTime(0,10000,speed,Math.round(position/speed));check(Math.abs(mapped-position)<=2);check(length>0);}
        for(double speed:new double[]{0,Double.NaN,Double.POSITIVE_INFINITY,5}){boolean rejected=false;try{EditTiming.duration(0,1000,speed);}catch(IllegalArgumentException expected){rejected=true;}check(rejected);}
        System.out.println("PASS trimmed and speed-adjusted timeline mapping, split margins, boundary clamps and invalid timing rejection");
        check(EditTiming.wrapCaption("abcdefghijkl",5).equals("abcde\nfghij\nkl"));check(EditTiming.wrapCaption("abc\ndef",3).equals("abc\ndef"));
        check(EditTiming.wrapCaption("A\uD83D\uDE00B",2).equals("A\uD83D\uDE00\nB"));
        System.out.println("PASS long caption wrapping preserves text and explicit line breaks");
    }
}
