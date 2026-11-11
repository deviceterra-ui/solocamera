package studio.solocamera;

import java.util.*;

/** Deterministic voice-activity envelope shared by playback and export. */
final class CreatorDucking {
    private final ArrayList<long[]> ranges=new ArrayList<>();private final double strength;private final long fade;
    CreatorDucking(EditorProject p){strength=p.duckStrength;fade=Math.max(20,p.duckFadeMs);ArrayList<long[]> raw=new ArrayList<>();for(EditorProject.Audio a:p.audios)if(a.narration&&a.activity.length>0){for(int i=0;i<a.activity.length;i++)if(a.activity[i]>=.25f){long start=a.duration*i/a.activity.length,end=a.duration*(i+1)/a.activity.length;start=Math.max(start,a.start);end=Math.min(end,a.end);if(end>start)raw.add(new long[]{a.at+start-a.start,a.at+end-a.start});}}if(p.duckCameraSpeech)for(int k=0;k<p.clips.size();k++){EditorProject.Clip c=p.clips.get(k);if(!c.speechSource)continue;for(int i=0;i<c.speechActivity.length;i++)if(c.speechActivity[i]>=.25f){long start=Math.max(c.start,c.duration*i/c.speechActivity.length),end=Math.min(c.end,c.duration*(i+1)/c.speechActivity.length);if(end>start)raw.add(new long[]{p.offset(k)+Math.round((start-c.start)/c.speed),p.offset(k)+Math.round((end-c.start)/c.speed)});}}raw.sort(Comparator.comparingLong(a->a[0]));for(long[] r:raw){if(!ranges.isEmpty()&&ranges.get(ranges.size()-1)[1]>=r[0])ranges.get(ranges.size()-1)[1]=Math.max(ranges.get(ranges.size()-1)[1],r[1]);else ranges.add(r);}}
    double gain(long time){int low=0,high=ranges.size();while(low<high){int mid=(low+high)>>>1;if(ranges.get(mid)[0]<=time)low=mid+1;else high=mid;}long distance=Long.MAX_VALUE;if(low>0)distance=Math.max(0,time-ranges.get(low-1)[1]);if(low<ranges.size())distance=Math.min(distance,ranges.get(low)[0]-time);return 1-strength*Math.max(0,1-distance/(double)fade);}
}
