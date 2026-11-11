package studio.solocamera;

import androidx.media3.common.C;
import androidx.media3.common.audio.*;
import java.nio.ByteBuffer;

/** Positive offset advances late audio; negative offset delays early audio. */
@androidx.annotation.OptIn(markerClass=androidx.media3.common.util.UnstableApi.class)
final class SyncAudioProcessor extends BaseAudioProcessor {
    private volatile int offset;private int configuredOffset,skip,pad,ringAt;private byte[] ring=new byte[0];
    SyncAudioProcessor(int offset){this.offset=offset;}
    void offset(int value){offset=Math.max(-1000,Math.min(1000,value));}
    @Override protected AudioFormat onConfigure(AudioFormat f)throws AudioProcessor.UnhandledAudioFormatException{if(f.encoding!=C.ENCODING_PCM_16BIT&&f.encoding!=C.ENCODING_PCM_FLOAT)throw new AudioProcessor.UnhandledAudioFormatException(f);return f;}
    @Override protected void onFlush(){configuredOffset=offset;int frameBytes=inputAudioFormat.channelCount*(inputAudioFormat.encoding==C.ENCODING_PCM_FLOAT?4:2);int count=(int)(Math.abs((long)offset)*inputAudioFormat.sampleRate/1000)*frameBytes;skip=offset>0?count:0;pad=0;ring=new byte[offset<0?count:0];ringAt=0;}
    @Override public void queueInput(ByteBuffer input){if(configuredOffset!=offset)onFlush();if(skip>0){int n=Math.min(skip,input.remaining());input.position(input.position()+n);skip-=n;pad+=n;}if(!input.hasRemaining())return;ByteBuffer out=replaceOutputBuffer(input.remaining());if(ring.length==0)out.put(input);else while(input.hasRemaining()){byte next=input.get();out.put(ring[ringAt]);ring[ringAt]=next;ringAt=(ringAt+1)%ring.length;}out.flip();}
    @Override protected void onQueueEndOfStream(){if(pad>0){ByteBuffer out=replaceOutputBuffer(pad);for(int i=0;i<pad;i++)out.put((byte)0);out.flip();pad=0;}}
}
