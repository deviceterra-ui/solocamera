package studio.solocamera;
import android.content.Context;import android.graphics.*;import android.view.*;
final class EditorCurve extends View {
 final EditorMotion.Key key;final Runnable begin,change,end;final Paint p=new Paint(3);int handle;
 EditorCurve(Context c,EditorMotion.Key k,Runnable begin,Runnable change,Runnable end){super(c);key=k;this.begin=begin;this.change=change;this.end=end;setContentDescription("Easing curve. Drag either control point.");}
 float x(double v){return 20+(getWidth()-40)*(float)v;}float y(double v){return getHeight()-20-(getHeight()-40)*(float)v;}
 protected void onDraw(Canvas c){c.drawColor(0xff10141a);p.setStrokeWidth(2);p.setColor(0xff586777);c.drawLine(x(0),y(0),x(key.x1),y(key.y1),p);c.drawLine(x(1),y(1),x(key.x2),y(key.y2),p);Path path=new Path();path.moveTo(x(0),y(0));path.cubicTo(x(key.x1),y(key.y1),x(key.x2),y(key.y2),x(1),y(1));p.setColor(0xffffc857);p.setStyle(Paint.Style.STROKE);c.drawPath(path,p);p.setStyle(Paint.Style.FILL);c.drawCircle(x(key.x1),y(key.y1),10,p);c.drawCircle(x(key.x2),y(key.y2),10,p);}
 public boolean onTouchEvent(MotionEvent e){if(e.getAction()==0){begin.run();handle=Math.hypot(e.getX()-x(key.x1),e.getY()-y(key.y1))<Math.hypot(e.getX()-x(key.x2),e.getY()-y(key.y2))?1:2;getParent().requestDisallowInterceptTouchEvent(true);}double nx=EditorProject.bound((e.getX()-20)/(getWidth()-40),0,1),ny=EditorProject.bound((getHeight()-20-e.getY())/(getHeight()-40),0,1);if(handle==1){key.x1=nx;key.y1=ny;}else{key.x2=nx;key.y2=ny;}change.run();invalidate();if(e.getAction()==1||e.getAction()==3){end.run();getParent().requestDisallowInterceptTouchEvent(false);performClick();}return true;}public boolean performClick(){super.performClick();return true;}
}
