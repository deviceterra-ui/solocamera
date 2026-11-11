package studio.solocamera;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.RggbChannelVector;
import android.hardware.camera2.params.ColorSpaceTransform;
import android.media.*;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.util.*;
import android.view.Surface;
import org.json.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

/** Foreground-only capture engine. All camera mutations are serialized on one handler. */
public final class CameraEngine {
    public interface Listener { void state(JSONObject state); void frame(byte[] jpeg,int rotation); void error(String error); }
    private final Context context; private final Listener listener;
    private final CameraManager manager; private final HandlerThread thread=new HandlerThread("solo-camera");
    private final Handler handler;
    private CameraDevice camera; private CameraCaptureSession session; private volatile ImageReader reader;
    private final HandlerThread previewThread=new HandlerThread("solo-preview");private final Handler previewHandler;
    private int audioInputId,recordingRouteId;private boolean voiceProcessing;private int look;private double lookStrength=70;private JSONArray fpsRates=new JSONArray();private String actualAudioRoute="Not recording";
    private CameraCharacteristics chars; private MediaRecorder recorder; private Uri recordingUri; private ParcelFileDescriptor recordingDescriptor;
    private Size videoSize; private volatile boolean closed;private volatile boolean livePreview;
    private final Map<String,String> lensFormats=new HashMap<>();
    private JSONArray videoOptions=new JSONArray();private String videoOption="";private int videoFps=30,videoBitrate=8_000_000,openGeneration;private boolean highQuality=true;
    private long lastSensorTimestamp;private double sensorFps;
    private int antiBanding=3;private boolean recording,busy; private long started,lastFrame,lastMetadataStatus;
    private String recordingSyncKey="";private String cameraId,notice="Ready"; private int iso=400,wb=1,deviceDegrees,frameRotation;
    private long shutter=20_000_000; private float focus=-1,zoom=1,aperture=0; private int ev=0;
    private boolean wbManual;private double wbFine;
    private RggbChannelVector wbBaseline;private ColorSpaceTransform wbTransform;
    private boolean manual,torch,aeLock,awbLock,tapFocus,tapTriggerPending; private int stabilization; private MeteringRectangle metering; private int actualIso; private long actualShutter;private float actualAperture;
    private final LinkedHashMap<String,Boolean> commandIds=new LinkedHashMap<>();
    public CameraEngine(Context context,Listener listener) {
        this.context=context;this.listener=listener;livePreview=context.getSharedPreferences("camera-preview",Context.MODE_PRIVATE).getBoolean("live",true);manager=(CameraManager)context.getSystemService(Context.CAMERA_SERVICE);
        thread.start();handler=new Handler(thread.getLooper());previewThread.start();previewHandler=new Handler(previewThread.getLooper());
    }
    private boolean manualSupported() { int[] caps=chars.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES); return caps!=null&&contains(caps,CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR); }
    private boolean fineWbSupported(){return chars!=null&&contains(chars.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES),CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_POST_PROCESSING)&&contains(chars.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES),CaptureRequest.CONTROL_AWB_MODE_OFF)&&chars.getAvailableCaptureRequestKeys().containsAll(Arrays.asList(CaptureRequest.COLOR_CORRECTION_GAINS,CaptureRequest.COLOR_CORRECTION_TRANSFORM,CaptureRequest.COLOR_CORRECTION_MODE));}
    private boolean lookSupported(){Integer points=chars.get(CameraCharacteristics.TONEMAP_MAX_CURVE_POINTS);return points!=null&&points>=4&&contains(chars.get(CameraCharacteristics.TONEMAP_AVAILABLE_TONE_MAP_MODES),CaptureRequest.TONEMAP_MODE_CONTRAST_CURVE)&&chars.getAvailableCaptureRequestKeys().contains(CaptureRequest.TONEMAP_CURVE);}
    private boolean fpsNeedsManual(int fps){Range<Integer>[] rates=chars.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);if(rates!=null)for(Range<Integer> range:rates)if(range.getUpper()==fps)return false;return true;}
    private JSONObject selectedVideo()throws Exception{for(int i=0;i<videoOptions.length();i++){JSONObject o=videoOptions.getJSONObject(i);if(o.getString("id").equals(videoOption))return o;}throw new Exception("Video format unavailable");}
    private static boolean contains(int[] values,int v) { if(values!=null) for(int x:values) if(x==v)return true; return false; }
    public void open() { handler.post(()->{
        try {
            String[] ids=manager.getCameraIdList(); if(ids.length==0)throw new Exception("No camera available");
            String selected=ids[0]; for(String id:ids) {Integer facing=manager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING);if(facing!=null&&facing==CameraCharacteristics.LENS_FACING_BACK){selected=id;break;}}
            openCamera(selected);
        } catch(Exception e){fail(e);}
    }); }
    private void openCamera(String id) throws Exception {
        if(closed)return;
        if(context.checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED) throw new Exception("Camera permission required");
        lastSensorTimestamp=0;lastFrame=0;sensorFps=0;cameraId=id;chars=manager.getCameraCharacteristics(id);
        StreamConfigurationMap map=chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if(map==null) throw new Exception("No supported camera streams");
        videoOptions=VideoOptions.discover(chars,livePreview);JSONObject selected=null;String preferred=lensFormats.getOrDefault(id,videoOption);
        for(int i=0;i<videoOptions.length();i++){JSONObject option=videoOptions.getJSONObject(i);if(option.getString("id").equals(preferred))selected=option;}
        if(selected==null)for(int i=0;i<videoOptions.length();i++){JSONObject option=videoOptions.getJSONObject(i);if(option.getInt("fps")==30&&option.getInt("width")<=1920&&option.getInt("height")<=1080)selected=option;}
        if(selected==null&&videoOptions.length()>0)selected=videoOptions.getJSONObject(0);
        if(selected==null)throw new Exception("No compatible camera / hardware video encoder format available");
        videoOption=selected.getString("id");lensFormats.put(id,videoOption);videoFps=selected.getInt("fps");videoSize=new Size(selected.getInt("width"),selected.getInt("height"));fpsRates=VideoOptions.frameRates(chars,selected);if(!lookSupported())look=0;updateBitrate(selected);clampExposure();
        Size preview=new Size(selected.getInt("previewWidth"),selected.getInt("previewHeight"));
        reader=ImageReader.newInstance(preview.getWidth(),preview.getHeight(),ImageFormat.YUV_420_888,3);
        reader.setOnImageAvailableListener(r->{
            try(Image image=r.acquireLatestImage()) {
                if(image==null||closed||r!=reader)return;
                long now=image.getTimestamp();if(lastFrame!=0&&now>lastFrame&&now-lastFrame<28_000_000L)return;lastFrame=now;
                byte[] nv21=toNv21(image);ByteArrayOutputStream output=new ByteArrayOutputStream();
                new YuvImage(nv21,ImageFormat.NV21,image.getWidth(),image.getHeight(),null).compressToJpeg(new Rect(0,0,image.getWidth(),image.getHeight()),livePreview?55:70,output);
                listener.frame(output.toByteArray(),frameRotation);
            } catch(Exception e){if(!closed&&r==reader)listener.error("Preview: "+e.getMessage());}
        },previewHandler);
        frameRotation=rotation();
        final int generation=++openGeneration;busy=true;
        manager.openCamera(id,new CameraDevice.StateCallback(){
            @Override public void onOpened(CameraDevice device){if(closed||generation!=openGeneration){device.close();return;}camera=device;configure(false);}
            @Override public void onDisconnected(CameraDevice device){device.close();if(generation!=openGeneration||closed)return;camera=null;recover(new Exception("Camera disconnected"));}
            @Override public void onError(CameraDevice device,int error){device.close();if(generation!=openGeneration||closed)return;camera=null;recover(new Exception("Camera error "+error));}
        },handler);
    }
    static byte[] toNv21(Image image) {
        int w=image.getWidth(),h=image.getHeight();byte[] result=new byte[w*h*3/2];Image.Plane[] planes=image.getPlanes();
        for(int p=0;p<3;p++) {
            ByteBuffer buf=planes[p].getBuffer();int base=buf.position(),rs=planes[p].getRowStride(),ps=planes[p].getPixelStride();
            int pw=p==0?w:w/2,ph=p==0?h:h/2;
            if(p==0&&ps==1){for(int y=0;y<h;y++){buf.position(base+y*rs);buf.get(result,y*w,w);}continue;}
            for(int y=0;y<ph;y++) for(int x=0;x<pw;x++) {
                int dest=p==0?y*w+x:w*h+(y*pw+x)*2+(p==1?1:0);
                result[dest]=buf.get(base+y*rs+x*ps);
            }
        } return result;
    }
    private int rotation() {
        Integer sensor=chars.get(CameraCharacteristics.SENSOR_ORIENTATION),facing=chars.get(CameraCharacteristics.LENS_FACING);
        int s=sensor==null?0:sensor;
        return (s+(facing!=null&&facing==CameraCharacteristics.LENS_FACING_FRONT?deviceDegrees:-deviceDegrees)+360)%360;
    }
    public void orientation(int displayRotation) { handler.post(()->{deviceDegrees=displayRotation*90;if(chars!=null)frameRotation=rotation();}); }
    private void configure(boolean start) {
        if(closed||camera==null)return;busy=true;publish();
        final CameraDevice configuring=camera;final int generation=openGeneration;
        try {
            if(session!=null){session.close();session=null;}
            ArrayList<Surface> surfaces=new ArrayList<>();surfaces.add(reader.getSurface());if(start)surfaces.add(recorder.getSurface());
            camera.createCaptureSession(surfaces,new CameraCaptureSession.StateCallback(){
                @Override public void onConfigured(CameraCaptureSession configured) {
                    if(closed||camera!=configuring||generation!=openGeneration){configured.close();return;}session=configured;
                    try {
                        apply(start);
                        if(start){recorder.start();recording=true;AudioDeviceInfo route=recorder.getRoutedDevice();if(route!=null){actualAudioRoute=audioName(route);recordingSyncKey=route.getType()+":"+route.getProductName();recordingRouteId=route.getId();if(audioInputId!=0&&recordingRouteId!=audioInputId)throw new Exception("Android routed to a different microphone; recording stopped");}started=SystemClock.elapsedRealtime();notice="Recording on camera phone";}
                        busy=false;publish();
                    } catch(Exception e){recover(e);if(start&&!closed&&camera!=null)configure(false);}
                }
                @Override public void onConfigureFailed(CameraCaptureSession failed){failed.close();if(generation!=openGeneration||closed)return;recover(new Exception("Device rejected this preview / recording combination. Try a lower video format."));if(start&&!closed&&camera!=null)configure(false);}
            },handler);
        } catch(Exception e){recover(e);}
    }
    private void apply(boolean includeRecorder) throws Exception {
        if(session==null||camera==null)return;
        CaptureRequest.Builder b=camera.createCaptureRequest(includeRecorder?CameraDevice.TEMPLATE_RECORD:CameraDevice.TEMPLATE_PREVIEW);
        b.addTarget(reader.getSurface());if(includeRecorder)b.addTarget(recorder.getSurface());
        b.set(CaptureRequest.CONTROL_MODE,CaptureRequest.CONTROL_MODE_AUTO);
        if(manual&&manualSupported()) {
            b.set(CaptureRequest.CONTROL_AE_MODE,CaptureRequest.CONTROL_AE_MODE_OFF);
            b.set(CaptureRequest.SENSOR_SENSITIVITY,iso);b.set(CaptureRequest.SENSOR_EXPOSURE_TIME,shutter);b.set(CaptureRequest.SENSOR_FRAME_DURATION,1_000_000_000L/videoFps);
        } else {b.set(CaptureRequest.CONTROL_AE_MODE,CaptureRequest.CONTROL_AE_MODE_ON);b.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION,ev);}
        if(contains(chars.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES),antiBanding))b.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE,antiBanding);
        if(Boolean.TRUE.equals(chars.get(CameraCharacteristics.CONTROL_AE_LOCK_AVAILABLE)))b.set(CaptureRequest.CONTROL_AE_LOCK,aeLock);
        if(Boolean.TRUE.equals(chars.get(CameraCharacteristics.CONTROL_AWB_LOCK_AVAILABLE)))b.set(CaptureRequest.CONTROL_AWB_LOCK,awbLock);
        if(focus>=0) {b.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_OFF);b.set(CaptureRequest.LENS_FOCUS_DISTANCE,focus);}
        else {
            int[] modes=chars.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
            b.set(CaptureRequest.CONTROL_AF_MODE,tapFocus?CaptureRequest.CONTROL_AF_MODE_AUTO:contains(modes,CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO)?CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO:CaptureRequest.CONTROL_AF_MODE_OFF);
            if(tapFocus&&metering!=null)b.set(CaptureRequest.CONTROL_AF_REGIONS,new MeteringRectangle[]{metering});
        }
        b.set(CaptureRequest.CONTROL_AWB_MODE,wbManual?CaptureRequest.CONTROL_AWB_MODE_OFF:wb);
        if(wbManual){
            float[] gains=WhiteBalanceFine.gains(wbBaseline.getRed(),wbBaseline.getGreenEven(),wbBaseline.getGreenOdd(),wbBaseline.getBlue(),wbFine);
            b.set(CaptureRequest.COLOR_CORRECTION_MODE,CaptureRequest.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX);
            b.set(CaptureRequest.COLOR_CORRECTION_GAINS,new RggbChannelVector(gains[0],gains[1],gains[2],gains[3]));
            b.set(CaptureRequest.COLOR_CORRECTION_TRANSFORM,wbTransform);
        }
        if(look>0&&lookSupported()){
            int points=Math.min(32,chars.get(CameraCharacteristics.TONEMAP_MAX_CURVE_POINTS));
            b.set(CaptureRequest.TONEMAP_MODE,CaptureRequest.TONEMAP_MODE_CONTRAST_CURVE);
            b.set(CaptureRequest.TONEMAP_CURVE,new android.hardware.camera2.params.TonemapCurve(CreatorLooks.curve(look,0,lookStrength,points),CreatorLooks.curve(look,1,lookStrength,points),CreatorLooks.curve(look,2,lookStrength,points)));
        }
        int[] electronic=chars.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES),optical=chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION);
        if(electronic!=null&&electronic.length>0)b.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,stabilization==1?CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON:CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF);
        if(optical!=null&&optical.length>0)b.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE,stabilization==2?CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON:CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_OFF);
        Boolean flash=chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);if(Boolean.TRUE.equals(flash))b.set(CaptureRequest.FLASH_MODE,torch?CaptureRequest.FLASH_MODE_TORCH:CaptureRequest.FLASH_MODE_OFF);
        Rect sensor=chars.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        if(sensor!=null) {int w=(int)(sensor.width()/zoom),h=(int)(sensor.height()/zoom);int x=sensor.left+(sensor.width()-w)/2,y=sensor.top+(sensor.height()-h)/2;b.set(CaptureRequest.SCALER_CROP_REGION,new Rect(x,y,x+w,y+h));}
        if(aperture>0)b.set(CaptureRequest.LENS_APERTURE,aperture);
        Range<Integer>[] rates=chars.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
        if(rates!=null) {Range<Integer> best=null;for(Range<Integer> rate:rates)if(rate.getUpper()==videoFps&&(best==null||rate.getLower()>best.getLower()))best=rate;if(best!=null)b.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,best);}
        if(tapFocus&&tapTriggerPending){b.set(CaptureRequest.CONTROL_AF_TRIGGER,CaptureRequest.CONTROL_AF_TRIGGER_START);session.capture(b.build(),null,handler);b.set(CaptureRequest.CONTROL_AF_TRIGGER,CaptureRequest.CONTROL_AF_TRIGGER_IDLE);tapTriggerPending=false;}
        session.setRepeatingRequest(b.build(),new CameraCaptureSession.CaptureCallback(){
            @Override public void onCaptureCompleted(CameraCaptureSession s,CaptureRequest request,TotalCaptureResult result){
                if(s!=session||closed)return;
                if(!wbManual&&Objects.equals(request.get(CaptureRequest.CONTROL_AWB_MODE),wb)){
                    RggbChannelVector gains=result.get(CaptureResult.COLOR_CORRECTION_GAINS);ColorSpaceTransform transform=result.get(CaptureResult.COLOR_CORRECTION_TRANSFORM);
                    if(gains!=null&&transform!=null){wbBaseline=gains;wbTransform=transform;}
                }
                Long timestamp=result.get(CaptureResult.SENSOR_TIMESTAMP);if(timestamp!=null){long delta=timestamp-lastSensorTimestamp;if(lastSensorTimestamp>0&&delta>0&&delta<=1_000_000_000L){double measured=1e9/delta;sensorFps=sensorFps==0?measured:sensorFps*.9+measured*.1;}lastSensorTimestamp=timestamp;}
                Integer i=result.get(CaptureResult.SENSOR_SENSITIVITY);Long t=result.get(CaptureResult.SENSOR_EXPOSURE_TIME);
                Float a=result.get(CaptureResult.LENS_APERTURE);if(a!=null)actualAperture=a;
                if(i!=null)actualIso=i;if(t!=null)actualShutter=t;
                if(SystemClock.elapsedRealtime()-lastMetadataStatus>200){lastMetadataStatus=SystemClock.elapsedRealtime();publish();if(recording&&context.getFilesDir().getUsableSpace()<20_000_000)handler.post(()->{if(recording){stop();notice="Recording stopped: storage nearly full. "+notice;publish();}});}
            }
        },handler);
    }
    public void command(JSONObject json) {handler.post(()->{
        String id=json.optString("id");
        if(!id.isEmpty()&&commandIds.containsKey(id)){publish();return;}
        try {
            if(closed||camera==null)throw new Exception("Camera is not ready");
            if(busy)throw new Exception("Camera is changing state; try again after confirmation");
            String action=json.getString("action");
            if(!id.isEmpty()){commandIds.put(id,true);if(commandIds.size()>128)commandIds.remove(commandIds.keySet().iterator().next());}
            switch(action) {
                case "marker": if(!recording||recordingUri==null)throw new Exception("Start a take before marking it");String marker=json.optString("value","good");if(!marker.equals("good")&&!marker.equals("mistake"))throw new Exception("Unknown marker");CreatorNotes.marker(context,recordingUri.toString(),SystemClock.elapsedRealtime()-started,marker);notice=marker.equals("good")?"Good take marked":"Retake marked";break;
                case "shootingPreset": applyShootingPreset(json.getJSONObject("value"));break;
                case "syncOffset": if(recording)throw new Exception("Stop before changing sync calibration");int offset=json.getInt("value");if(Math.abs(offset)>1000)throw new Exception("Sync correction must be within 1000 ms");context.getSharedPreferences("microphone-sync",0).edit().putInt(syncKey(),offset).apply();break;
                case "start": if(!recording)start();break;
                case "stop": if(recording)stop();break;
                case "auto": if(fpsNeedsManual(videoFps))throw new Exception("Choose a camera auto-exposure FPS preset before enabling Auto exposure");manual=false;aeLock=false;apply(recording);break;
                case "iso": {
                    if(!manualSupported())throw new Exception("Manual exposure unsupported");
                    Range<Integer> r=chars.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);if(r==null)throw new Exception("ISO range unavailable");
                    int v=json.getInt("value");if(!r.contains(v))throw new Exception("ISO outside supported range");if(!manual&&actualShutter>0)shutter=actualShutter;iso=v;clampExposure();manual=true;aeLock=false;apply(recording);break;
                }
                case "shutter": {
                    if(!manualSupported())throw new Exception("Manual exposure unsupported");
                    long v=json.getLong("value");Range<Long> r=chars.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
                    if(r==null||!r.contains(v)||v>1_000_000_000L/videoFps)throw new Exception("Shutter must fit the selected video frame rate");if(!manual&&actualIso>0)iso=actualIso;shutter=v;clampExposure();manual=true;aeLock=false;apply(recording);break;
                }
                case "focus": {
                    float v=(float)json.getDouble("value");Float max=chars.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
                    if(!Float.isFinite(v)||(v!=-1&&(max==null||max<=0||v<0||v>max)))throw new Exception("Focus distance unsupported");focus=v;tapFocus=false;apply(recording);break;
                }
                case "tap": {
                    Integer regions=chars.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF);
                    if(regions==null||regions<1||!contains(chars.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES),CaptureRequest.CONTROL_AF_MODE_AUTO))throw new Exception("Tap-to-focus unsupported on this lens");
                    JSONObject point=json.getJSONObject("value");double px=point.getDouble("x"),py=point.getDouble("y");if(!Double.isFinite(px)||!Double.isFinite(py)||px<0||px>1||py<0||py>1)throw new Exception("Invalid focus point");
                    Rect a=chars.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);if(a==null)throw new Exception("Focus area unavailable");
                    int cw=(int)(a.width()/zoom),ch=(int)(a.height()/zoom);double aspect=(double)reader.getWidth()/reader.getHeight();if((double)cw/ch>aspect)cw=(int)(ch*aspect);else ch=(int)(cw/aspect);int left=a.left+(a.width()-cw)/2,top=a.top+(a.height()-ch)/2;
                    int w=Math.max(1,cw/10),h=Math.max(1,ch/10),x=Math.max(left,Math.min(left+cw-w,left+(int)(px*cw)-w/2)),y=Math.max(top,Math.min(top+ch-h,top+(int)(py*ch)-h/2));
                    metering=new MeteringRectangle(x,y,w,h,MeteringRectangle.METERING_WEIGHT_MAX);tapFocus=true;tapTriggerPending=true;focus=-1;apply(recording);break;
                }
                case "aeLock": if(!Boolean.TRUE.equals(chars.get(CameraCharacteristics.CONTROL_AE_LOCK_AVAILABLE))||manual)throw new Exception("Exposure lock requires supported automatic exposure");aeLock=json.getBoolean("value");apply(recording);break;
                case "awbLock": if(wbManual)throw new Exception("Manual white balance is already fixed; choose a preset to use AWB lock");if(!Boolean.TRUE.equals(chars.get(CameraCharacteristics.CONTROL_AWB_LOCK_AVAILABLE)))throw new Exception("White balance lock unsupported");awbLock=json.getBoolean("value");apply(recording);break;
                case "zoom": {float v=(float)json.getDouble("value");Float max=chars.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);if(!Float.isFinite(v)||max==null||v<1||v>max)throw new Exception("Zoom outside supported range");zoom=v;apply(recording);break;}
                case "wb": {int v=json.getInt("value");if(v==0||!contains(chars.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES),v))throw new Exception("White balance mode unsupported");wb=v;wbManual=false;wbFine=0;wbBaseline=null;wbTransform=null;awbLock=false;apply(recording);break;}
                case "wbFine": {
                    double v=json.getDouble("value");if(!fineWbSupported())throw new Exception("Fine white balance unsupported on this lens");
                    if(wbBaseline==null||wbTransform==null)throw new Exception("Wait for camera white balance to settle first");
                    WhiteBalanceFine.gains(wbBaseline.getRed(),wbBaseline.getGreenEven(),wbBaseline.getGreenOdd(),wbBaseline.getBlue(),v);
                    double previous=wbFine;boolean previousManual=wbManual,previousLock=awbLock;
                    wbFine=v;wbManual=true;awbLock=false;
                    try{apply(recording);}catch(Exception failure){wbFine=previous;wbManual=previousManual;awbLock=previousLock;throw failure;}break;
                }
                case "ev": {int v=json.getInt("value");Range<Integer> r=chars.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);if(r==null||!r.contains(v))throw new Exception("Exposure compensation unsupported");ev=v;manual=false;aeLock=false;apply(recording);break;}
                case "torch": if(!Boolean.TRUE.equals(chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)))throw new Exception("Torch unavailable");torch=json.getBoolean("value");apply(recording);break;
                case "stabilization": {if(recording)throw new Exception("Stop recording before changing stabilization");int v=json.getInt("value");if(v<0||v>2||(v==1&&!contains(chars.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES),1))||(v==2&&!contains(chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION),1)))throw new Exception("Stabilization mode unsupported");stabilization=v;apply(false);break;}
                case "aperture": {float v=(float)json.getDouble("value");float[] values=chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES);boolean ok=false;if(values!=null&&values.length>1)for(float a:values)if(Math.abs(a-v)<0.001f)ok=true;if(!ok)throw new Exception("Aperture is fixed or value unsupported");aperture=v;apply(recording);break;}
                case "lens": {
                    if(recording)throw new Exception("Stop recording before changing lenses");String next=json.getString("value");if(!Arrays.asList(manager.getCameraIdList()).contains(next))throw new Exception("Camera unavailable");
                    switchLens(next);break;
                }
                case "video": {
                    if(recording)throw new Exception("Stop recording before changing video quality");
                    JSONObject value=json.getJSONObject("value");String idValue=value.getString("id");JSONObject option=null;
                    for(int i=0;i<videoOptions.length();i++)if(videoOptions.getJSONObject(i).getString("id").equals(idValue))option=videoOptions.getJSONObject(i);
                    if(option==null)throw new Exception("Video format unavailable on this camera");
                    highQuality=value.getBoolean("highQuality");
                    if(!videoOption.equals(idValue)){String reopen=cameraId;videoOption=idValue;lensFormats.put(cameraId,idValue);closeDevices();openCamera(reopen);}else updateBitrate(option);
                    break;
                }
                case "flip": {
                    if(recording)throw new Exception("Stop recording before switching front / back");
                    Integer facing=chars.get(CameraCharacteristics.LENS_FACING);int target=facing!=null&&facing==CameraCharacteristics.LENS_FACING_FRONT?CameraCharacteristics.LENS_FACING_BACK:CameraCharacteristics.LENS_FACING_FRONT;
                    String next=null;for(String idValue:manager.getCameraIdList())if(Objects.equals(manager.getCameraCharacteristics(idValue).get(CameraCharacteristics.LENS_FACING),target)){next=idValue;break;}
                    if(next==null)throw new Exception("Opposite-facing camera unavailable");
                    switchLens(next);break;
                }
                case "fps": {
                    if(recording)throw new Exception("Stop recording before changing frame rate");
                    double raw=json.getDouble("value");int requested=(int)raw;boolean available=false;for(int i=0;i<fpsRates.length();i++)if(fpsRates.getInt(i)==requested)available=true;
                    if(raw!=requested||!available)throw new Exception("Frame rate unavailable at this resolution");
                    int previousFps=videoFps,previousIso=iso;long previousShutter=shutter;boolean previousManual=manual,previousLock=aeLock;
                    if(fpsNeedsManual(requested)){if(!manualSupported())throw new Exception("This rate requires manual exposure");if(!manual){if(actualIso>0)iso=actualIso;if(actualShutter>0)shutter=actualShutter;}manual=true;aeLock=false;}
                    videoFps=requested;lastSensorTimestamp=0;sensorFps=0;clampExposure();updateBitrate(selectedVideo());try{apply(false);}catch(Exception error){videoFps=previousFps;iso=previousIso;shutter=previousShutter;manual=previousManual;aeLock=previousLock;updateBitrate(selectedVideo());throw error;}notice=fpsNeedsManual(requested)?"Custom FPS uses manual exposure; adjust ISO / shutter":"Ready";break;
                }
                case "look": {int requested=json.getInt("value");if(requested<0||requested>=CreatorLooks.NAMES.length||requested!=0&&!lookSupported())throw new Exception("Color looks unavailable on this camera");int previous=look;look=requested;try{apply(recording);}catch(Exception error){look=previous;throw error;}break;}
                case "lookStrength": {double requested=json.getDouble("value");if(!lookSupported()||!Double.isFinite(requested)||requested<0||requested>100)throw new Exception("Look intensity unavailable");double previous=lookStrength;lookStrength=requested;try{apply(recording);}catch(Exception error){lookStrength=previous;throw error;}break;}
                case "voiceProcessing": {if(recording)throw new Exception("Stop recording before changing audio processing");voiceProcessing=json.getBoolean("value");break;}
                case "audioInput": {
                    if(recording)throw new Exception("Stop recording before changing microphone");
                    int requested=json.getInt("value");if(requested!=0&&findAudioInput(requested)==null)throw new Exception("Microphone disconnected; reconnect it and choose again");audioInputId=requested;break;
                }
                case "previewProfile": {if(recording)throw new Exception("Stop recording before changing preview quality");livePreview=json.getBoolean("value");context.getSharedPreferences("camera-preview",Context.MODE_PRIVATE).edit().putBoolean("live",livePreview).apply();int previousFps=videoFps;String currentId=cameraId;closeDevices();openCamera(currentId);for(int i=0;i<fpsRates.length();i++)if(fpsRates.optInt(i)==previousFps){videoFps=previousFps;updateBitrate(selectedVideo());clampExposure();break;}break;}
                case "antiBanding": {int value=json.getInt("value");if(!contains(chars.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES),value))throw new Exception("Anti-flicker mode unavailable on this camera");int previous=antiBanding;antiBanding=value;try{apply(recording);}catch(Exception e){antiBanding=previous;throw e;}break;}
                case "slate": {if(recording)throw new Exception("Stop recording before changing the slate");JSONObject value=json.getJSONObject("value");context.getSharedPreferences("production-slate",Context.MODE_PRIVATE).edit().putString("production",TakeNaming.safe(value.optString("production"))).putString("scene",TakeNaming.safe(value.optString("scene"))).putInt("take",Math.max(1,Math.min(99999,value.optInt("take",1)))).apply();break;}
                case "state": break;
                case "recover": if(recording)throw new Exception("Stop recording before recovery");recoverPending();break;
                default:throw new Exception("Unknown camera command");
            }
            if(notice.startsWith("Rejected:"))notice=recording?"Recording on camera phone":"Ready";publish();
        } catch(Exception e){if(!recording&&!busy&&recorder!=null){recorder.release();recorder=null;closeRecordingDescriptor();}notice="Rejected: "+e.getMessage();listener.error(notice);publish();}
    });}
    private String syncKey(){AudioDeviceInfo input=audioInputId==0?null:findAudioInput(audioInputId);return input==null?"system-default":input.getType()+":"+input.getProductName();}
    private void applyShootingPreset(JSONObject p)throws Exception{
        if(recording)throw new Exception("Stop recording before applying a preset");
        if(!cameraId.equals(p.optString("cameraId")))throw new Exception("Switch to the lens used to save this preset first");
        JSONObject format=null;for(int i=0;i<videoOptions.length();i++)if(videoOptions.getJSONObject(i).optString("id").equals(p.optString("videoOption")))format=videoOptions.getJSONObject(i);
        if(format==null)throw new Exception("Preset video format is no longer available");
        int requestedFps=p.optInt("videoFps",30);JSONArray rates=VideoOptions.frameRates(chars,format);boolean hasRate=false;for(int i=0;i<rates.length();i++)if(rates.optInt(i)==requestedFps)hasRate=true;if(!hasRate)throw new Exception("Preset frame rate unavailable");
        int input=p.optInt("audioInputId");if(input!=0){JSONArray savedInputs=p.optJSONArray("audioInputs");String inputName="";if(savedInputs!=null)for(int i=0;i<savedInputs.length();i++){JSONObject savedInput=savedInputs.optJSONObject(i);if(savedInput!=null&&savedInput.optInt("id")==input)inputName=savedInput.optString("name");}if(!inputName.isEmpty()){input=-1;for(AudioDeviceInfo candidate:context.getSystemService(AudioManager.class).getDevices(AudioManager.GET_DEVICES_INPUTS))if(audioName(candidate).equals(inputName)){input=candidate.getId();break;}}}if(input!=0&&findAudioInput(input)==null)throw new Exception("Preset microphone disconnected; connect it and save the preset again");
        boolean manualWb=p.optBoolean("wbManual");JSONArray gains=p.optJSONArray("wbGains"),matrix=p.optJSONArray("wbMatrix");if(manualWb&&(!fineWbSupported()||gains==null||matrix==null||gains.length()!=4||matrix.length()!=18))throw new Exception("Preset white balance unavailable");
        boolean reopen=!videoOption.equals(format.getString("id"));videoOption=format.getString("id");highQuality=p.optBoolean("highQuality",true);if(reopen){lensFormats.put(cameraId,videoOption);String id=cameraId;closeDevices();openCamera(id);}
        videoFps=requestedFps;iso=p.optInt("iso",400);shutter=p.optLong("shutter",20000000);clampExposure();manual=p.optBoolean("manual")||fpsNeedsManual(videoFps);zoom=(float)Math.max(1,Math.min(p.optDouble("zoom",1),chars.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)==null?1:chars.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)));focus=(float)p.optDouble("focus",-1);Float maxFocus=chars.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);if(focus<0||maxFocus==null||focus>maxFocus)focus=-1;
        wb=p.optInt("wb",1);if(!contains(chars.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES),wb))wb=1;wbManual=manualWb;wbFine=p.optDouble("wbFine");if(manualWb){wbBaseline=new RggbChannelVector((float)gains.getDouble(0),(float)gains.getDouble(1),(float)gains.getDouble(2),(float)gains.getDouble(3));Rational[] cells=new Rational[9];for(int i=0;i<9;i++)cells[i]=new Rational(matrix.getInt(i*2),matrix.getInt(i*2+1));wbTransform=new ColorSpaceTransform(cells);}
        ev=p.optInt("ev");Range<Integer> compensation=chars.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);ev=compensation==null?0:compensation.clamp(ev);torch=p.optBoolean("torch")&&Boolean.TRUE.equals(chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE));aeLock=p.optBoolean("aeLock")&&!manual&&Boolean.TRUE.equals(chars.get(CameraCharacteristics.CONTROL_AE_LOCK_AVAILABLE));awbLock=p.optBoolean("awbLock")&&!manualWb&&Boolean.TRUE.equals(chars.get(CameraCharacteristics.CONTROL_AWB_LOCK_AVAILABLE));
        stabilization=p.optInt("stabilization");if(stabilization==1&&!contains(chars.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES),1)||stabilization==2&&!contains(chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION),1))stabilization=0;
        aperture=(float)p.optDouble("requestedAperture");look=lookSupported()?Math.max(0,Math.min(CreatorLooks.NAMES.length-1,p.optInt("look"))):0;lookStrength=p.optDouble("lookStrength",100);antiBanding=p.optInt("antiBanding",3);audioInputId=input;voiceProcessing=p.optBoolean("voiceProcessing");tapFocus=false;updateBitrate(format);if(!reopen)apply(false);notice="Shooting preset applied";
    }
    private AudioDeviceInfo findAudioInput(int id){for(AudioDeviceInfo input:context.getSystemService(AudioManager.class).getDevices(AudioManager.GET_DEVICES_INPUTS))if(input.getId()==id)return input;return null;}
    private String audioName(AudioDeviceInfo input){String kind=input.getType()==AudioDeviceInfo.TYPE_USB_DEVICE||input.getType()==AudioDeviceInfo.TYPE_USB_HEADSET?"USB":input.getType()==AudioDeviceInfo.TYPE_BUILTIN_MIC?"Built-in":input.getType()==AudioDeviceInfo.TYPE_WIRED_HEADSET?"Wired":"Input";return kind+" / "+input.getProductName();}
    private void switchLens(String next)throws Exception {
        closeDevices();manual=false;focus=-1;zoom=1;aperture=0;actualAperture=0;actualIso=0;actualShutter=0;torch=false;wb=1;wbManual=false;wbFine=0;wbBaseline=null;wbTransform=null;aeLock=false;awbLock=false;tapFocus=false;tapTriggerPending=false;stabilization=0;openCamera(next);
    }
    private void updateBitrate(JSONObject option)throws Exception {
        videoBitrate=VideoQuality.bitrate(option.getInt("width"),option.getInt("height"),videoFps,highQuality,option.getInt("bitrateMin"),option.getInt("bitrateMax"));
    }
    private void clampExposure() {
        Range<Integer> i=chars.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);if(i!=null)iso=i.clamp(iso);
        Range<Long> t=chars.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);if(t!=null)shutter=t.clamp(Math.min(shutter,1_000_000_000L/videoFps));
    }
    private void start() throws Exception {
        if(context.checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)throw new Exception("Microphone permission required for video recording");
        if(context.getFilesDir().getUsableSpace()<150_000_000)throw new Exception("Less than 150 MB free storage");
        AudioDeviceInfo preferred=audioInputId==0?null:findAudioInput(audioInputId);if(audioInputId!=0&&preferred==null)throw new Exception("Selected microphone disconnected");
        android.content.SharedPreferences slate=context.getSharedPreferences("production-slate",Context.MODE_PRIVATE);int take=slate.getInt("take",1);String name=TakeNaming.filename(slate.getString("production",""),slate.getString("scene",""),take,System.currentTimeMillis());ContentValues values=new ContentValues();values.put(MediaStore.Video.Media.DISPLAY_NAME,name);values.put(MediaStore.Video.Media.MIME_TYPE,"video/mp4");values.put(MediaStore.Video.Media.RELATIVE_PATH,"Movies/SoloCamera");values.put(MediaStore.Video.Media.IS_PENDING,1);
        recordingUri=context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,values);if(recordingUri==null)throw new IOException("Cannot create recording");slate.edit().putInt("take",Math.min(99999,take+1)).apply();
        recordingDescriptor=context.getContentResolver().openFileDescriptor(recordingUri,"rw");if(recordingDescriptor==null)throw new IOException("Cannot open recording output");
        recorder=new MediaRecorder();recorder.setAudioSource(voiceProcessing?MediaRecorder.AudioSource.VOICE_COMMUNICATION:MediaRecorder.AudioSource.CAMCORDER);recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);recorder.setOutputFile(recordingDescriptor.getFileDescriptor());
        recorder.setVideoEncodingBitRate(videoBitrate);recorder.setVideoFrameRate(videoFps);recorder.setVideoSize(videoSize.getWidth(),videoSize.getHeight());
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);recorder.setAudioEncodingBitRate(128000);recorder.setAudioSamplingRate(48000);
        recorder.setOrientationHint(rotation());
        recorder.setOnErrorListener((r,what,extra)->handler.post(()->recover(new Exception("Recorder error "+what+" / "+extra))));
        if(preferred!=null&&!recorder.setPreferredDevice(preferred))throw new Exception("Android rejected the selected microphone");
        recordingSyncKey="";recordingRouteId=0;actualAudioRoute="Starting microphone";
        recorder.addOnRoutingChangedListener(routing->{if(!recording||recorder==null)return;AudioDeviceInfo actual=recorder.getRoutedDevice();if(actual==null)return;
            actualAudioRoute=audioName(actual);if(recordingSyncKey.isEmpty())recordingSyncKey=actual.getType()+":"+actual.getProductName();
            if((recordingRouteId!=0&&recordingRouteId!=actual.getId())||(audioInputId!=0&&actual.getId()!=audioInputId)){
                stop();notice="Microphone route changed; take stopped and saved. Reconnect the selected microphone before the next take.";listener.error(notice);publish();return;
            }
            recordingRouteId=actual.getId();publish();
        },handler);
        recorder.prepare();notice="Starting recording";configure(true);
    }
    private void stop() {
        busy=true;notice="Saving video";publish();
        try {
            if(session!=null){session.stopRepeating();session.abortCaptures();session.close();session=null;}
            recorder.stop();recorder.release();recorder=null;recording=false;
            finalizeVideo();notice="Saved to Movies/SoloCamera";
        } catch(Exception e){notice="Save failed; use Recover interrupted takes in Controls. "+e.getMessage();listener.error(notice);if(recorder!=null){recorder.release();recorder=null;}closeRecordingDescriptor();recording=false;}
        busy=false;if(!closed)configure(false);publish();
    }
    private void closeRecordingDescriptor(){if(recordingDescriptor!=null){try{recordingDescriptor.close();}catch(Exception ignored){}recordingDescriptor=null;}}
    private void finalizeVideo() throws Exception {
        closeRecordingDescriptor();if(recordingUri==null)throw new IOException("No output recording");
        JSONObject notes=CreatorNotes.read(context,recordingUri.toString());String takeRoute=recordingSyncKey.isEmpty()?syncKey():recordingSyncKey;notes.put("audioSyncMs",context.getSharedPreferences("microphone-sync",0).getInt(takeRoute,0));notes.put("routeKey",takeRoute);notes.put("route",actualAudioRoute);CreatorNotes.write(context,recordingUri.toString(),notes);ContentValues values=new ContentValues();values.put(MediaStore.Video.Media.IS_PENDING,0);if(context.getContentResolver().update(recordingUri,values,null,null)!=1)throw new IOException("Cannot finalize gallery entry");recordingUri=null;
    }
    private void recoverPending() throws Exception {
        int recovered=0,unreadable=0;
        Uri collection=MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        try(android.database.Cursor rows=context.getContentResolver().query(collection,new String[]{MediaStore.Video.Media._ID},MediaStore.Video.Media.IS_PENDING+"=1 AND "+MediaStore.Video.Media.RELATIVE_PATH+"=? AND "+MediaStore.Video.Media.OWNER_PACKAGE_NAME+"=?",new String[]{"Movies/SoloCamera/",context.getPackageName()},null)){
            if(rows!=null)while(rows.moveToNext()){
                Uri uri=ContentUris.withAppendedId(collection,rows.getLong(0));
                try(MediaMetadataRetriever retriever=new MediaMetadataRetriever()){
                    retriever.setDataSource(context,uri);String duration=retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    if(duration==null||Long.parseLong(duration)<=0){unreadable++;continue;}
                    ContentValues values=new ContentValues();values.put(MediaStore.Video.Media.IS_PENDING,0);recovered+=context.getContentResolver().update(uri,values,null,null);
                }catch(Exception e){unreadable++;}
            }
        }notice="Recovered "+recovered+" take(s); "+unreadable+" unreadable pending take(s) retained. Recovery cannot repair unfinished MP4 metadata.";
    }
    private void recover(Exception e) {
        if(session!=null){session.close();session=null;}
        boolean stopped=false;if(recorder!=null){try {if(recording){recorder.stop();stopped=true;}}catch(Exception ignored){}recorder.release();recorder=null;}closeRecordingDescriptor();
        if(stopped)try{finalizeVideo();}catch(Exception ignored){}
        recording=false;busy=false;notice="Camera failure: "+e.getMessage()+". Check gallery, or use Recover interrupted takes.";listener.error(notice);publish();
        // A failed stream combination must not trigger an endless reconfiguration loop.
    }
    private void fail(Exception e){notice=e.getMessage();listener.error(notice);busy=false;publish();}
    private JSONObject snapshot() throws Exception {
        JSONObject s=new JSONObject();s.put("type","state");s.put("recording",recording);s.put("busy",busy);s.put("notice",notice);s.put("elapsed",recording?SystemClock.elapsedRealtime()-started:0);
        s.put("syncKey",syncKey());s.put("audioSyncMs",context.getSharedPreferences("microphone-sync",0).getInt(syncKey(),0));s.put("audioInputAvailable",audioInputId==0||findAudioInput(audioInputId)!=null);s.put("ready",camera!=null&&session!=null&&!closed);int amplitude=recording&&recorder!=null?recorder.getMaxAmplitude():0;s.put("audioDb",amplitude==0?-96:Math.max(-96,20*Math.log10(amplitude/32767.0)));
        s.put("freeMB",context.getFilesDir().getUsableSpace()/1_000_000);s.put("iso",iso);s.put("shutter",shutter);s.put("actualIso",actualIso);s.put("actualShutter",actualShutter);s.put("manual",manual);s.put("focus",focus);s.put("zoom",zoom);s.put("wb",wb);s.put("torch",torch);s.put("ev",ev);s.put("rotation",frameRotation);
        Intent battery=context.registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));if(battery!=null)s.put("battery",battery.getIntExtra(BatteryManager.EXTRA_LEVEL,0)*100/Math.max(1,battery.getIntExtra(BatteryManager.EXTRA_SCALE,100)));
        PowerManager power=(PowerManager)context.getSystemService(Context.POWER_SERVICE);s.put("thermal",power.getCurrentThermalStatus());
        if(chars!=null){
            android.content.SharedPreferences slate=context.getSharedPreferences("production-slate",Context.MODE_PRIVATE);s.put("production",slate.getString("production",""));s.put("scene",slate.getString("scene",""));s.put("nextTake",slate.getInt("take",1));JSONArray antiModes=new JSONArray();int[] availableAnti=chars.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES);if(availableAnti!=null)for(int mode:availableAnti)antiModes.put(mode);s.put("antiBandingModes",antiModes);s.put("antiBanding",antiBanding);s.put("livePreview",livePreview);s.put("previewSize",reader==null?"":reader.getWidth()+" × "+reader.getHeight());s.put("cameraId",cameraId);s.put("manualSupported",manualSupported());
            s.put("wbManual",wbManual);if(wbBaseline!=null&&wbTransform!=null){s.put("wbGains",new JSONArray(new double[]{wbBaseline.getRed(),wbBaseline.getGreenEven(),wbBaseline.getGreenOdd(),wbBaseline.getBlue()}));JSONArray transform=new JSONArray();for(int row=0;row<3;row++)for(int col=0;col<3;col++){Rational r=wbTransform.getElement(col,row);transform.put(r.getNumerator());transform.put(r.getDenominator());}s.put("wbMatrix",transform);}s.put("wbFine",wbFine);s.put("wbFineSupported",fineWbSupported());s.put("wbFineReady",fineWbSupported()&&wbBaseline!=null&&wbTransform!=null);
            s.put("actualAperture",actualAperture);s.put("requestedAperture",aperture);Rational step=chars.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP);s.put("evStep",step==null?1:step.doubleValue());
            s.put("aeLock",aeLock);s.put("awbLock",awbLock);s.put("aeLockSupported",Boolean.TRUE.equals(chars.get(CameraCharacteristics.CONTROL_AE_LOCK_AVAILABLE)));s.put("awbLockSupported",Boolean.TRUE.equals(chars.get(CameraCharacteristics.CONTROL_AWB_LOCK_AVAILABLE)));
            Range<Integer> ir=chars.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);s.put("isoMin",ir==null?100:ir.getLower());s.put("isoMax",ir==null?100:ir.getUpper());
            Range<Long> er=chars.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);s.put("shutterMin",er==null?1:er.getLower());s.put("shutterMax",er==null?1:Math.min(1_000_000_000L/videoFps,er.getUpper()));
            Float max=chars.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);s.put("focusMax",max==null?0:max);
            Float z=chars.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);s.put("zoomMax",z==null?1:z);
            s.put("torchSupported",Boolean.TRUE.equals(chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)));
            s.put("videoStabilization",contains(chars.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES),1));s.put("opticalStabilization",contains(chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION),1));s.put("stabilization",stabilization);
            Range<Integer> evr=chars.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);s.put("evMin",evr==null?0:evr.getLower());s.put("evMax",evr==null?0:evr.getUpper());
            JSONArray apertures=new JSONArray();float[] aa=chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES);if(aa!=null)for(float a:aa)apertures.put(a);s.put("apertures",apertures);
            JSONArray wbs=new JSONArray();int[] ww=chars.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES);if(ww!=null)for(int w:ww)if(w!=0)wbs.put(w);s.put("wbs",wbs);
            JSONArray lenses=new JSONArray();for(String id:manager.getCameraIdList()){JSONObject lens=new JSONObject();lens.put("id",id);Integer f=manager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING);lens.put("name",(f!=null&&f==0?"Front":"Rear")+" · "+id);lenses.put(lens);}s.put("lenses",lenses);
            JSONArray inputs=new JSONArray();for(AudioDeviceInfo input:context.getSystemService(AudioManager.class).getDevices(AudioManager.GET_DEVICES_INPUTS)){JSONObject item=new JSONObject();item.put("id",input.getId());item.put("name",audioName(input));inputs.put(item);}s.put("audioInputs",inputs);s.put("audioInputId",audioInputId);s.put("audioRoute",actualAudioRoute);
            s.put("sensorFps",Math.round(sensorFps));s.put("fpsRates",fpsRates);s.put("fpsManual",fpsNeedsManual(videoFps));s.put("lookSupported",lookSupported());s.put("look",look);s.put("lookStrength",lookStrength);s.put("voiceProcessing",voiceProcessing);s.put("videoOptions",videoOptions);s.put("videoOption",videoOption);s.put("videoFps",videoFps);s.put("videoBitrate",videoBitrate);s.put("highQuality",highQuality);
            s.put("videoLabel",videoSize==null?"Video":VideoQuality.label(videoSize.getWidth(),videoSize.getHeight())+" / "+videoFps);
            Integer facing=chars.get(CameraCharacteristics.LENS_FACING);s.put("front",facing!=null&&facing==CameraCharacteristics.LENS_FACING_FRONT);boolean canFlip=false;for(String id:manager.getCameraIdList()){Integer other=manager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING);if(other!=null&&facing!=null&&!other.equals(facing)&&(other==CameraCharacteristics.LENS_FACING_FRONT||other==CameraCharacteristics.LENS_FACING_BACK))canFlip=true;}s.put("canFlip",canFlip);
            s.put("format",videoSize==null?"Unavailable":videoSize.getWidth()+" x "+videoSize.getHeight()+" / H.264 / AAC / "+videoFps+" fps requested / "+Math.round(videoBitrate/1e6)+" Mbps requested");
        }return s;
    }
    private void publish(){try {listener.state(snapshot());}catch(Exception e){listener.error("Status: "+e.getMessage());}}
    private void closeDevices(){openGeneration++;if(session!=null){session.close();session=null;}if(camera!=null){camera.close();camera=null;}ImageReader oldReader=reader;reader=null;if(oldReader!=null)previewHandler.post(oldReader::close);}
    public void close(){close(null);}
    public void close(Runnable afterClose){closed=true;handler.post(()->{if(recording)stop();if(recorder!=null){recorder.release();recorder=null;}closeRecordingDescriptor();closeDevices();thread.quitSafely();previewThread.quitSafely();if(afterClose!=null)afterClose.run();});}
}




