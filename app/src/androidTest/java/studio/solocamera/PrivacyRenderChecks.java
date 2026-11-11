package studio.solocamera;

import android.app.Instrumentation;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import java.io.File;
import java.lang.reflect.Method;

/** Verifies exported privacy pixels without requiring an unlocked foreground activity. */
final class PrivacyRenderChecks {
    static void run(Instrumentation h,Bundle result)throws Exception {
        EditorFonts.init(h.getTargetContext());File dir=new File(h.getTargetContext().getCacheDir(),"privacy-render-qa");dir.mkdirs();
        Method make=CreatorExpansionChecks.class.getDeclaredMethod("colorVideo",Instrumentation.class,File.class,String.class,int.class);make.setAccessible(true);
        File video=(File)make.invoke(null,h,dir,"source",Color.CYAN);
        EditorProject p=new EditorProject();p.aspect=2;p.height=720;EditorProject.Clip clip=new EditorProject.Clip(video.getAbsolutePath(),"source",2400);clip.width=640;clip.height=360;p.clips.add(clip);
        EditorProject.Caption cover=EditorPrivacy.create(new RectF(.15f,.4f,.85f,.6f),500,1800,0);p.captions.add(cover);
        EditorProject.Caption title=new EditorProject.Caption();title.type="Text";title.at=0;title.end=2400;title.style=EditorTitlePresets.create(20,2400);title.style.titleMotion=0;title.style.text="PRIVATE NUMBER";p.captions.add(title);
        File output=new File(dir,"blackout.mp4");output.delete();Method render=CreatorExpansionChecks.class.getDeclaredMethod("render",Instrumentation.class,androidx.media3.transformer.Composition.class,File.class);render.setAccessible(true);render.invoke(null,h,EditorComposition.build(p),output);
        MediaMetadataRetriever media=new MediaMetadataRetriever();try{media.setDataSource(output.getAbsolutePath());for(long time:new long[]{250,1000,2100}){Bitmap frame=media.getFrameAtTime(time*1000,MediaMetadataRetriever.OPTION_CLOSEST);if(frame==null)throw new AssertionError("No frame at "+time);int color=frame.getPixel(frame.getWidth()/2,frame.getHeight()/2);if(time==1000){if(Color.red(color)>18||Color.green(color)>18||Color.blue(color)>18)throw new AssertionError("Private detail visible at "+time+": "+Integer.toHexString(color));}else if(Color.red(color)<18&&Color.green(color)<18&&Color.blue(color)<18)throw new AssertionError("Blackout leaked outside its range at "+time);frame.recycle();}}finally{media.release();}
        result.putString("checks","PASS opaque timed blackout above title in MP4 export");h.finish(Activity.RESULT_OK,result);
    }
}
