package studio.solocamera;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** Local scanner: frames and pairing secrets never leave the phone. */
@SuppressWarnings("deprecation")
public final class QrScanActivity extends Activity implements SurfaceHolder.Callback {
    private final Object cameraOwnership=new Object();private SurfaceView preview;private TextView hint;private Camera camera;
    private final ExecutorService decoder=Executors.newSingleThreadExecutor();
    private final AtomicBoolean decoding=new AtomicBoolean();private volatile boolean found,paused=true;
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|WindowManager.LayoutParams.FLAG_SECURE);
        LinearLayout layout=new LinearLayout(this);layout.setOrientation(LinearLayout.VERTICAL);layout.setBackgroundColor(0xff090b0e);
        hint=new TextView(this);hint.setText("Scan the Solo Camera pairing QR code\nUse the same Wi-Fi or hotspot as the other device.");hint.setTextColor(-1);hint.setTextSize(18);hint.setPadding(24,24,24,24);layout.addView(hint);
        preview=new SurfaceView(this);layout.addView(preview,new LinearLayout.LayoutParams(-1,0,1));preview.getHolder().addCallback(this);
        Button cancel=new Button(this);cancel.setText("Cancel");cancel.setOnClickListener(v->finish());layout.addView(cancel);setContentView(layout);
        if(checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.CAMERA},1);
    }
    @Override protected void onResume(){super.onResume();CameraOwnership.claim(cameraOwnership,"QR scanner");paused=false;open();}
    @Override protected void onPause(){paused=true;release();CameraOwnership.release(cameraOwnership);super.onPause();}
    @Override protected void onDestroy(){decoder.shutdownNow();CameraOwnership.release(cameraOwnership);super.onDestroy();}
    @Override public void onRequestPermissionsResult(int code,String[] names,int[] grants){super.onRequestPermissionsResult(code,names,grants);if(checkSelfPermission(Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)open();else{Toast.makeText(this,"Camera access is needed to scan. Manual pairing is still available.",Toast.LENGTH_LONG).show();finish();}}
    public void surfaceCreated(SurfaceHolder holder){open();}
    public void surfaceChanged(SurfaceHolder holder,int format,int width,int height){release();open();}
    public void surfaceDestroyed(SurfaceHolder holder){release();}
    private void open(){
        if(paused||found||camera!=null||!preview.getHolder().getSurface().isValid()||checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED)return;
        try{
            int id=0;Camera.CameraInfo info=new Camera.CameraInfo();
            for(int i=0;i<Camera.getNumberOfCameras();i++){Camera.getCameraInfo(i,info);if(info.facing==Camera.CameraInfo.CAMERA_FACING_BACK){id=i;break;}}
            Camera.getCameraInfo(id,info);camera=Camera.open(id);Camera.Parameters p=camera.getParameters();
            Camera.Size selected=null;for(Camera.Size size:p.getSupportedPreviewSizes())if(size.width<=1280&&(selected==null||size.width*size.height>selected.width*selected.height))selected=size;
            if(selected!=null)p.setPreviewSize(selected.width,selected.height);
            if(p.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            p.setPreviewFormat(android.graphics.ImageFormat.NV21);camera.setParameters(p);
            int rotation=getWindowManager().getDefaultDisplay().getRotation()*90;
            camera.setDisplayOrientation((info.orientation-rotation+360)%360);camera.setPreviewDisplay(preview.getHolder());
            camera.setPreviewCallback((data,source)->{
                if(found||paused||!decoding.compareAndSet(false,true))return;
                Camera.Size size=source.getParameters().getPreviewSize();byte[] copy=data.clone();
                decoder.execute(()->{try{
                    String raw=new QRCodeReader().decode(new BinaryBitmap(new HybridBinarizer(new PlanarYUVLuminanceSource(copy,size.width,size.height,0,0,size.width,size.height,false)))).getText();
                    if(getIntent().getBooleanExtra("desktop",false))DesktopPairingCode.decode(raw);else PairingCode.decode(raw);found=true;runOnUiThread(()->{if(!paused){setResult(RESULT_OK,new Intent().putExtra("pairing",raw));finish();}else found=false;});
                }catch(IllegalArgumentException invalid){runOnUiThread(()->hint.setText("This is not a valid Solo Camera pairing code. Scan the code on the camera phone."));}
                catch(ReaderException ignored){}finally{decoding.set(false);}});
            });camera.startPreview();
        }catch(Exception failure){release();hint.setText("Unable to open camera. Close other camera apps and try again, or use manual pairing.");}
    }
    private void release(){if(camera!=null){camera.setPreviewCallback(null);camera.release();camera=null;}}
}

