package studio.solocamera;

/** Deterministic cut-centered transitions, shared by thumbnails, live preview and export. */
final class EditorTransitions {
    static final String[] NAMES={"None","Black dip","White flash","Crash zoom","Whip left","Whip right","Whip up","Whip down","Spin clockwise","Spin counter","Zoom pull","Tilt punch","Bounce","Swing","Squeeze X","Squeeze Y","Iris","Wipe left","Wipe right","Shutter","Warm flash"};
    static final String[] NOTES={"Clean cut","Classic cinematic pause","Bright editorial flash","Fast push through the cut","Leftward motion","Rightward motion","Upward motion","Downward motion","Clockwise roll with flash","Counterclockwise roll with flash","Pull away and return","Angled zoom with flash","Springy vertical movement","Pivot through the cut","Compress horizontally","Compress vertically","Close and open a circular iris","Sweep from right to left","Sweep from left to right","Close and open horizontal blades","Warm amber light flash"};
    static final class State {double x,y,scale=1,sx=1,sy=1,rotation,gain=1,r,g,b,open=1;int mask;}
    static double clamp(double v){return Math.max(0,Math.min(1,v));}
    static State sample(EditorProject.Clip c,long at){
        long half=Math.max(1,c.length()/2),in=Math.min(c.fadeIn,half),out=Math.min(c.fadeOut,half);
        if(in>0&&at<in)return phase(Math.max(1,c.transitionIn),1-clamp(at/(double)in),true);
        if(out>0&&at>c.length()-out)return phase(Math.max(1,c.transitionOut),1-clamp((c.length()-at)/(double)out),false);
        return new State();
    }
    static State phase(int type,double amount,boolean incoming){State s=new State();double q=clamp(amount);q=q*q*(3-2*q);double direction=incoming?1:-1;
        switch(type){
            case 1:s.gain=1-q;break;
            case 2:s.gain=1-q;s.r=s.g=s.b=q;break;
            case 3:s.scale=1+1.4*q;s.gain=1-q*q*q;break;
            case 4:s.x=direction*2*q;break;
            case 5:s.x=-direction*2*q;break;
            case 6:s.y=-direction*2*q;break;
            case 7:s.y=direction*2*q;break;
            case 8:case 9:s.rotation=direction*(type==8?1:-1)*100*q;s.scale=1+.6*q;s.r=s.g=s.b=q*q*q;s.gain=1-s.r;break;
            case 10:s.scale=1-.85*q;s.gain=1-q*q;break;
            case 11:s.scale=1+.8*q;s.rotation=direction*22*q;s.r=s.g=s.b=q*q*q;s.gain=1-s.r;break;
            case 12:s.y=direction*(2*q+.22*Math.sin(q*3*Math.PI)*(1-q));s.scale=1+.12*Math.sin(q*Math.PI);break;
            case 13:s.x=direction*.8*q;s.rotation=direction*65*q;s.gain=1-q*q;break;
            case 14:s.sx=Math.max(.001,1-q);s.sy=1+.2*q;break;
            case 15:s.sy=Math.max(.001,1-q);s.sx=1+.2*q;break;
            case 16:s.mask=1;s.open=1-q;break;
            case 17:s.mask=incoming?3:2;s.open=1-q;break;
            case 18:s.mask=incoming?2:3;s.open=1-q;break;
            case 19:s.mask=4;s.open=1-q;break;
            case 20:s.gain=1-q;s.r=q;s.g=.65*q;s.b=.25*q;break;
            default:break;
        }return s;
    }
}
