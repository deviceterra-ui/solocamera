package studio.solocamera;
import android.content.Context;
import android.media.*;
import android.net.Uri;
import java.io.*;
import java.util.UUID;
final class EditorAudio {
 static EditorProject.Audio importFile(Context context,Uri uri,String name)throws Exception{
  File dir=new File(context.getFilesDir(),"editor-audio");if(!dir.exists()&&!dir.mkdirs())throw new IOException("Cannot create audio storage");File file=new File(dir,UUID.randomUUID()+".audio");boolean valid=false;
  try{try(InputStream in=context.getContentResolver().openInputStream(uri);FileOutputStream out=new FileOutputStream(file)){if(in==null)throw new IOException("The file provider did not open this audio");byte[] buffer=new byte[65536];int n;long total=0;while((n=in.read(buffer))!=-1){total+=n;if(total>512L*1024*1024||dir.getUsableSpace()<20L*1024*1024)throw new IOException("Not enough storage for this audio import");out.write(buffer,0,n);}}
   long duration=0;boolean hasAudio=false;MediaExtractor extractor=new MediaExtractor();try{extractor.setDataSource(file.getAbsolutePath());for(int i=0;i<extractor.getTrackCount();i++){MediaFormat f=extractor.getTrackFormat(i);String mime=f.getString(MediaFormat.KEY_MIME);if(mime!=null&&mime.startsWith("audio/")){hasAudio=true;if(f.containsKey(MediaFormat.KEY_DURATION))duration=Math.max(duration,f.getLong(MediaFormat.KEY_DURATION)/1000);}}}finally{extractor.release();}
   if(!hasAudio)throw new IOException("No supported audio track found. Try MP3, WAV, M4A/AAC, OGG or FLAC.");
   if(duration<=0){MediaMetadataRetriever r=new MediaMetadataRetriever();try{r.setDataSource(file.getAbsolutePath());String value=r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);if(value!=null)duration=Long.parseLong(value);}finally{r.release();}}
   if(duration<200)throw new IOException("Audio duration could not be read or is shorter than 0.2 seconds");EditorProject.Audio a=new EditorProject.Audio();a.uri=file.getAbsolutePath();a.name=name;a.duration=a.end=duration;valid=true;return a;
  }finally{if(!valid)file.delete();}
 }
 static void trim(EditorProject.Audio a,long at,int edge){long local=Math.max(0,Math.min(a.length()-1,at-a.at));if(edge==2){a.start=0;a.end=a.duration;}else if(edge==0){a.start+=local;a.at+=local;}else a.end=a.start+Math.max(1,local);}
}
