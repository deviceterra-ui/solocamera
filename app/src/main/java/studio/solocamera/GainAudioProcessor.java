package studio.solocamera;

import androidx.media3.common.C;
import androidx.media3.common.audio.BaseAudioProcessor;
import androidx.media3.common.audio.AudioProcessor;
import java.nio.ByteBuffer;

/** Offline clip/music gain. Does not claim hardware microphone gain or denoising. */
@androidx.annotation.OptIn(markerClass=androidx.media3.common.util.UnstableApi.class)
final class GainAudioProcessor extends BaseAudioProcessor {
    private final float gain;
    GainAudioProcessor(float gain){this.gain=gain;}
    @Override protected AudioFormat onConfigure(AudioFormat format)throws AudioProcessor.UnhandledAudioFormatException{
        if(format.encoding!=C.ENCODING_PCM_16BIT&&format.encoding!=C.ENCODING_PCM_FLOAT)throw new AudioProcessor.UnhandledAudioFormatException(format);
        return gain==1?AudioFormat.NOT_SET:format;
    }
    @Override public void queueInput(ByteBuffer input){
        ByteBuffer out=replaceOutputBuffer(input.remaining());
        if(inputAudioFormat.encoding==C.ENCODING_PCM_FLOAT)while(input.remaining()>=4)out.putFloat(Math.max(-1,Math.min(1,input.getFloat()*gain)));
        else while(input.remaining()>=2)out.putShort((short)Math.max(Short.MIN_VALUE,Math.min(Short.MAX_VALUE,Math.round(input.getShort()*gain))));
        out.flip();
    }
}
