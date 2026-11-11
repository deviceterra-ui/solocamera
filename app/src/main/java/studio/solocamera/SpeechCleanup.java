package studio.solocamera;
import java.util.Arrays;
/** Streaming voice DSP: rumble reduction, smooth expansion and compression. */
final class SpeechCleanup {
    private final double[] input,high,low,envelope,gain;
    private final double highAlpha,lowAlpha,attack,release,gainAttack,gainRelease;
    SpeechCleanup(int rate,int channels){if(rate<8000||channels<1||channels>8)throw new IllegalArgumentException("Unsupported PCM format");input=new double[channels];high=new double[channels];low=new double[channels];envelope=new double[channels];gain=new double[channels];highAlpha=Math.exp(-2*Math.PI*75/rate);lowAlpha=1-Math.exp(-2*Math.PI*Math.min(12000,rate*.4)/rate);attack=Math.exp(-1.0/(.006*rate));release=Math.exp(-1.0/(.120*rate));gainAttack=Math.exp(-1.0/(.008*rate));gainRelease=Math.exp(-1.0/(.160*rate));reset();}
    void reset(){Arrays.fill(input,0);Arrays.fill(high,0);Arrays.fill(low,0);Arrays.fill(envelope,0);Arrays.fill(gain,1);}
    float sample(float value,int channel,double strength){double dry=Float.isFinite(value)?Math.max(-1,Math.min(1,value)):0;strength=Double.isFinite(strength)?Math.max(0,Math.min(1,strength)):0;double filtered=highAlpha*(high[channel]+dry-input[channel]);input[channel]=dry;high[channel]=filtered;low[channel]+=lowAlpha*(filtered-low[channel]);double magnitude=Math.abs(low[channel]),coefficient=magnitude>envelope[channel]?attack:release;envelope[channel]=coefficient*envelope[channel]+(1-coefficient)*magnitude;double floor=.004+.006*strength,ratio=Math.max(0,Math.min(1,envelope[channel]/floor));double expansion=.08+.92*ratio*ratio;double peak=Math.max(envelope[channel],magnitude);double compression=peak>.22?Math.pow(.22/peak,.65*strength):1;double target=expansion*compression*(1+.18*strength);double smoothing=target>gain[channel]?gainAttack:gainRelease;gain[channel]=smoothing*gain[channel]+(1-smoothing)*target;double wet=low[channel]*gain[channel];return(float)Math.max(-.98,Math.min(.98,dry*(1-strength)+wet*strength));}
}
