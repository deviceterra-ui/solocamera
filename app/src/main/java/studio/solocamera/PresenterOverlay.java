package studio.solocamera;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.hardware.Camera;
import android.hardware.display.DisplayManager;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.FrameLayout;
import java.util.function.Consumer;

/** GPU camera preview in a draggable overlay; MediaProjection records its visible composition. */
@SuppressWarnings("deprecation")
final class PresenterOverlay implements AutoCloseable,TextureView.SurfaceTextureListener,DisplayManager.DisplayListener {
    private final Context context;
    private final WindowManager windows;
    private final DisplayManager displays;
    private final Handler main=new Handler(Looper.getMainLooper());
    private final Consumer<String> failure;
    private final Consumer<Boolean> modeChanged;
    private final FrameLayout root;
    private final TextureView texture;
    private final WindowManager.LayoutParams layout;
    private final int diameter;
    private final boolean mirror;
    private float positionX,positionY,downX,downY;
    private int initialX,initialY,width,height,top,bottom,previewWidth,previewHeight,cameraOrientation;
    private boolean fullscreen,closed,attached,moved,suspended;private int facing=Camera.CameraInfo.CAMERA_FACING_FRONT,retries;private long touchStarted;
    private Camera camera;
    private final Camera.CameraInfo cameraInfo=new Camera.CameraInfo();

    PresenterOverlay(Context service,int diameterDp,boolean mirror,Consumer<String> failure,Consumer<Boolean> modeChanged){
        displays=service.getSystemService(DisplayManager.class);
        Display display=displays.getDisplay(Display.DEFAULT_DISPLAY);
        if(display==null)throw new IllegalStateException("No display available");
        Context visual=service.createDisplayContext(display);
        context=Build.VERSION.SDK_INT>=30?visual.createWindowContext(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,null):visual;
        windows=context.getSystemService(WindowManager.class);this.failure=failure;this.modeChanged=modeChanged;this.mirror=mirror;
        diameter=(int)(Math.max(80,Math.min(160,diameterDp))*context.getResources().getDisplayMetrics().density);
        positionX=ScreenRecordingService.preferences(service).getFloat("bubbleX",.9f);positionY=ScreenRecordingService.preferences(service).getFloat("bubbleY",.85f);
        root=new FrameLayout(context);root.setBackgroundColor(Color.BLACK);root.setClipToOutline(true);
        root.setOutlineProvider(new ViewOutlineProvider(){@Override public void getOutline(View view,Outline outline){if(fullscreen)outline.setRect(0,0,view.getWidth(),view.getHeight());else outline.setOval(0,0,view.getWidth(),view.getHeight());}});
        texture=new TextureView(context);texture.setOpaque(true);texture.setSurfaceTextureListener(this);root.addView(texture,new FrameLayout.LayoutParams(-1,-1));
        root.setElevation(12*context.getResources().getDisplayMetrics().density);root.setOnClickListener(v->toggle());
        root.setOnTouchListener((view,event)->touch(event));
        layout=new WindowManager.LayoutParams(diameter,diameter,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);layout.gravity=Gravity.TOP|Gravity.LEFT;layout.setTitle("Solo Camera presenter");
    }
    void show(){
        if(!Settings.canDrawOverlays(context))throw new SecurityException("Allow Display over other apps for the presenter camera");
        if(context.checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED)throw new SecurityException("Camera permission is needed");
        updateScreen();applyBounds(false);windows.addView(root,layout);attached=true;displays.registerDisplayListener(this,main);describe();
    }
    private int dimension(String name){int id=context.getResources().getIdentifier(name,"dimen","android");return id==0?0:context.getResources().getDimensionPixelSize(id);}
    private void updateScreen(){android.util.DisplayMetrics metrics=new android.util.DisplayMetrics();Display display=displays.getDisplay(Display.DEFAULT_DISPLAY);if(display==null)return;display.getRealMetrics(metrics);width=metrics.widthPixels;height=metrics.heightPixels;top=dimension("status_bar_height");bottom=dimension("navigation_bar_height");}
    private void applyBounds(boolean update){int[] bounds=PresenterGeometry.bounds(width,height,top,bottom,diameter,positionX,positionY,fullscreen);layout.x=bounds[0];layout.y=bounds[1];layout.width=bounds[2];layout.height=bounds[3];if(update&&attached)windows.updateViewLayout(root,layout);root.invalidateOutline();root.post(this::transform);}
    private void describe(){root.setContentDescription((facing==Camera.CameraInfo.CAMERA_FACING_FRONT?"Front":"Back")+" presenter camera. "+(fullscreen?"Tap to return to circle.":"Drag to move. Tap to fill screen.")+" Hold to switch front/back.");}
    boolean front(){return facing==Camera.CameraInfo.CAMERA_FACING_FRONT;}
    void suspend(boolean value){if(closed)return;suspended=value;if(value){releaseCamera();root.setVisibility(View.GONE);}else{retries=0;root.setVisibility(View.VISIBLE);if(texture.isAvailable())openCamera(texture.getSurfaceTexture());}}
    void flip(){if(closed)return;int next=front()?Camera.CameraInfo.CAMERA_FACING_BACK:Camera.CameraInfo.CAMERA_FACING_FRONT;boolean exists=false;Camera.CameraInfo info=new Camera.CameraInfo();for(int i=0;i<Camera.getNumberOfCameras();i++){Camera.getCameraInfo(i,info);if(info.facing==next)exists=true;}if(!exists){failure.accept("This phone has no opposite-facing presenter camera. Screen recording continues.");return;}releaseCamera();facing=next;retries=0;describe();if(!suspended&&texture.isAvailable()){root.setVisibility(View.VISIBLE);openCamera(texture.getSurfaceTexture());}modeChanged.accept(fullscreen);}
    void toggle(){if(closed)return;fullscreen=!fullscreen;applyBounds(true);describe();modeChanged.accept(fullscreen);}
    private boolean touch(MotionEvent event){
        if(closed)return false;
        switch(event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:touchStarted=SystemClock.uptimeMillis();downX=event.getRawX();downY=event.getRawY();initialX=layout.x;initialY=layout.y;moved=false;return true;
            case MotionEvent.ACTION_MOVE:float dx=event.getRawX()-downX,dy=event.getRawY()-downY;if(Math.hypot(dx,dy)>ViewConfiguration.get(context).getScaledTouchSlop())moved=true;if(!fullscreen&&moved){int size=layout.width;int x=Math.max(0,Math.min(width-size,initialX+Math.round(dx))),y=Math.max(top,Math.min(height-bottom-size,initialY+Math.round(dy)));positionX=width==size?0:x/(float)(width-size);positionY=height-top-bottom==size?0:(y-top)/(float)(height-top-bottom-size);applyBounds(true);}return true;
            case MotionEvent.ACTION_UP:if(!moved){if(SystemClock.uptimeMillis()-touchStarted>=ViewConfiguration.getLongPressTimeout())flip();else root.performClick();}else ScreenRecordingService.preferences(context).edit().putFloat("bubbleX",positionX).putFloat("bubbleY",positionY).apply();return true;
            case MotionEvent.ACTION_CANCEL:return true;
            default:return true;
        }
    }
    private void openCamera(SurfaceTexture target){
        if(closed||suspended||camera!=null)return;
        try{
            int id=-1;for(int i=0;i<Camera.getNumberOfCameras();i++){Camera.getCameraInfo(i,cameraInfo);if(cameraInfo.facing==facing){id=i;break;}}
            if(id<0)throw new IllegalStateException("Selected presenter camera unavailable");
            Camera.getCameraInfo(id,cameraInfo);camera=Camera.open(id);Camera.Parameters parameters=camera.getParameters();Camera.Size chosen=null;
            for(Camera.Size candidate:parameters.getSupportedPreviewSizes())if(candidate.width<=1280&&candidate.height<=720&&(chosen==null||(long)candidate.width*candidate.height>(long)chosen.width*chosen.height))chosen=candidate;
            if(chosen==null)chosen=parameters.getSupportedPreviewSizes().get(0);parameters.setPreviewSize(chosen.width,chosen.height);previewWidth=chosen.width;previewHeight=chosen.height;
            java.util.List<String> focus=parameters.getSupportedFocusModes();if(focus!=null&&focus.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            int[] fps=null;for(int[] candidate:parameters.getSupportedPreviewFpsRange())if(candidate[1]<=30000&&(fps==null||candidate[1]>fps[1]||candidate[1]==fps[1]&&candidate[0]>fps[0]))fps=candidate;if(fps!=null)parameters.setPreviewFpsRange(fps[0],fps[1]);
            camera.setParameters(parameters);target.setDefaultBufferSize(previewWidth,previewHeight);orient();camera.setPreviewTexture(target);camera.setErrorCallback((code,source)->{if(camera==source&&!closed){releaseCamera();root.setVisibility(View.GONE);failure.accept("Presenter camera unavailable. Screen recording continues; switch cameras or return to retry.");}});camera.startPreview();retries=0;root.setVisibility(View.VISIBLE);transform();
        }catch(Exception e){releaseCamera();if(!closed&&!suspended&&retries++<4)main.postDelayed(()->{if(texture.isAvailable())openCamera(texture.getSurfaceTexture());},300);else{root.setVisibility(View.GONE);failure.accept("Cannot open presenter camera. Screen recording continues. "+e.getMessage());}}
    }
    private void orient(){if(camera==null)return;Display display=displays.getDisplay(Display.DEFAULT_DISPLAY);int degrees=display==null?0:display.getRotation()*90;cameraOrientation=PresenterOrientation.degrees(cameraInfo.orientation,degrees,front());camera.setDisplayOrientation(cameraOrientation);}
    private void transform(){if(closed||previewWidth==0||texture.getWidth()==0||texture.getHeight()==0)return;boolean swap=cameraOrientation==90||cameraOrientation==270;float sourceWidth=swap?previewHeight:previewWidth,sourceHeight=swap?previewWidth:previewHeight;float scale=Math.max(texture.getWidth()/sourceWidth,texture.getHeight()/sourceHeight);Matrix matrix=new Matrix();matrix.setScale(sourceWidth*scale/texture.getWidth(),sourceHeight*scale/texture.getHeight(),texture.getWidth()/2f,texture.getHeight()/2f);texture.setTransform(matrix);texture.setScaleX(front()&&!mirror?-1:1);}
    private void releaseCamera(){if(camera!=null){try{camera.setErrorCallback(null);camera.stopPreview();}catch(Exception ignored){}camera.release();camera=null;}}
    @Override public void onSurfaceTextureAvailable(SurfaceTexture surface,int width,int height){openCamera(surface);}
    @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surface,int width,int height){transform();}
    @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface){releaseCamera();return true;}
    @Override public void onSurfaceTextureUpdated(SurfaceTexture surface){}
    @Override public void onDisplayAdded(int id){}
    @Override public void onDisplayRemoved(int id){if(id==Display.DEFAULT_DISPLAY)failure.accept("Screen display disconnected");}
    @Override public void onDisplayChanged(int id){if(id==Display.DEFAULT_DISPLAY&&!closed){updateScreen();orient();applyBounds(true);}}
    @Override public void close(){if(closed)return;closed=true;main.removeCallbacksAndMessages(null);displays.unregisterDisplayListener(this);releaseCamera();if(attached){try{windows.removeViewImmediate(root);}catch(Exception ignored){}attached=false;}}
}
