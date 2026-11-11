package studio.solocamera;

import android.graphics.*;

/** Cached component plates; preview and export share the same staggered choreography. */
final class EditorTitleAnimation {
 private static final ThreadLocal<Integer> PART=new ThreadLocal<>();
 static void begin(Canvas canvas,int part){canvas.save();Integer selected=PART.get();if(selected!=null&&selected!=part)canvas.clipRect(0,0,0,0);}
 static void end(Canvas canvas){canvas.restore();}
 private final Bitmap[] layers=new Bitmap[5];
 private final Bitmap output;
 private final Canvas canvas;
 private final Paint paint=new Paint(3);
 private long last=-1;private Bitmap held;private EditorProfessionFx fx;
 EditorTitleAnimation(EditorProject.Clip clip,int width,int height){
  try{for(int i=0;i<layers.length;i++){PART.set(i);layers[i]=EditorText.bitmap(clip,width,height);}}finally{PART.remove();}
  output=Bitmap.createBitmap(layers[0].getWidth(),layers[0].getHeight(),Bitmap.Config.ARGB_8888);canvas=new Canvas(output);if(EditorProfessionTitles.profession(clip.titleDesign-1)){fx=new EditorProfessionFx(clip);held=Bitmap.createBitmap(output.getWidth(),output.getHeight(),Bitmap.Config.ARGB_8888);Canvas hold=new Canvas(held);for(Bitmap layer:layers)hold.drawBitmap(layer,0,0,null);}
 }
 Bitmap frame(long time,long duration){long t=Math.max(0,Math.min(duration,time));boolean settled=t>=780&&duration-t>=540;if(fx==null&&settled)t=780;else if(fx!=null&&t<duration)t=t/33*33;if(last==t)return output;last=t;output.eraseColor(Color.TRANSPARENT);
  if(fx!=null&&settled){canvas.drawBitmap(held,0,0,null);fx.draw(canvas,output.getWidth(),output.getHeight(),t,duration);return output;}for(int i=0;i<layers.length;i++){float enter=unit((t-i*90)/420f),exit=unit((duration-t-(4-i)*65)/280f);float a=enter*exit;if(a<=0)continue;float eased=1-(float)Math.pow(1-enter,3);canvas.save();paint.setAlpha(Math.round(255*a));float w=output.getWidth(),h=output.getHeight();
   if(i==0)canvas.scale(.82f+.18f*eased,1,w*.08f,h/2);else if(i==1){canvas.translate(-w*.065f*(1-eased),0);canvas.scale(.8f+.2f*eased,.8f+.2f*eased,w*.12f,h/2);}else{canvas.clipRect(0,0,w*eased,h);canvas.translate(0,h*.1f*(1-eased)+h*.04f*(1-exit));}
   canvas.drawBitmap(layers[i],0,0,paint);canvas.restore();
  }if(fx!=null)fx.draw(canvas,output.getWidth(),output.getHeight(),t,duration);return output;
 }
 private static float unit(float n){return Math.max(0,Math.min(1,n));}
 void release(){if(held!=null&&!held.isRecycled())held.recycle();for(Bitmap b:layers)if(b!=null&&!b.isRecycled())b.recycle();if(!output.isRecycled())output.recycle();}
}
