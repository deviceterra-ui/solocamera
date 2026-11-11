package studio.solocamera;
import android.graphics.*;
/** Vector teaching graphics, editable and animated through the existing title renderer. */
final class EditorTutorialTitles {
 static final String[] NAMES={"Arrow callout","Focus circle","Focus rectangle","Step 1","Step 2","Step 3","Keyboard shortcut","Tap here","Zoom detail","Tip banner","Warning note","Checklist"};
 static void configure(EditorProject.Clip c,int id,long duration){c.text=new String[]{"Look here","Focus on this","Important detail","STEP 01","STEP 02","STEP 03","Ctrl + S","Tap here","Closer look","PRO TIP","PLEASE NOTE","Your checklist"}[id-76];c.designSubtitle=id==87?"First action  /  Second action  /  Next step":"Edit this explanation";c.designEyebrow="";c.backgroundEnabled=id>=79;if(!c.backgroundEnabled){c.textColor=0xffffffff;c.shadowEnabled=true;c.shadowBlur=3;c.shadowX=c.shadowY=1;}c.textY=0;c.textSize=56;c.alignment=0;EditorTitlePresets.animate(c,duration,1);}
 static Bitmap bitmap(EditorProject.Clip c,int width,int height){int kind=c.titleDesign-77;float unit=width/1080f;int w=Math.max(2,(int)(width*.84f)),h=Math.max(2,Math.min((int)(height*.55f),(int)(320*unit)));Bitmap b=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);Canvas a=new Canvas(b);Paint p=new Paint(3);float r=16*unit;
  EditorTitleAnimation.begin(a,0);if(c.backgroundEnabled){p.setColor(c.textBackground);a.drawRoundRect(0,0,w,h,r,r,p);}EditorTitleAnimation.end(a);
  EditorTitleAnimation.begin(a,1);p.setColor(c.designAccent);p.setStrokeWidth(8*unit);p.setStrokeCap(Paint.Cap.ROUND);p.setStyle(Paint.Style.STROKE);float cx=w*.16f,cy=h*.5f,rad=Math.min(w*.12f,h*.35f);
  if(kind==0){a.drawLine(w*.04f,h*.7f,w*.25f,h*.3f,p);a.drawLine(w*.25f,h*.3f,w*.14f,h*.31f,p);a.drawLine(w*.25f,h*.3f,w*.23f,h*.52f,p);}else if(kind==1)a.drawCircle(cx,cy,rad,p);else if(kind==2||kind==8)a.drawRoundRect(w*.04f,h*.2f,w*.28f,h*.8f,r,r,p);else if(kind>=3&&kind<=5){p.setStyle(Paint.Style.FILL);a.drawCircle(cx,cy,rad,p);p.setColor(c.textBackground);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(rad);p.setTypeface(Typeface.DEFAULT_BOLD);a.drawText(""+(kind-2),cx,cy+rad*.35f,p);}else if(kind==7){a.drawCircle(cx,cy,rad*.55f,p);a.drawCircle(cx,cy,rad,p);}else{a.drawLine(w*.05f,h*.15f,w*.05f,h*.85f,p);}EditorTitleAnimation.end(a);
  float left=kind<=5||kind==7||kind==8?w*.34f:w*.10f,available=w-left-w*.06f;
  EditorTitleAnimation.begin(a,3);float after=EditorTitleGraphics.draw(a,c,c.text,left,h*.23f,available,c.textSize*unit,h*.31f,c.font,c.bold,c.textColor,c.alignment);EditorTitleAnimation.end(a);
  EditorTitleAnimation.begin(a,4);EditorTitleGraphics.draw(a,c,c.designSubtitle,left,Math.max(h*.60f,after+8*unit),available,c.textSize*unit*.43f,h*.30f,c.font,false,c.textColor,c.alignment);EditorTitleAnimation.end(a);return b;
 }
}
