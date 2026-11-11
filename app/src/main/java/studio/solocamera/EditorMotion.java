package studio.solocamera;
import org.json.*;
import java.util.*;
/** Local-time object keyframes, shared by the live canvas and export. */
final class EditorMotion {
    static final class Key {long at;double x,y,scale=1,rotation,alpha=1,x1=.25,y1=.25,x2=.75,y2=.75;
        JSONObject json()throws JSONException{return new JSONObject().put("at",at).put("x",x).put("y",y).put("scale",scale).put("rotation",rotation).put("alpha",alpha).put("x1",x1).put("y1",y1).put("x2",x2).put("y2",y2);}}
    static JSONArray json(List<Key> keys)throws JSONException{JSONArray a=new JSONArray();for(Key k:keys)a.put(k.json());return a;}
    static void read(JSONArray a,List<Key> keys)throws JSONException{if(a==null)return;if(a.length()>1000)throw new JSONException("Too many keyframes");for(int i=0;i<a.length();i++){JSONObject j=a.getJSONObject(i);Key k=new Key();k.at=Math.max(0,j.optLong("at"));k.x=EditorProject.bound(j.optDouble("x"),-2,2);k.y=EditorProject.bound(j.optDouble("y"),-2,2);k.scale=EditorProject.bound(j.optDouble("scale",1),.025,8);k.rotation=EditorProject.bound(j.optDouble("rotation"),-720,720);k.alpha=EditorProject.bound(j.optDouble("alpha",1),0,1);k.x1=EditorProject.bound(j.optDouble("x1",.25),0,1);k.y1=EditorProject.bound(j.optDouble("y1",.25),0,1);k.x2=EditorProject.bound(j.optDouble("x2",.75),0,1);k.y2=EditorProject.bound(j.optDouble("y2",.75),0,1);keys.add(k);}keys.sort((x,y)->Long.compare(x.at,y.at));}
    static double cubic(double t,double a,double b){double u=1-t;return 3*u*u*t*a+3*u*t*t*b+t*t*t;}
    static double ease(Key k,double x){double lo=0,hi=1;for(int i=0;i<24;i++){double m=(lo+hi)/2;if(cubic(m,k.x1,k.x2)<x)lo=m;else hi=m;}return cubic((lo+hi)/2,k.y1,k.y2);}
    static Key sample(EditorProject.Clip c,long at,boolean text){return sample(c,at,text,c.length());}
    static Key sample(EditorProject.Clip c,long at,boolean text,long duration){Key k=sampleBase(c,at,text);EditorTransitions.State s=EditorTransitions.sample(c,at);k.x+=s.x;k.y+=s.y;k.scale*=s.scale;k.rotation+=s.rotation;if(text)EditorTitlePresets.motion(c,at,duration,k);return k;}
    static double white(EditorProject.Clip c,long at){return EditorTransitions.sample(c,at).r;}
    static Key sampleBase(EditorProject.Clip c,long at,boolean text){Key base=new Key();base.at=at;base.x=text?c.textX:c.panX;base.y=text?(c.textPlaced?c.textY:c.textPosition==0?.7:c.textPosition==2?-.7:0):c.panY;base.scale=c.zoom;base.rotation=c.motionRotation;
        if(c.keys.isEmpty())return base;Key a=c.keys.get(0),b=a;for(Key k:c.keys){if(k.at<=at)a=k;if(k.at>=at){b=k;break;}b=k;}if(at<=a.at||a==b)return copy(a,at);double f=ease(a,Math.max(0,Math.min(1,(at-a.at)/(double)Math.max(1,b.at-a.at))));Key out=new Key();out.at=at;out.x=a.x+(b.x-a.x)*f;out.y=a.y+(b.y-a.y)*f;out.scale=a.scale+(b.scale-a.scale)*f;out.rotation=a.rotation+(b.rotation-a.rotation)*f;out.alpha=a.alpha+(b.alpha-a.alpha)*f;return out;}
    static Key copy(Key a,long at){Key k=new Key();k.at=at;k.x=a.x;k.y=a.y;k.scale=a.scale;k.rotation=a.rotation;k.alpha=a.alpha;k.x1=a.x1;k.y1=a.y1;k.x2=a.x2;k.y2=a.y2;return k;}
    static Key put(EditorProject.Clip c,long at,boolean text){for(Key k:c.keys)if(Math.abs(k.at-at)<20)return k;Key k=copy(sampleBase(c,at,text),at);c.keys.add(k);c.keys.sort((a,b)->Long.compare(a.at,b.at));return k;}
    static void place(EditorProject.Clip c,double x,double y,long at,boolean text){Key pose=sampleBase(c,at,text);double dx=x-pose.x,dy=y-pose.y;if(text){c.textX+=dx;c.textY=(c.textPlaced?c.textY:c.textPosition==0?.7:c.textPosition==2?-.7:0)+dy;c.textPlaced=true;}else{c.panX+=dx;c.panY+=dy;}for(Key key:c.keys){key.x+=dx;key.y+=dy;}}
    static double scale(EditorProject.Clip c,double value,long at,boolean text){double old=sampleBase(c,at,text).scale,ratio=value/Math.max(.025,old);c.zoom*=ratio;for(Key key:c.keys)key.scale*=ratio;return value;}
    static Key nearest(EditorProject.Clip c,long at,long tolerance){Key closest=null;long distance=tolerance;for(Key k:c.keys){long delta=Math.abs(k.at-at);if(delta<distance){closest=k;distance=delta;}}return closest;}
    static void freeze(EditorProject.Clip c,Key pose,boolean text){if(text){c.textX=pose.x;c.textY=pose.y;c.textPlaced=true;}else{c.panX=pose.x;c.panY=pose.y;}c.zoom=pose.scale;c.motionRotation=pose.rotation;c.opacity*=pose.alpha;}
    static boolean remove(EditorProject.Clip c,Key key,boolean text){if(key==null||!c.keys.contains(key))return false;Key pose=sampleBase(c,key.at,text);c.keys.remove(key);if(c.keys.isEmpty())freeze(c,pose,text);return true;}
    static void clear(EditorProject.Clip c,long at,boolean text){Key pose=sampleBase(c,at,text);c.keys.clear();freeze(c,pose,text);}
    static double transition(EditorProject.Clip c,long at){return EditorTransitions.sample(c,at).gain;}
}
