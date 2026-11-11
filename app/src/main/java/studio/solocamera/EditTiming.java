package studio.solocamera;

final class EditTiming {
    static long duration(long start,long end,double speed){
        if(start<0||end<=start||!Double.isFinite(speed)||speed<.25||speed>4)throw new IllegalArgumentException("Invalid clip timing");
        return Math.max(1,Math.round((end-start)/speed));
    }
    static long sourceTime(long start,long end,double speed,long projectOffset){
        duration(start,end,speed);return Math.max(start,Math.min(end,start+Math.round(Math.max(0,projectOffset)*speed)));
    }
    static boolean canSplit(long start,long end,long source){return source-start>=200&&end-source>=200;}
    static String wrapCaption(String text,int columns){
        if(columns<1)throw new IllegalArgumentException("Invalid caption width");
        StringBuilder out=new StringBuilder();int count=0;
        for(int i=0;i<text.length();){int c=text.codePointAt(i);i+=Character.charCount(c);if(c=='\n'){out.appendCodePoint(c);count=0;continue;}if(count>=columns){out.append('\n');count=0;}out.appendCodePoint(c);count++;}
        return out.toString();
    }

}
