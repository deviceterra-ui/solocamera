package studio.solocamera;
import android.graphics.*;
import android.text.*;
final class EditorText {
 static Bitmap bitmap(EditorProject.Clip c,int width,int height){
  Bitmap raw;
  if(c.privacyBlackout)return EditorPrivacy.bitmap(c,width,height);if(c.titleDesign>0&&c.imageUri.isEmpty()){raw=EditorTitleGraphics.bitmap(c,width,height);
  }else if(!c.imageUri.isEmpty()){
   BitmapFactory.Options opts=new BitmapFactory.Options();opts.inJustDecodeBounds=true;BitmapFactory.decodeFile(c.imageUri,opts);opts.inSampleSize=1;while(opts.outWidth/opts.inSampleSize>2048||opts.outHeight/opts.inSampleSize>2048)opts.inSampleSize*=2;opts.inJustDecodeBounds=false;Bitmap source=BitmapFactory.decodeFile(c.imageUri,opts);if(source==null)throw new IllegalArgumentException("Image unavailable");float scale=c.fillFrame?Math.max(width/(float)source.getWidth(),height/(float)source.getHeight()):Math.min(width*.7f/source.getWidth(),height*.7f/source.getHeight());raw=Bitmap.createScaledBitmap(source,Math.max(1,Math.round(source.getWidth()*scale)),Math.max(1,Math.round(source.getHeight()*scale)),true);
  }else{
  CharSequence captionText=EditorCaptionStyles.text(c);TextPaint p=new TextPaint(3);p.setColor(c.textColor);p.setTypeface(EditorFonts.face(c.font,c.bold?(c.italic?Typeface.BOLD_ITALIC:Typeface.BOLD):(c.italic?Typeface.ITALIC:Typeface.NORMAL)));float unit=width/1080f;p.setTextSize(c.textSize*unit);
  int pad=Math.max(2,(int)(((c.shadowEnabled?c.shadowBlur:0)+c.backgroundPadding+Math.max(Math.abs(c.shadowX),Math.abs(c.shadowY))+c.outline+4)*unit));int w=Math.max(1,Math.min((int)(width*.85),(int)Math.ceil(Layout.getDesiredWidth(captionText,p))));
  StaticLayout layout=StaticLayout.Builder.obtain(captionText,0,captionText.length(),p,w).setAlignment(c.alignment==0?Layout.Alignment.ALIGN_NORMAL:c.alignment==2?Layout.Alignment.ALIGN_OPPOSITE:Layout.Alignment.ALIGN_CENTER).setLineSpacing(0,(float)c.lineSpacing).setIncludePad(true).build();
  raw=Bitmap.createBitmap(w+pad*2,Math.max(1,layout.getHeight()+pad*2),Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(raw);if(c.backgroundEnabled){Paint bg=new Paint(3);bg.setColor(c.textBackground);float inset=Math.max(0,pad-(float)c.backgroundPadding*unit);canvas.drawRoundRect(inset,inset,raw.getWidth()-inset,raw.getHeight()-inset,(float)c.backgroundRadius*unit,(float)c.backgroundRadius*unit,bg);}canvas.translate(pad,pad);
  if(c.shadowEnabled)p.setShadowLayer(Math.max(.1f,(float)c.shadowBlur*unit),(float)c.shadowX*unit,(float)c.shadowY*unit,c.shadowColor);
  if(c.outline>0){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth((float)c.outline*unit*2);p.setColor(c.outlineColor);if(c.captionStyle>0){StaticLayout.Builder.obtain(captionText.toString(),0,captionText.length(),p,w).setAlignment(layout.getAlignment()).setLineSpacing(0,(float)c.lineSpacing).setIncludePad(true).build().draw(canvas);}else layout.draw(canvas);p.setStyle(Paint.Style.FILL);p.setColor(c.textColor);}layout.draw(canvas);
  }
  if(c.titleDesign>0&&!c.designLogo.isEmpty())EditorBrandLogo.draw(new Canvas(raw),c,raw.getWidth(),raw.getHeight());
  int left=(int)(raw.getWidth()*c.cropX),top=(int)(raw.getHeight()*c.cropY);Bitmap out=Bitmap.createBitmap(raw,left,top,Math.max(1,raw.getWidth()-left*2),Math.max(1,raw.getHeight()-top*2));
  if(c.mask==0)return out;out=out.copy(Bitmap.Config.ARGB_8888,true);int rw=out.getWidth(),rh=out.getHeight();int[] pixels=new int[rw*rh];out.getPixels(pixels,0,rw,0,0,rw,rh);for(int y=0;y<rh;y++)for(int x=0;x<rw;x++){double alpha=EditorMask.alpha((x+.5)*2/rw-1,(y+.5)*2/rh-1,c);int i=y*rw+x;pixels[i]=(pixels[i]&0xffffff)|((int)((pixels[i]>>>24)*alpha)<<24);}out.setPixels(pixels,0,rw,0,0,rw,rh);return out;
 }
}
