package studio.solocamera;
import java.util.*;

/** Track-local edits: no operation here mutates another track or its timing. */
final class EditorEdits {
    static void closeMainGaps(EditorProject p){long cursor=0;for(EditorProject.Clip c:p.clips){c.timelineAt=cursor;cursor+=c.length();}}
    static void deleteMain(EditorProject p,int index){if(index<0||index>=p.clips.size())throw new IllegalArgumentException("Select a main clip");freezeMain(p);p.clips.remove(index);if(!p.allowMainGaps)closeMainGaps(p);}
    static void trim(EditorProject p,String kind,int index,int overlay,long at,int edge){
        if(kind.equals("Audio")){if(edge==2){p.musicAt=0;p.musicEnd=Long.MAX_VALUE;}else if(edge==0)p.musicAt=Math.max(0,Math.min(at,Math.min(p.duration(),p.musicEnd)-1));else p.musicEnd=Math.max(p.musicAt+1,Math.min(p.duration(),at));return;}
        if(kind.equals("Text")){EditorProject.Caption caption=p.captions.get(index);if(edge==2){caption.at=0;caption.end=Math.max(1,p.videoDuration());}else if(edge==0)caption.at=Math.max(0,Math.min(at,caption.end-1));else caption.end=Math.max(caption.at+1,at);return;}
        EditorProject.Clip c=overlay>=0?p.overlays.get(overlay).clip:p.clips.get(index);long offset=overlay>=0?p.overlays.get(overlay).at:p.offset(index);
        if(edge==2){c.start=0;c.end=c.duration;return;}
        long source=EditTiming.sourceTime(c.start,c.end,c.speed,at-offset);
        if(edge==0)c.start=Math.min(c.end-200,source);else c.end=Math.max(c.start+200,source);
    }

    // Moving the main row fixes existing timestamps before changing its order.
    static void freezeMain(EditorProject p){long[] at=new long[p.clips.size()];for(int i=0;i<at.length;i++)at[i]=p.offset(i);for(int i=0;i<at.length;i++)p.clips.get(i).timelineAt=at[i];}
    static long availableMainTime(EditorProject p,int exclude,long requested,long length){
        long at=Math.max(0,requested);for(int i=0;i<p.clips.size();i++){if(i==exclude)continue;long start=p.offset(i),end=start+p.clips.get(i).length();if(at<end&&at+length>start)at=end;}return at;
    }
    /** Returns the moved item's new index. target=-1 is main video, >=0 is a PIP row. */
    static int moveVideo(EditorProject p,int main,int overlay,int target,long requested){
        if(overlay<0&&target>=0&&p.overlays.size()>=EditorProject.MAX_CLIPS)throw new IllegalArgumentException("Project item limit reached");
        if(overlay>=0&&target<0&&p.clips.size()>=EditorProject.MAX_CLIPS)throw new IllegalArgumentException("Project item limit reached");
        EditorProject.Clip c=overlay>=0?p.overlays.get(overlay).clip:p.clips.get(main);long at=target<0?availableMainTime(p,overlay<0?main:-1,requested,c.length()):Math.max(0,requested);
        freezeMain(p);
        if(target<0){if(overlay>=0){EditorProject.Overlay o=p.overlays.remove(overlay);c.zoom*=o.scale;c.panX=o.x+o.scale*c.panX;c.panY=o.y+o.scale*c.panY;p.clips.add(c);}c.timelineAt=at;p.clips.sort((a,b)->Long.compare(a.timelineAt,b.timelineAt));return p.clips.indexOf(c);}
        EditorProject.Overlay o;if(overlay>=0)o=p.overlays.remove(overlay);else{o=new EditorProject.Overlay();o.clip=p.clips.remove(main);o.x=0;o.y=0;o.scale=1;}
        o.track=target;o.at=availableLayerTime(p,-1,target,at,c.length());p.overlays.add(o);p.overlays.sort((a,b)->a.track==b.track?Long.compare(a.at,b.at):Integer.compare(a.track,b.track));return p.overlays.indexOf(o);
    }
    static long availableLayerTime(EditorProject p,int exclude,int row,long at,long length){p.normalizeTracks();ArrayList<EditorProject.Overlay> items=new ArrayList<>();for(int i=0;i<p.overlays.size();i++)if(i!=exclude&&p.overlays.get(i).track==row)items.add(p.overlays.get(i));items.sort((a,b)->Long.compare(a.at,b.at));for(EditorProject.Overlay o:items)if(at<o.at+o.clip.length()&&at+length>o.at)at=o.at+o.clip.length();return at;}
}
