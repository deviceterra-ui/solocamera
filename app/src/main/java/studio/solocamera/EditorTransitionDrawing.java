package studio.solocamera;
import android.graphics.*;
/** Geometry shared by transition tiles and the live mask canvas. */
final class EditorTransitionDrawing {
 static void clip(Canvas c,RectF r,EditorTransitions.State s){float p=(float)s.open;if(s.mask==1){Path path=new Path();path.addCircle(r.centerX(),r.centerY(),(float)Math.hypot(r.width(),r.height())*.5f*p,Path.Direction.CW);c.clipPath(path);}else if(s.mask==2)c.clipRect(r.left,r.top,r.left+r.width()*p,r.bottom);else if(s.mask==3)c.clipRect(r.right-r.width()*p,r.top,r.right,r.bottom);else if(s.mask==4)c.clipRect(r.left,r.centerY()-r.height()*.5f*p,r.right,r.centerY()+r.height()*.5f*p);}
}
