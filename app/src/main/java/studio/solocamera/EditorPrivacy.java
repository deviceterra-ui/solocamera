package studio.solocamera;
import android.graphics.*;import java.util.*;
/** Opaque editor objects, rendered after all normal layers in both preview and export. */
final class EditorPrivacy {
 static EditorProject.Caption create(RectF box,long at,long end,int track){EditorProject.Caption c=new EditorProject.Caption();c.type="Image";c.track=track;c.at=at;c.end=Math.max(at+200,end);c.style=new EditorProject.Clip("privacy:blackout","Privacy blackout",Math.max(200,c.end-at));c.style.privacyBlackout=true;c.style.privacyWidth=Math.max(.005,Math.min(1,box.width()));c.style.privacyHeight=Math.max(.005,Math.min(1,box.height()));c.style.text="Privacy blackout";c.style.textPlaced=true;c.style.textX=box.centerX()*2-1;c.style.textY=1-box.centerY()*2;return c;}
 static Bitmap bitmap(EditorProject.Clip c,int w,int h){Bitmap b=Bitmap.createBitmap(Math.max(2,(int)Math.ceil(w*c.privacyWidth)),Math.max(2,(int)Math.ceil(h*c.privacyHeight)),Bitmap.Config.ARGB_8888);b.eraseColor(Color.BLACK);return b;}
 static java.util.List<EditorProject.Caption> ordered(EditorProject p){ArrayList<EditorProject.Caption> list=new ArrayList<>(p.captions);list.sort(Comparator.comparing(c->c.style.privacyBlackout));return list;}
}
