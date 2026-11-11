package studio.solocamera;
import androidx.media3.common.C;
import androidx.media3.common.audio.BaseAudioProcessor;
import androidx.media3.common.audio.AudioProcessor;
import java.nio.ByteBuffer;
/** Same offline voice cleanup for preview and exported mixes. */
@androidx.annotation.OptIn(markerClass=androidx.media3.common.util.UnstableApi.class)
final class VoiceEnhancementAudioProcessor extends BaseAudioProcessor {
    private volatile double strength=.6;private SpeechCleanup cleanup;private int channel;
    VoiceEnhancementAudioProcessor(){}
    VoiceEnhancementAudioProcessor(double amount){strength(amount);}
    void strength(double amount){strength=Double.isFinite(amount)?Math.max(0,Math.min(1,amount)):0;}
    @Override protected AudioFormat onConfigure(AudioFormat format)throws AudioProcessor.UnhandledAudioFormatException{if((format.encoding!=C.ENCODING_PCM_16BIT&&format.encoding!=C.ENCODING_PCM_FLOAT)||format.channelCount>8)throw new AudioProcessor.UnhandledAudioFormatException(format);cleanup=new SpeechCleanup(format.sampleRate,format.channelCount);return format;}
    @Override protected void onFlush(){channel=0;if(cleanup!=null)cleanup.reset();}
    @Override protected void onReset(){cleanup=null;channel=0;}
    @Override public void queueInput(ByteBuffer input){ByteBuffer out=replaceOutputBuffer(input.remaining());int bytes=inputAudioFormat.encoding==C.ENCODING_PCM_FLOAT?4:2,channels=inputAudioFormat.channelCount;double amount=strength;while(input.remaining()>=bytes){float dry=bytes==4?input.getFloat():input.getShort()/32768f;float value=amount==0?dry:cleanup.sample(dry,channel,amount);if(bytes==4)out.putFloat(value);else out.putShort((short)Math.max(-32768,Math.min(32767,Math.round(value*32768))));channel=(channel+1)%channels;}out.flip();}
}
