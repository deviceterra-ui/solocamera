package studio.solocamera;
import android.graphics.*;
final class EditorBrandLogo {
 static void draw(Canvas canvas,EditorProject.Clip c,int w,int h){if(c.designLogo.isEmpty()||EditorSocialTitles.social(c.titleDesign-1))return;BitmapFactory.Options opts=new BitmapFactory.Options();opts.inSampleSize=4;Bitmap b=BitmapFactory.decodeFile(c.designLogo,opts);if(b==null)return;float box=Math.min(w*.12f,h*.18f),scale=box/Math.max(b.getWidth(),b.getHeight()),right=w*.94f,top=h*.07f;EditorTitleAnimation.begin(canvas,1);canvas.drawBitmap(b,null,new RectF(right-b.getWidth()*scale,top,right,top+b.getHeight()*scale),new Paint(3));EditorTitleAnimation.end(canvas);b.recycle();}
}
