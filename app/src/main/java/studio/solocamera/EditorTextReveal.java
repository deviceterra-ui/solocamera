package studio.solocamera;

import android.graphics.*;

/** Small deterministic title reveal library shared by preview and export. */
final class EditorTextReveal {
    static final String[] NAMES={"Fade","Rise","Drop","Slide left","Slide right","Slide up","Slide down","Pop","Zoom in","Zoom out","Typewriter","Wipe left","Wipe right","Wipe up","Wipe down","Iris","Soft reveal","Bounce","Elastic","Blur-like fade","Flicker","Glitch","Wave","Flip","Spin","Sparkle","Confetti","Fireflies","Stardust","Burst","Comet","Bubbles","Snow","Hearts","Stars","Pixel dust","Neon sparks","Gold dust","Color pop","Particle sweep"};
    private EditorTextReveal(){}
    static Bitmap frame(Bitmap source,int preset,float progress){
        if(source==null||preset<=0)return source;
        float p=Math.max(0,Math.min(1,progress));int w=source.getWidth(),h=source.getHeight();Bitmap out=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(out);Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
        int mode=(preset-1)%25;
        if(mode==9||mode==10||mode==11||mode==12||mode==13||mode==14||mode==15||mode==16||mode==21||mode==22||mode==23||mode==24){float q=mode==10?Math.min(1,p*1.8f):p;Rect src=new Rect(0,0,w,h);Rect dst=new Rect(0,0,w,h);if(mode==10)dst.right=Math.max(1,(int)(w*q));else if(mode==11)src.left=(int)(w*(1-q));else if(mode==12)src.right=Math.max(1,(int)(w*q));else if(mode==13)src.top=(int)(h*(1-q));else if(mode==14)src.bottom=Math.max(1,(int)(h*q));else if(mode==15){float r=(float)Math.sqrt(q);dst.inset((int)(w*(1-r)/2),(int)(h*(1-r)/2));}else if(mode==16){dst.left=(int)(w*(1-q));}else if(mode==21){dst.left=(int)(w*(1-q));}else if(mode==22){dst.top=(int)(h*(1-q));}else if(mode==23){c.rotate(360*q,w/2f,h/2f);}else if(mode==24){dst.left=(int)(w*(1-q));}c.drawBitmap(source,src,dst,paint);
        }else{float a=p;if(mode==0)a=p;if(mode==1)a=p; if(mode==7)a=Math.min(1,p*1.7f);paint.setAlpha(Math.round(255*a));c.save();float scale=mode==7?.55f+.45f*p:mode==8?.8f+.2f*p:mode==17?(.92f+.08f*(float)Math.sin(p*8*Math.PI)):mode==18?.75f+.25f*p:1;c.scale(scale,scale,w/2f,h/2f);float dy=mode==1?(1-p)*h*.22f:mode==2?-(1-p)*h*.22f:0,dx=mode==3?(1-p)*w*.25f:mode==4?-(1-p)*w*.25f:0;c.translate(dx,dy);c.drawBitmap(source,0,0,paint);c.restore();}
        if(preset>=26){Paint particle=new Paint(Paint.ANTI_ALIAS_FLAG);particle.setColor(Color.WHITE);particle.setAlpha(Math.round(220*(1-p)*Math.min(1,p*4)));int count=10+(preset%7)*3;for(int i=0;i<count;i++){float phase=(i*37%101)/101f;float x=(phase*w+(i%2==0?1:-1)*p*w*.18f)%w,y=(h*(.15f+((i*53)%83)/100f))-p*h*.16f;float radius=Math.max(1,Math.min(w,h)*(.003f+(i%4)*.0012f));if(preset%3==0)c.drawRect(x-radius,y-radius,x+radius,y+radius,particle);else c.drawCircle(x,y,radius,particle);}}
        return out;
    }
}
