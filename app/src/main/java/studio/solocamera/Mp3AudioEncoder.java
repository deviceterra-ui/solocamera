package studio.solocamera;

import de.sciss.jump3r.mp3.*;
import de.sciss.jump3r.mpg.*;
import java.io.*;

/** Android adapter for Jump3r's LAME core; avoids desktop javax.sound dependencies. */
final class Mp3AudioEncoder implements AutoCloseable {
    private final Lame lame=new Lame();private final LameGlobalFlags flags;private final int channels;private final OutputStream output;
    Mp3AudioEncoder(int rate,int channels,OutputStream output)throws IOException{this.channels=channels;this.output=output;GainAnalysis ga=new GainAnalysis();BitStream bs=new BitStream();Presets presets=new Presets();QuantizePVT qp=new QuantizePVT();Quantize q=new Quantize();VBRTag vbr=new VBRTag();Version version=new Version();ID3Tag tags=new ID3Tag();Reservoir reservoir=new Reservoir();Takehiro takehiro=new Takehiro();MPGLib mpg=new MPGLib();de.sciss.jump3r.mpg.Interface decoder=new de.sciss.jump3r.mpg.Interface();Common common=new Common();lame.setModules(ga,bs,presets,qp,q,vbr,version,tags,mpg);bs.setModules(ga,mpg,version,vbr);tags.setModules(bs,version);presets.setModules(lame);q.setModules(bs,reservoir,qp,takehiro);qp.setModules(takehiro,reservoir,lame.enc.psy);reservoir.setModules(bs);takehiro.setModules(qp);vbr.setModules(lame,bs,version);mpg.setModules(decoder,common);decoder.setModules(vbr,common);flags=lame.lame_init();flags.num_channels=channels;flags.in_samplerate=rate;flags.out_samplerate=rate;flags.mode=channels==1?MPEGMode.MONO:MPEGMode.JOINT_STEREO;flags.brate=rate<32000?128:192;flags.quality=5;flags.write_id3tag_automatic=false;flags.bWriteVbrTag=false;flags.findReplayGain=false;tags.id3tag_init(flags);if(lame.lame_init_params(flags)<0)throw new IOException("MP3 encoder rejected the audio format");}
    void write(byte[] pcm)throws IOException{int frames=pcm.length/(channels*2);if(frames==0)return;int[] left=new int[frames],right=new int[frames];for(int i=0;i<frames;i++){int at=i*channels*2;left[i]=((short)((pcm[at]&255)|(pcm[at+1]<<8)))<<16;if(channels==2)right[i]=((short)((pcm[at+2]&255)|(pcm[at+3]<<8)))<<16;else right[i]=left[i];}byte[] encoded=new byte[(int)(1.25*frames)+7200];int count=lame.lame_encode_buffer_int(flags,left,right,frames,encoded,0,encoded.length);if(count<0)throw new IOException("MP3 encoding failed: "+count);output.write(encoded,0,count);}
    void finish()throws IOException{byte[] encoded=new byte[16384];int count=lame.lame_encode_flush(flags,encoded,0,encoded.length);if(count<0)throw new IOException("MP3 flush failed");output.write(encoded,0,count);}
    public void close(){lame.lame_close(flags);}
}
