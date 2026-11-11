package studio.solocamera;

import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.Switch;
import android.view.accessibility.AccessibilityNodeInfo;
import java.lang.reflect.*;
import java.io.*;

/** Real projection consent, synthetic media audio, GPU capture and saved-file checks. */
final class ScreenLiveChecks {
    private static Object field(Object value,String name)throws Exception{Field f=value.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(value);}
    private static void ui(Instrumentation host,Action action){Throwable[] error={null};host.runOnMainSync(()->{try{action.run();}catch(Throwable e){error[0]=e;}});if(error[0]!=null)throw new RuntimeException(error[0]);}
    interface Action{void run()throws Exception;}
    static void run(Instrumentation host,Bundle report)throws Exception{
        ScreenRecordingActivity activity=(ScreenRecordingActivity)host.startActivitySync(new Intent(host.getTargetContext(),ScreenRecordingActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        try{ui(host,()->{((Switch)field(activity,"live")).setChecked(true);((Switch)field(activity,"appAudio")).setChecked(true);((Switch)field(activity,"microphone")).setChecked(false);((Switch)field(activity,"face")).setChecked(false);((Switch)field(activity,"annotations")).setChecked(false);Method request=activity.getClass().getDeclaredMethod("requestRecording");request.setAccessible(true);request.invoke(activity);});
            long deadline=SystemClock.elapsedRealtime()+20000;boolean approved=false;
            while(SystemClock.elapsedRealtime()<deadline&&!ScreenRecordingService.running){Thread.sleep(400);AccessibilityNodeInfo root=host.getUiAutomation().getRootInActiveWindow();if(root!=null&&"com.android.systemui".contentEquals(root.getPackageName()))for(AccessibilityNodeInfo node:root.findAccessibilityNodeInfosByText("Start")){if("Start".contentEquals(node.getText())&&node.isClickable()){node.performAction(AccessibilityNodeInfo.ACTION_CLICK);approved=true;break;}}}
            if(!ScreenRecordingService.running||ScreenRecordingService.livePeer==null)throw new AssertionError("Live capture did not start; consent clicked="+approved+" error="+ScreenRecordingService.preferences(activity).getString("error",""));
            File credentials=new File(activity.getCacheDir(),"screen-live-qa-key");try(FileWriter writer=new FileWriter(credentials)){writer.write(ScreenRecordingService.livePeer.key);}
            long end=SystemClock.elapsedRealtime()+65000;int approvals=0;
            android.media.AudioTrack tone=new android.media.AudioTrack.Builder().setAudioAttributes(new android.media.AudioAttributes.Builder().setUsage(android.media.AudioAttributes.USAGE_MEDIA).setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC).build()).setAudioFormat(new android.media.AudioFormat.Builder().setEncoding(android.media.AudioFormat.ENCODING_PCM_16BIT).setSampleRate(48000).setChannelMask(android.media.AudioFormat.CHANNEL_OUT_MONO).build()).setBufferSizeInBytes(9600).setTransferMode(android.media.AudioTrack.MODE_STREAM).build();
            tone.play();short[] samples=new short[960];long cursor=0;try{while(SystemClock.elapsedRealtime()<end&&ScreenRecordingService.running){ScreenLivePeer peer=ScreenRecordingService.livePeer;if(peer!=null&&peer.approval!=null){peer.answer(true);approvals++;}for(int i=0;i<samples.length;i++)samples[i]=(short)(Math.sin(2*Math.PI*440*(cursor++)/48000)*3000);tone.write(samples,0,samples.length);}}finally{tone.stop();tone.release();credentials.delete();}
            activity.startService(new Intent(activity,ScreenRecordingService.class).setAction(ScreenRecordingService.STOP));deadline=SystemClock.elapsedRealtime()+15000;while((ScreenRecordingService.running||ScreenRecordingService.preferences(activity).contains("pendingUri"))&&SystemClock.elapsedRealtime()<deadline)Thread.sleep(200);
            String error=ScreenRecordingService.preferences(activity).getString("error","")+ScreenRecordingService.preferences(activity).getString("liveAudioError","");if(!error.isEmpty())throw new AssertionError(error);String saved=ScreenRecordingService.preferences(activity).getString("lastUri","");android.media.MediaMetadataRetriever media=new android.media.MediaMetadataRetriever();try{media.setDataSource(activity,android.net.Uri.parse(saved));if(!"yes".equals(media.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)))throw new AssertionError("Saved file missing audio");android.graphics.Bitmap frame=media.getFrameAtTime(5_000_000);if(frame==null)throw new AssertionError("Saved video not decodable");frame.recycle();report.putString("uri",saved);report.putString("checks","PASS real projection, live GPU capture, playback audio and saved A/V; Windows approvals="+approvals);}finally{media.release();}
        }finally{if(ScreenRecordingService.running)activity.startService(new Intent(activity,ScreenRecordingService.class).setAction(ScreenRecordingService.STOP));ui(host,activity::finish);}
    }
}
