package studio.solocamera;

import android.app.Instrumentation;
import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.content.ContentValues;
import android.net.Uri;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import androidx.media3.common.MimeTypes;
import androidx.media3.transformer.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Read-only export diagnosis of the user's saved edit; all output stays in cache. */
final class ExportReproChecks {
    static void run(Instrumentation h,Bundle args)throws Exception {
        if("true".equals(args.getString("verifyExisting"))){verify(h,args);return;}
        EditorFonts.init(h.getTargetContext());
        String id=args.getString("projectId");
        if(id==null||!id.matches("[a-fA-F0-9-]{36}"))throw new IllegalArgumentException("Valid projectId is required");
        EditorProject original=EditorProject.load(new File(h.getTargetContext().getFilesDir(),"edit-projects/"+id+".json"));
        EditorProject p=EditorProject.from(original.json().toString());
        int count=Integer.parseInt(args.getString("clipCount",Integer.toString(p.clips.size())));
        while(p.clips.size()>count)p.clips.remove(p.clips.size()-1);
        if("true".equals(args.getString("noAudio")))p.audios.clear();
        if("true".equals(args.getString("noCaptions")))p.captions.clear();
        if("true".equals(args.getString("normalSpeed")))for(EditorProject.Clip clip:p.clips)clip.speed=1;
        p.height=Integer.parseInt(args.getString("height","720"));
        File output=new File(h.getTargetContext().getCacheDir(),"export-repro.mp4");output.delete();
        Bundle result=new Bundle();result.putInt("clipCount",count);result.putLong("durationMs",p.duration());
        CountDownLatch done=new CountDownLatch(1);Throwable[] error={null};Transformer[] task={null};
        h.runOnMainSync(()->{task[0]=new Transformer.Builder(h.getTargetContext()).setMaxDelayBetweenMuxerSamplesMs(20000).setMuxerFactory(new InAppMp4Muxer.Factory()).setVideoMimeType(MimeTypes.VIDEO_H264).setAudioMimeType(MimeTypes.AUDIO_AAC).addListener(new Transformer.Listener(){
            public void onCompleted(Composition c,ExportResult r){done.countDown();}
            public void onError(Composition c,ExportResult r,ExportException e){error[0]=e;done.countDown();}
        }).build();task[0].start(EditorComposition.build(p),output.getAbsolutePath());});
        long deadline=SystemClock.elapsedRealtime()+300000;ProgressHolder progress=new ProgressHolder();int percent=-1;
        while(!done.await(1,TimeUnit.SECONDS)&&SystemClock.elapsedRealtime()<deadline){final int[] snapshot={-1};h.runOnMainSync(()->{if(task[0].getProgress(progress)==Transformer.PROGRESS_STATE_AVAILABLE)snapshot[0]=progress.progress;});percent=snapshot[0];}
        if(done.getCount()!=0)h.runOnMainSync(()->task[0].cancel());
        result.putInt("progress",percent);result.putLong("bytes",output.length());result.putString("error",error[0]==null?"":android.util.Log.getStackTraceString(error[0]));
        h.finish(error[0]==null&&done.getCount()==0?Activity.RESULT_OK:Activity.RESULT_CANCELED,result);
    }
    private static void verify(Instrumentation h,Bundle args)throws Exception {
        File file=new File(h.getTargetContext().getCacheDir(),"export-repro.mp4");
        if(!file.isFile()||file.length()<1000)throw new AssertionError("Rendered MP4 missing");
        MediaMetadataRetriever metadata=new MediaMetadataRetriever();MediaExtractor extractor=new MediaExtractor();
        Bundle result=new Bundle();
        try{
            metadata.setDataSource(file.getAbsolutePath());extractor.setDataSource(file.getAbsolutePath());
            long duration=Long.parseLong(metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            boolean video=false,audio=false;for(int i=0;i<extractor.getTrackCount();i++){MediaFormat track=extractor.getTrackFormat(i);String mime=track.getString(MediaFormat.KEY_MIME);video|=mime.startsWith("video/");audio|=mime.startsWith("audio/");}
            if(Math.abs(duration-153335)>1500||!video||!audio)throw new AssertionError("Incomplete output duration="+duration+" video="+video+" audio="+audio);
            android.graphics.Bitmap frame=metadata.getFrameAtTime((duration-500)*1000,MediaMetadataRetriever.OPTION_CLOSEST);
            if(frame==null)throw new AssertionError("Final frame does not decode");result.putInt("width",frame.getWidth());result.putInt("height",frame.getHeight());frame.recycle();
            result.putLong("durationMs",duration);result.putLong("bytes",file.length());result.putString("checks","PASS full-length video and audio tracks, final frame decode");
            if("true".equals(args.getString("publish"))){
                ContentValues values=new ContentValues();values.put(MediaStore.MediaColumns.DISPLAY_NAME,"story-export-repaired.mp4");values.put(MediaStore.MediaColumns.MIME_TYPE,"video/mp4");values.put(MediaStore.MediaColumns.RELATIVE_PATH,"Movies/SoloCamera/Exports");values.put(MediaStore.MediaColumns.IS_PENDING,1);values.put(MediaStore.Video.Media.DURATION,duration);values.put(MediaStore.Video.Media.WIDTH,result.getInt("width"));values.put(MediaStore.Video.Media.HEIGHT,result.getInt("height"));
                Uri uri=h.getTargetContext().getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,values);if(uri==null)throw new AssertionError("Could not create gallery export");
                try{try(FileInputStream in=new FileInputStream(file);OutputStream out=h.getTargetContext().getContentResolver().openOutputStream(uri,"w")){if(out==null)throw new AssertionError("Could not write gallery export");byte[] block=new byte[131072];for(int count;(count=in.read(block))!=-1;)out.write(block,0,count);}values.clear();values.put(MediaStore.MediaColumns.IS_PENDING,0);h.getTargetContext().getContentResolver().update(uri,values,null,null);result.putString("exportUri",uri.toString());}catch(Throwable e){h.getTargetContext().getContentResolver().delete(uri,null,null);throw e;}
            }
            h.finish(Activity.RESULT_OK,result);
        }finally{extractor.release();metadata.release();}
    }
}
