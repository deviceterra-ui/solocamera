package studio.solocamera;

import androidx.media3.common.C;
import androidx.media3.common.audio.*;
import java.nio.ByteBuffer;

@androidx.annotation.OptIn(markerClass=androidx.media3.common.util.UnstableApi.class)
final class DuckingAudioProcessor extends BaseAudioProcessor {
    private final CreatorDucking envelope;private final long origin;private long frame,nextUpdate;private float gain=1;
    DuckingAudioProcessor(EditorProject project,long origin){envelope=new CreatorDucking(project);this.origin=origin;}
    @Override protected AudioFormat onConfigure(AudioFormat f)throws AudioProcessor.UnhandledAudioFormatException{if(f.encoding!=C.ENCODING_PCM_16BIT&&f.encoding!=C.ENCODING_PCM_FLOAT)throw new AudioProcessor.UnhandledAudioFormatException(f);return f;}
    @Override protected void onFlush(){frame=0;nextUpdate=0;gain=1;}
    @Override public void queueInput(ByteBuffer input){ByteBuffer out=replaceOutputBuffer(input.remaining());int bytes=inputAudioFormat.encoding==C.ENCODING_PCM_FLOAT?4:2;int channels=inputAudioFormat.channelCount;while(input.remaining()>=bytes*channels){if(frame>=nextUpdate){gain=(float)envelope.gain(origin+frame*1000/inputAudioFormat.sampleRate);nextUpdate=frame+Math.max(1,inputAudioFormat.sampleRate/200);}for(int c=0;c<channels;c++)if(bytes==4)out.putFloat(input.getFloat()*gain);else out.putShort((short)Math.round(input.getShort()*gain));frame++;}out.flip();}
}
