package studio.solocamera;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Recording appearance changes only after camera confirmation. */
public final class RecordButton extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean recording,busy;private int countdown;
    public RecordButton(Context c){super(c);setClickable(true);setFocusable(true);setContentDescription("Start recording");}
    public void state(boolean recording,boolean busy){if(this.recording==recording&&this.busy==busy)return;this.recording=recording;this.busy=busy;setContentDescription(recording?"Stop recording and save":"Start recording");invalidate();}
    public void countdown(int seconds){if(countdown==seconds)return;countdown=seconds;setContentDescription(seconds>0?"Cancel countdown":recording?"Stop recording and save":"Start recording");invalidate();}
    @Override protected void onDraw(Canvas c){super.onDraw(c);float r=Math.min(getWidth(),getHeight())*.42f,x=getWidth()/2f,y=getHeight()/2f;
        paint.setColor(isEnabled()?0xffe8ebed:0xff666a70);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(r*.06f);c.drawCircle(x,y,r,paint);
        paint.setStyle(Paint.Style.FILL);paint.setColor(isEnabled()?0xffff4d55:0xff69383d);
        if(recording)c.drawRoundRect(x-r*.46f,y-r*.46f,x+r*.46f,y+r*.46f,r*.12f,r*.12f,paint);else c.drawCircle(x,y,r*.76f,paint);
        if(busy){paint.setColor(0xffffc857);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(r*.07f);c.drawArc(x-r,y-r,x+r,y+r,-90,240,false,paint);}
        if(countdown>0){paint.setStyle(Paint.Style.FILL);paint.setColor(Color.WHITE);paint.setTextSize(r*.9f);paint.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));paint.setTextAlign(Paint.Align.CENTER);c.drawText(String.valueOf(countdown),x,y+r*.33f,paint);}
    }
}
