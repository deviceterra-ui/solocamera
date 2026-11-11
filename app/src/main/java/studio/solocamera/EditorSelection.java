package studio.solocamera;
import java.util.*;

/** Ephemeral selection uses object identity, so sorting clips does not change selected objects. */
final class EditorSelection {
    final Set<Object> items=Collections.newSetFromMap(new IdentityHashMap<>());
    void clear(){items.clear();}
    boolean contains(Object item){return items.contains(item);}
    void track(EditorProject p,String kind,int row){clear();p.normalizeTracks();if(kind.equals("Video"))items.addAll(p.clips);else if(kind.equals("Overlay")){for(EditorProject.Overlay o:p.overlays)if(o.track==row)items.add(o);}else if(kind.equals("Audio")){for(EditorProject.Audio a:p.audios)if(a.track==row)items.add(a);}else if(kind.equals("Effect")){for(EditorProject.EffectClip e:p.effects)if(e.track==row)items.add(e);}else for(EditorProject.Caption c:p.captions)if(c.type.equals(kind)&&c.track==row)items.add(c);}
    void all(EditorProject p){clear();items.addAll(p.clips);items.addAll(p.overlays);items.addAll(p.captions);items.addAll(p.audios);items.addAll(p.effects);}
    int size(){return items.size();}
    void delete(EditorProject p){EditorEdits.freezeMain(p);p.clips.removeIf(items::contains);p.overlays.removeIf(items::contains);p.captions.removeIf(items::contains);p.audios.removeIf(items::contains);p.effects.removeIf(items::contains);if(!p.allowMainGaps)EditorEdits.closeMainGaps(p);clear();}
}
