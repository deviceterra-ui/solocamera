package studio.solocamera;

import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** Decodes compact peak samples for drawing an audio clip's waveform. */
final class EditorWaveform {
    private static final int BINS=192;
    private EditorWaveform(){}

    static float[] decode(String path,long durationMs)throws Exception{return decode(path,durationMs,()->true);}
    static float[] decode(String path,long durationMs,java.util.function.BooleanSupplier keepGoing)throws Exception{
        MediaExtractor extractor=new MediaExtractor();MediaCodec codec=null;boolean started=false;float[] peaks=new float[BINS];
        try{
            extractor.setDataSource(path);int audioTrack=-1;MediaFormat format=null;
            for(int i=0;i<extractor.getTrackCount();i++){MediaFormat candidate=extractor.getTrackFormat(i);String mime=candidate.getString(MediaFormat.KEY_MIME);if(mime!=null&&mime.startsWith("audio/")){audioTrack=i;format=candidate;break;}}
            if(audioTrack<0||format==null)throw new IllegalArgumentException("No audio stream");
            long durationUs=format.containsKey(MediaFormat.KEY_DURATION)?format.getLong(MediaFormat.KEY_DURATION):Math.max(1,durationMs)*1000L;
            extractor.selectTrack(audioTrack);codec=MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME));codec.configure(format,null,null,0);codec.start();started=true;
            MediaCodec.BufferInfo info=new MediaCodec.BufferInfo();boolean inputDone=false,outputDone=false;int sampleRate=format.containsKey(MediaFormat.KEY_SAMPLE_RATE)?format.getInteger(MediaFormat.KEY_SAMPLE_RATE):48000;int channels=format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)?Math.max(1,format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)):1;long fallbackTime=0;
            while(!outputDone){if(Thread.currentThread().isInterrupted()||!keepGoing.getAsBoolean())throw new java.io.InterruptedIOException("Audio analysis paused; reopen the editor to retry");
                if(!inputDone){int input=codec.dequeueInputBuffer(10000);if(input>=0){ByteBuffer buffer=codec.getInputBuffer(input);if(buffer==null)continue;buffer.clear();int size=extractor.readSampleData(buffer,0);if(size<0){codec.queueInputBuffer(input,0,0,0,MediaCodec.BUFFER_FLAG_END_OF_STREAM);inputDone=true;}else{long pts=extractor.getSampleTime();codec.queueInputBuffer(input,0,size,Math.max(0,pts),0);extractor.advance();}}}
                int output=codec.dequeueOutputBuffer(info,10000);if(output>=0){ByteBuffer pcm=codec.getOutputBuffer(output);if(pcm!=null&&info.size>0){MediaFormat out=codec.getOutputFormat();if(out.containsKey(MediaFormat.KEY_SAMPLE_RATE))sampleRate=out.getInteger(MediaFormat.KEY_SAMPLE_RATE);if(out.containsKey(MediaFormat.KEY_CHANNEL_COUNT))channels=Math.max(1,out.getInteger(MediaFormat.KEY_CHANNEL_COUNT));int encoding=out.containsKey(MediaFormat.KEY_PCM_ENCODING)?out.getInteger(MediaFormat.KEY_PCM_ENCODING):AudioFormat.ENCODING_PCM_16BIT;pcm.position(info.offset);pcm.limit(info.offset+info.size);pcm.order(ByteOrder.LITTLE_ENDIAN);int bytesPerSample=encoding==AudioFormat.ENCODING_PCM_FLOAT?4:encoding==AudioFormat.ENCODING_PCM_8BIT?1:2;int frameBytes=bytesPerSample*channels;int frames=info.size/frameBytes;long pts=info.presentationTimeUs<0?fallbackTime:info.presentationTimeUs;
                        for(int frame=0;frame<frames;frame++){float magnitude=0;for(int ch=0;ch<channels;ch++){float value;if(bytesPerSample==4)value=Math.abs(pcm.getFloat());else if(bytesPerSample==1)value=Math.abs((pcm.get()&255)-128)/128f;else value=Math.abs(pcm.getShort()/32768f);magnitude+=value;}magnitude/=channels;long time=pts+(long)frame*1_000_000L/Math.max(1,sampleRate);int bin=(int)Math.min(BINS-1,Math.max(0,time*BINS/Math.max(1,durationUs)));peaks[bin]=Math.max(peaks[bin],magnitude);}fallbackTime=pts+(long)frames*1_000_000L/Math.max(1,sampleRate);
                    }codec.releaseOutputBuffer(output,false);if((info.flags&MediaCodec.BUFFER_FLAG_END_OF_STREAM)!=0)outputDone=true;}
                else if(output==MediaCodec.INFO_TRY_AGAIN_LATER&&inputDone)outputDone=true;
            }
            float max=0;for(float p:peaks)max=Math.max(max,p);if(max>0)for(int i=0;i<peaks.length;i++)peaks[i]=(float)Math.sqrt(peaks[i]/max);return peaks;
        }finally{if(codec!=null){if(started)try{codec.stop();}catch(Exception ignored){}codec.release();}extractor.release();}
    }
}
