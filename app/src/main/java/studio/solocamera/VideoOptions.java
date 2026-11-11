package studio.solocamera;

import android.graphics.ImageFormat;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.*;
import android.util.*;
import org.json.*;
import java.util.*;

/** Intersect camera timings, regular-session AE rates, preview sizes and hardware AVC encoding. */
final class VideoOptions {
    static JSONArray discover(CameraCharacteristics chars)throws Exception{return discover(chars,false);}
    static JSONArray discover(CameraCharacteristics chars,boolean livePreview)throws Exception{
        StreamConfigurationMap map=chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        JSONArray result=new JSONArray();if(map==null)return result;
        Size[] sizes=map.getOutputSizes(MediaRecorder.class),previews=map.getOutputSizes(ImageFormat.YUV_420_888);
        if(sizes==null||previews==null)return result;
        ArrayList<Size> sorted=new ArrayList<>(Arrays.asList(sizes));sorted.sort(Comparator.comparingLong(s->(long)s.getWidth()*s.getHeight()));
        Range<Integer>[] rates=chars.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
        for(Size size:sorted)for(int fps:new int[]{30,60}){
            if(size.getWidth()<640||size.getHeight()<360)continue;
            boolean rate=false;if(rates!=null)for(Range<Integer> r:rates)if(r.getUpper()==fps)rate=true;if(!rate)continue;
            long duration=map.getOutputMinFrameDuration(MediaRecorder.class,size);
            if((fps>30&&duration==0)||duration>1_000_000_000L/fps+1000)continue;
            Size preview=null;
            for(Size p:previews){
                if(p.getWidth()>960||p.getHeight()>720||Math.abs((double)p.getWidth()/p.getHeight()-(double)size.getWidth()/size.getHeight())>.001)continue;
                long time=map.getOutputMinFrameDuration(ImageFormat.YUV_420_888,p);
                if((fps>30&&time==0)||time>1_000_000_000L/fps+1000)continue;
                if(preview==null||(livePreview&&p.getWidth()<=640&&preview.getWidth()>640)||((!livePreview||(p.getWidth()<=640)==(preview.getWidth()<=640))&&(long)p.getWidth()*p.getHeight()>(long)preview.getWidth()*preview.getHeight()))preview=p;
            }
            if(preview==null)continue;
            Range<Integer> bitrate=null;
            for(MediaCodecInfo codec:new MediaCodecList(MediaCodecList.REGULAR_CODECS).getCodecInfos()){
                if(!codec.isEncoder()||!codec.isHardwareAccelerated())continue;
                try{MediaCodecInfo.CodecCapabilities caps=codec.getCapabilitiesForType("video/avc");boolean surface=false;for(int color:caps.colorFormats)if(color==MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)surface=true;
                    if(surface&&caps.getVideoCapabilities().areSizeAndRateSupported(size.getWidth(),size.getHeight(),fps)){bitrate=caps.getVideoCapabilities().getBitrateRange();break;}
                }catch(IllegalArgumentException ignored){}
            }
            if(bitrate==null)continue;
            JSONObject option=new JSONObject();option.put("id",size.getWidth()+"x"+size.getHeight()+"@"+fps);option.put("width",size.getWidth());option.put("height",size.getHeight());option.put("fps",fps);
            option.put("previewWidth",preview.getWidth());option.put("previewHeight",preview.getHeight());option.put("bitrateMin",bitrate.getLower());option.put("bitrateMax",bitrate.getUpper());
            result.put(option);
        }
        return result;
    }
    /** Normal-speed recording rates; arbitrary rates require manual sensor timing. */
    static JSONArray frameRates(CameraCharacteristics chars,JSONObject option)throws Exception{
        JSONArray result=new JSONArray();int width=option.getInt("width"),height=option.getInt("height"),ceiling=option.getInt("fps");
        int[] capabilities=chars.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
        boolean manual=false;if(capabilities!=null)for(int c:capabilities)if(c==CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR)manual=true;
        Long longest=chars.get(CameraCharacteristics.SENSOR_INFO_MAX_FRAME_DURATION);
        Range<Integer>[] ae=chars.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
        if(ae!=null)for(Range<Integer> range:ae)ceiling=Math.max(ceiling,Math.min(60,range.getUpper()));
        StreamConfigurationMap map=chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        long recordTime=map.getOutputMinFrameDuration(MediaRecorder.class,new Size(width,height));
        long previewTime=map.getOutputMinFrameDuration(ImageFormat.YUV_420_888,new Size(option.getInt("previewWidth"),option.getInt("previewHeight")));

        ArrayList<MediaCodecInfo.VideoCapabilities> encoders=new ArrayList<>();
        for(MediaCodecInfo codec:new MediaCodecList(MediaCodecList.REGULAR_CODECS).getCodecInfos())if(codec.isEncoder()&&codec.isHardwareAccelerated())try{
            MediaCodecInfo.CodecCapabilities caps=codec.getCapabilitiesForType("video/avc");
            for(int color:caps.colorFormats)if(color==MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface){encoders.add(caps.getVideoCapabilities());break;}
        }catch(IllegalArgumentException ignored){}
        for(int fps=10;fps<=ceiling;fps++){
            long duration=1_000_000_000L/fps;if(recordTime>duration+1000||previewTime>duration+1000||fps>30&&(recordTime==0||previewTime==0))continue;
            boolean automatic=false;if(ae!=null)for(Range<Integer> range:ae)if(range.getUpper()==fps)automatic=true;
            if(!FrameRatePolicy.allowed(fps,ceiling,automatic,manual,longest==null?0:longest))continue;
            boolean encoded=false;for(MediaCodecInfo.VideoCapabilities caps:encoders)if(caps.areSizeAndRateSupported(width,height,fps)){encoded=true;break;}
            if(encoded)result.put(fps);
        }
        return result;
    }

}

