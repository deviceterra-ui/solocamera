package studio.solocamera;

import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.view.View;
import android.widget.ImageView;

public final class PreviewGuides extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);private final ImageView image;
    private boolean grid,safe;private double ratio;private float safeRight=.18f,safeBottom=.22f;private Bitmap signal;private float focusX,focusY;private long focusUntil;
    public PreviewGuides(Context c,ImageView image){super(c);this.image=image;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
    public void grid(boolean enabled){grid=enabled;invalidate();}
    public void social(double ratio,boolean safe){this.ratio=ratio;this.safe=safe;invalidate();}
    public void safeMargins(float right,float bottom){safeRight=Math.max(.05f,Math.min(.4f,right));safeBottom=Math.max(.05f,Math.min(.4f,bottom));invalidate();}
    public void signal(Bitmap bitmap){if(signal!=null&&signal!=bitmap)signal.recycle();signal=bitmap;invalidate();}
    public void focus(float x,float y){focusX=x;focusY=y;focusUntil=SystemClock.uptimeMillis()+1200;invalidate();postDelayed(this::invalidate,1250);}
    @Override protected void onDraw(Canvas c){super.onDraw(c);paint.setStrokeWidth(getResources().getDisplayMetrics().density);paint.setStyle(Paint.Style.STROKE);
        if(grid&&image.getDrawable()!=null){RectF r=new RectF(0,0,image.getDrawable().getIntrinsicWidth(),image.getDrawable().getIntrinsicHeight());image.getImageMatrix().mapRect(r);paint.setColor(0x70ffffff);for(int i=1;i<3;i++){float x=r.left+r.width()*i/3,y=r.top+r.height()*i/3;c.drawLine(x,r.top,x,r.bottom,paint);c.drawLine(r.left,y,r.right,y,paint);}}
        if(image.getDrawable()!=null){RectF r=new RectF(0,0,image.getDrawable().getIntrinsicWidth(),image.getDrawable().getIntrinsicHeight());image.getImageMatrix().mapRect(r);if(signal!=null){paint.setStyle(Paint.Style.FILL);c.drawBitmap(signal,null,r,paint);}if(ratio>0){float w=r.width(),h=r.height();if(w/h>ratio)w=(float)(h*ratio);else h=(float)(w/ratio);RectF crop=new RectF(r.centerX()-w/2,r.centerY()-h/2,r.centerX()+w/2,r.centerY()+h/2);paint.setStyle(Paint.Style.STROKE);paint.setColor(0xffffc857);c.drawRect(crop,paint);if(safe){paint.setPathEffect(new DashPathEffect(new float[]{8,8},0));RectF keep=new RectF(crop.left+w*.08f,crop.top+h*.12f,crop.right-w*safeRight,crop.bottom-h*safeBottom);c.drawRect(keep,paint);paint.setPathEffect(null);paint.setStyle(Paint.Style.FILL);paint.setTextSize(11*getResources().getDisplayMetrics().density);c.drawText("SAFE AREA",keep.left,keep.top-5,paint);}}}
        if(SystemClock.uptimeMillis()<focusUntil){paint.setStyle(Paint.Style.STROKE);paint.setColor(0xffffc857);float s=26*getResources().getDisplayMetrics().density;c.drawRoundRect(focusX-s,focusY-s,focusX+s,focusY+s,3,3,paint);}
    }
}
