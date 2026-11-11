package studio.solocamera;

import android.graphics.*;
import android.graphics.drawable.Drawable;

/** Small native vector icons, with no font or network dependencies. */
public final class CameraIcon extends Drawable {
    private final String name;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int color;

    public CameraIcon(String name, int color) { this.name = name; this.color = color; }
    @Override public void draw(Canvas canvas) {
        int save = canvas.save();
        canvas.translate(getBounds().left, getBounds().top);
        canvas.scale(getBounds().width() / 24f, getBounds().height() / 24f);
        paint.setColor(color); paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(1.65f);
        paint.setStrokeCap(Paint.Cap.ROUND); paint.setStrokeJoin(Paint.Join.ROUND);
        switch (name) {
            case "keyframe":path(canvas,12,3,21,12,12,21,3,12,12,3);line(canvas,8,12,16,12);line(canvas,12,8,12,16);break;
            case "ai":path(canvas,12,2,22,12,12,22,2,12,12,2);line(canvas,12,7,12,17);line(canvas,7,12,17,12);break;
            case "undo":path(canvas,8,3,3,8,8,13);canvas.drawArc(3,8,21,22,180,260,false,paint);break;
            case "redo":path(canvas,16,3,21,8,16,13);canvas.drawArc(3,8,21,22,0,-260,false,paint);break;
            case "film":
                canvas.drawRoundRect(3,3,21,21,2,2,paint);line(canvas,7,3,7,21);line(canvas,17,3,17,21);line(canvas,3,8,7,8);line(canvas,17,16,21,16);path(canvas,10,8,15,12,10,16);break;
            case "add":line(canvas,12,4,12,20);line(canvas,4,12,20,12);break;
            case "trim":path(canvas,7,3,7,17,21,17);path(canvas,3,7,17,7,17,21);break;
            case "split":circle(canvas,5,6,3);circle(canvas,5,18,3);line(canvas,8,7,21,20);line(canvas,8,17,21,4);break;
            case "volume":path(canvas,3,9,7,9,12,4,12,20,7,15,3,15,3,9);canvas.drawArc(9,5,23,19,-65,130,false,paint);break;
            case "music":path(canvas,9,17,9,5,20,3,20,15);circle(canvas,6,18,3);circle(canvas,17,16,3);break;
            case "rotate":canvas.drawArc(3,3,21,21,15,290,false,paint);path(canvas,15,2,20,3,19,8);canvas.drawRect(8,8,16,16,paint);break;
            case "duplicate":canvas.drawRoundRect(7,7,21,21,2,2,paint);path(canvas,3,16,3,3,16,3);break;
            case "delete":line(canvas,3,6,21,6);path(canvas,6,6,7,21,17,21,18,6);path(canvas,8,6,8,3,16,3,16,6);line(canvas,10,10,10,17);line(canvas,14,10,14,17);break;
            case "fps":
                canvas.drawRoundRect(3,3,21,21,2,2,paint);line(canvas,3,8,21,8);line(canvas,3,16,21,16);line(canvas,8,3,8,8);line(canvas,16,16,16,21);break;
            case "look":
                circle(canvas,9,10,6);circle(canvas,15,10,6);circle(canvas,12,15,6);break;
            case "iso":
                canvas.drawArc(3, 3, 21, 21, 135, 270, false, paint);
                line(canvas, 12, 12, 17, 7); circle(canvas, 12, 12, 1.5f);
                line(canvas, 8, 20, 16, 20); break;
            case "shutter": case "aperture":
                circle(canvas, 12, 12, 9);
                for (int i = 0; i < 6; i++) {
                    int s = canvas.save(); canvas.rotate(i * 60, 12, 12);
                    line(canvas, 8, 4, 15, 9); canvas.restoreToCount(s);
                } break;
            case "focus":
                path(canvas, 8, 3, 3, 3, 3, 8); path(canvas, 16, 3, 21, 3, 21, 8);
                path(canvas, 3, 16, 3, 21, 8, 21); path(canvas, 16, 21, 21, 21, 21, 16);
                circle(canvas, 12, 12, 3); break;
            case "wb": case "daylight":
                circle(canvas, 12, 12, 4);
                for (int i = 0; i < 8; i++) { int s=canvas.save(); canvas.rotate(i*45,12,12);line(canvas,12,2,12,4);canvas.restoreToCount(s); } break;
            case "zoom":
                circle(canvas, 10, 10, 6);line(canvas,14.5f,14.5f,21,21);line(canvas,7,10,13,10);line(canvas,10,7,10,13);break;
            case "ev":
                circle(canvas,12,12,9);line(canvas,5,10,11,10);line(canvas,8,7,8,13);line(canvas,13,15,19,15);break;
            case "lens":
                circle(canvas,12,12,9);circle(canvas,12,12,5);circle(canvas,12,12,1);break;
            case "flip":
                canvas.drawRoundRect(5,7,19,17,2,2,paint);circle(canvas,12,12,3);
                canvas.drawArc(1,1,23,23,205,105,false,paint);path(canvas,18,1,21,4,17,5);
                canvas.drawArc(1,1,23,23,25,105,false,paint);path(canvas,6,23,3,20,7,19);break;
            case "camera":
                path(canvas,3,7,7,7,9,4,15,4,17,7,21,7,21,20,3,20,3,7);circle(canvas,12,13,4);break;
            case "screen-record":canvas.drawRoundRect(2,3,22,18,2,2,paint);circle(canvas,12,10,4);line(canvas,8,22,16,22);line(canvas,12,18,12,22);break;
            case "monitor":
                canvas.drawRoundRect(2,3,22,17,2,2,paint);line(canvas,12,17,12,21);line(canvas,8,21,16,21);break;
            case "torch": path(canvas,13,2,5,14,11,14,10,22,19,9,13,9,13,2);break;
            case "grid":
                canvas.drawRect(3,3,21,21,paint);line(canvas,9,3,9,21);line(canvas,15,3,15,21);line(canvas,3,9,21,9);line(canvas,3,15,21,15);break;
            case "script":
                canvas.drawRoundRect(4,2,20,22,2,2,paint);line(canvas,8,7,16,7);line(canvas,8,11,16,11);line(canvas,8,15,14,15);break;
            case "pair":
                canvas.drawRoundRect(2,7,13,17,5,5,paint);canvas.drawRoundRect(11,7,22,17,5,5,paint);line(canvas,8,12,16,12);break;
            case "settings":
                line(canvas,4,6,20,6);line(canvas,4,12,20,12);line(canvas,4,18,20,18);
                paint.setStyle(Paint.Style.FILL);canvas.drawCircle(8,6,2.5f,paint);canvas.drawCircle(16,12,2.5f,paint);canvas.drawCircle(10,18,2.5f,paint);break;
            case "lock":
                canvas.drawRoundRect(5,10,19,21,2,2,paint);canvas.drawArc(8,2,16,14,180,180,false,paint);line(canvas,8,6,8,10);line(canvas,16,6,16,10);break;
            case "stabilization":
                canvas.drawRoundRect(6,5,18,19,2,2,paint);path(canvas,3,8,1,12,3,16);path(canvas,21,8,23,12,21,16);break;
            case "close":line(canvas,6,6,18,18);line(canvas,18,6,6,18);break;
            case "back":path(canvas,15,4,7,12,15,20);break;
            case "play":paint.setStyle(Paint.Style.FILL);path(canvas,7,4,20,12,7,20,7,4);break;
            case "pause":paint.setStyle(Paint.Style.FILL);canvas.drawRoundRect(6,4,10,20,1,1,paint);canvas.drawRoundRect(14,4,18,20,1,1,paint);break;
            case "reset":canvas.drawArc(4,4,20,20,-45,290,false,paint);path(canvas,4,4,4,10,10,10);break;
            case "next":path(canvas,6,5,15,12,6,19);line(canvas,19,5,19,19);break;
            case "mirror":path(canvas,9,4,3,18,9,18,9,4);path(canvas,15,4,21,18,15,18,15,4);line(canvas,12,2,12,22);break;
            case "layout":canvas.drawRect(3,4,21,20,paint);line(canvas,12,4,12,20);break;
            case "text":paint.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));paint.setTextSize(16);paint.setTextAlign(Paint.Align.CENTER);canvas.drawText("T",12,17,paint);line(canvas,5,20,19,20);break;
            case "overlay":canvas.drawRoundRect(4,4,17,17,2,2,paint);canvas.drawRoundRect(8,8,21,21,2,2,paint);break;
            case "effect":path(canvas,12,2,14,9,21,12,14,14,12,22,10,14,3,12,10,10,12,2);circle(canvas,19,5,1);break;
            case "canvas":canvas.drawRect(3,4,21,20,paint);line(canvas,7,8,17,8);line(canvas,7,12,17,12);line(canvas,7,16,13,16);break;
            case "crop":path(canvas,9,3,3,3,3,9);path(canvas,15,3,21,3,21,9);path(canvas,3,15,3,21,9,21);path(canvas,21,15,21,21,15,21);break;
            case "mask":circle(canvas,12,12,9);path(canvas,12,3,12,21);break;
            case "transform":canvas.drawRect(5,5,19,19,paint);circle(canvas,5,5,1.7f);circle(canvas,19,5,1.7f);circle(canvas,5,19,1.7f);circle(canvas,19,19,1.7f);break;
            case "animate":path(canvas,3,16,8,11,12,15,20,6);path(canvas,15,6,20,6,20,11);break;
            case "transition":canvas.drawRoundRect(2,5,10,19,1,1,paint);canvas.drawRoundRect(14,5,22,19,1,1,paint);path(canvas,9,10,15,10,13,8);path(canvas,15,14,9,14,11,16);break;
            case "speed":canvas.drawArc(3,3,21,21,150,240,false,paint);path(canvas,12,12,18,7);line(canvas,5,20,19,20);break;
            case "audio":line(canvas,3,13,6,13);line(canvas,7,9,7,17);line(canvas,11,5,11,21);line(canvas,15,8,15,18);line(canvas,19,11,19,15);break;
            case "freeze":line(canvas,12,3,12,21);line(canvas,4,7,20,17);line(canvas,4,17,20,7);line(canvas,8,4,12,7);line(canvas,12,7,16,4);line(canvas,8,20,12,17);line(canvas,12,17,16,20);break;
            case "export":path(canvas,12,16,12,3);path(canvas,7,8,12,3,17,8);path(canvas,5,14,5,20,19,20,19,14);break;
            case "calendar":canvas.drawRoundRect(3,5,21,21,2,2,paint);line(canvas,7,3,7,7);line(canvas,17,3,17,7);line(canvas,3,9,21,9);circle(canvas,8,13,0.7f);circle(canvas,12,13,0.7f);circle(canvas,16,13,0.7f);circle(canvas,8,17,0.7f);circle(canvas,12,17,0.7f);break;
            case "fullscreen":path(canvas,9,3,3,3,3,9);path(canvas,15,3,21,3,21,9);path(canvas,3,15,3,21,9,21);path(canvas,21,15,21,21,15,21);break;
            case "cloudy":
                path(canvas,5,18,3,17,2,15,3,12,6,11);canvas.drawArc(6,5,16,15,180,180,false,paint);path(canvas,16,10,20,11,22,14,21,17,19,18,5,18);break;
            case "incandescent":circle(canvas,12,9,6);line(canvas,9,15,9,18);line(canvas,15,15,15,18);line(canvas,9,18,15,18);line(canvas,10,21,14,21);break;
            case "fluorescent":canvas.drawRoundRect(2,8,22,16,2,2,paint);line(canvas,5,8,5,16);line(canvas,19,8,19,16);break;
            default: // "auto", "font", and compact character modes
                paint.setStyle(Paint.Style.FILL);paint.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));paint.setTextSize(name.equals("font")?20:17);paint.setTextAlign(Paint.Align.CENTER);canvas.drawText(name.equals("font")?"A":"A",12,18,paint);
        }
        canvas.restoreToCount(save);
    }
    private void line(Canvas c,float x,float y,float xx,float yy){c.drawLine(x,y,xx,yy,paint);}
    private void circle(Canvas c,float x,float y,float r){c.drawCircle(x,y,r,paint);}
    private void path(Canvas c,float... xy){Path p=new Path();p.moveTo(xy[0],xy[1]);for(int i=2;i<xy.length;i+=2)p.lineTo(xy[i],xy[i+1]);c.drawPath(p,paint);}
    @Override public void setAlpha(int alpha){paint.setAlpha(alpha);}
    @Override public void setColorFilter(ColorFilter filter){paint.setColorFilter(filter);}
    @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    @Override public int getIntrinsicWidth(){return (int)(24*android.content.res.Resources.getSystem().getDisplayMetrics().density);}
    @Override public int getIntrinsicHeight(){return getIntrinsicWidth();}
}

