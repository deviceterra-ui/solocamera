package studio.solocamera;
import android.content.Context;import android.graphics.*;import android.view.View;
/** Editing aid only; never enters the exported composition. */
final class EditorSafeGuides extends View {
 static final String[] NAMES={"Off","TikTok","Instagram / Facebook Reels","YouTube Shorts","Presentation 5%"};
 private final Paint p=new Paint(3);private int mode;
 EditorSafeGuides(Context c){super(c);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);setClickable(false);}
 void mode(int m){mode=m;invalidate();}
 static RectF safe(int mode,float w,float h){return mode==4?new RectF(w*.05f,h*.05f,w*.95f,h*.95f):new RectF(w*.08f,h*.12f,w*(mode==2?.85f:.82f),h*(mode==3?.76f:.72f));}
 @Override protected void onDraw(Canvas c){if(mode==0)return;RectF s=safe(mode,getWidth(),getHeight());p.setColor(0x55370000);c.drawRect(0,0,getWidth(),s.top,p);c.drawRect(0,s.bottom,getWidth(),getHeight(),p);c.drawRect(0,s.top,s.left,s.bottom,p);c.drawRect(s.right,s.top,getWidth(),s.bottom,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2*getResources().getDisplayMetrics().density);p.setColor(0xff66d4e2);c.drawRect(s,p);p.setStyle(Paint.Style.FILL);p.setTextSize(11*getResources().getDisplayMetrics().scaledDensity);c.drawText("SAFE AREA · approximate",s.left+6,s.top+20*getResources().getDisplayMetrics().density,p);}
}
