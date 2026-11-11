package studio.solocamera;

import android.content.Context;
import android.media.*;
import java.io.File;

/** Explicit foreground-only microphone capture for checks and voice-over. */
final class CreatorRecorder implements AutoCloseable {
    private MediaRecorder recorder;private File file;private long started;
    void start(Context c,int input)throws Exception{
        close();file=File.createTempFile("creator-voice-",".m4a",c.getCacheDir());
        try{recorder=new MediaRecorder();recorder.setAudioSource(MediaRecorder.AudioSource.MIC);recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);recorder.setAudioSamplingRate(48000);recorder.setAudioEncodingBitRate(128000);recorder.setOutputFile(file.getAbsolutePath());if(input!=0){AudioDeviceInfo chosen=null;for(AudioDeviceInfo d:c.getSystemService(AudioManager.class).getDevices(AudioManager.GET_DEVICES_INPUTS))if(d.getId()==input)chosen=d;if(chosen==null||!recorder.setPreferredDevice(chosen))throw new IllegalStateException("Selected microphone unavailable");}recorder.prepare();recorder.start();started=android.os.SystemClock.elapsedRealtime();if(input!=0&&(recorder.getRoutedDevice()==null||recorder.getRoutedDevice().getId()!=input))throw new IllegalStateException("Android routed to a different microphone");}catch(Exception e){close();throw e;}
    }
    int amplitude(){return recorder==null?0:recorder.getMaxAmplitude();}
    String route(){AudioDeviceInfo d=recorder==null?null:recorder.getRoutedDevice();return d==null?"System microphone":d.getProductName().toString();}
    long elapsed(){return started==0?0:android.os.SystemClock.elapsedRealtime()-started;}
    File finish()throws Exception{if(recorder==null)throw new IllegalStateException("No active recording");try{if(elapsed()<300)throw new IllegalStateException("Record a little longer");recorder.stop();recorder.release();recorder=null;File result=file;file=null;started=0;return result;}catch(Exception e){close();throw e;}}
    @Override public void close(){if(recorder!=null){try{recorder.stop();}catch(Exception ignored){}recorder.release();recorder=null;}if(file!=null){file.delete();file=null;}started=0;}
}
