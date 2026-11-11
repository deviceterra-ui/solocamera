package studio.solocamera;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.media.projection.*;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;

/** Screen-capture setup and presenter controls remain separate from the camera workspace. */
public final class ScreenRecordingActivity extends Activity {
    private final Handler main=new Handler(Looper.getMainLooper());
    private TextView status;private Button start,stop,play,mode,flip,pairWindows;private Switch microphone,face,mirror,annotations,live,appAudio;
    private Spinner quality;private SeekBar bubbleSize;private LinearLayout presenterOptions;
    private boolean pendingMicrophone,pendingPresenter,pendingMirror,pendingAnnotations,pendingLive,pendingAppAudio,requesting,awaitingOverlay,hadRecording;
    private AlertDialog liveApproval;private int[] pendingSize;private int pendingDensity,pendingBubbleSize;
    private final Runnable tick=new Runnable(){public void run(){refresh();main.postDelayed(this,500);}};
    private int dp(int value){return (int)(value*getResources().getDisplayMetrics().density);}
    private TextView text(String value,int size){TextView view=new TextView(this);view.setText(value);view.setTextSize(size);view.setTextColor(0xffedf0f3);view.setPadding(0,dp(8),0,dp(8));return view;}
    private Button button(String title,Runnable action){Button view=new Button(this);view.setText(title);view.setOnClickListener(v->action.run());return view;}
    private Switch toggle(String title,boolean checked){Switch view=new Switch(this);view.setText(title);view.setTextColor(-1);view.setChecked(checked);view.setPadding(0,dp(8),0,dp(8));return view;}
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        if(saved!=null){requesting=saved.getBoolean("requesting");awaitingOverlay=saved.getBoolean("awaitingOverlay");pendingLive=saved.getBoolean("live");pendingAppAudio=saved.getBoolean("appAudio");pendingAnnotations=saved.getBoolean("annotations");pendingMicrophone=saved.getBoolean("microphone");pendingPresenter=saved.getBoolean("presenter");pendingMirror=saved.getBoolean("mirror",true);pendingBubbleSize=saved.getInt("bubbleSize",112);pendingSize=saved.getIntArray("size");pendingDensity=saved.getInt("density");}
        getWindow().setStatusBarColor(0xff090b0e);getWindow().setNavigationBarColor(0xff090b0e);
        ScrollView scroll=new ScrollView(this);LinearLayout panel=new LinearLayout(this);panel.setOrientation(LinearLayout.VERTICAL);panel.setPadding(dp(22),dp(20),dp(22),dp(22));panel.setBackgroundColor(0xff090b0e);scroll.addView(panel);setContentView(scroll);
        panel.addView(button("‹ Creator studio",this::finish));panel.addView(text("Screen recording",28));
        panel.addView(text("Record app demos with narration and a movable front-camera circle. Tap the camera to fill the screen, then tap again to return to your demo—all in one recording.",15));
        status=text("Ready to record",18);panel.addView(status);
        panel.addView(text("Video quality · up to 30 fps",13));quality=new Spinner(this);quality.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xff3c4654));
        quality.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,qualityLabels()){
            private View style(View view){TextView text=(TextView)view;text.setTextColor(0xffedf0f3);text.setPadding(dp(12),dp(12),dp(12),dp(12));return text;}
            @Override public View getView(int position,View recycled,ViewGroup parent){return style(super.getView(position,recycled,parent));}
            @Override public View getDropDownView(int position,View recycled,ViewGroup parent){View view=style(super.getDropDownView(position,recycled,parent));view.setBackgroundColor(0xff252b33);return view;}
        });quality.setSelection(getPreferences(0).getInt("quality",0));panel.addView(quality);
        live=toggle("Live screen demo to OBS",getPreferences(0).getBoolean("live",false));panel.addView(live);appAudio=toggle("Include app audio in live demo",getPreferences(0).getBoolean("appAudio",false));panel.addView(appAudio);panel.addView(text("Start screen recording, then Pair Windows and scan its Pair by QR code. Your screen, presenter and annotations go to OBS. Microphone narration and allowed app audio are mixed. A copy is also saved on this phone. Both devices need the same Wi-Fi/hotspot; USB feed is available in the Windows receiver after QR pairing. App audio depends on the demonstrated app allowing capture.",13));
        microphone=toggle("Record microphone narration",getPreferences(0).getBoolean("microphone",false));panel.addView(microphone);
        face=toggle("Show presenter camera · front / back",getPreferences(0).getBoolean("presenter",false));panel.addView(face);
        presenterOptions=new LinearLayout(this);presenterOptions.setOrientation(LinearLayout.VERTICAL);panel.addView(presenterOptions);
        presenterOptions.addView(text("Drag the circle to move it. Tap to maximize; tap again to minimize. Hold to switch front/back, or use the notification Front / back action. The recording keeps running. This mode records the entire screen so the camera stays in the video when you open another app.",13));
        presenterOptions.addView(text("Circle size · small ↔ large",13));bubbleSize=new SeekBar(this);bubbleSize.setMin(80);bubbleSize.setMax(160);bubbleSize.setProgress(getPreferences(0).getInt("bubbleSize",112));bubbleSize.setContentDescription("Presenter circle size");presenterOptions.addView(bubbleSize);
        mirror=toggle("Mirror presenter camera",getPreferences(0).getBoolean("mirror",true));presenterOptions.addView(mirror);
        face.setOnCheckedChangeListener((view,on)->presenterOptions.setVisibility(on?View.VISIBLE:View.GONE));presenterOptions.setVisibility(face.isChecked()?View.VISIBLE:View.GONE);
        annotations=toggle("Pointer, pen & highlighter",getPreferences(0).getBoolean("annotations",false));panel.addView(annotations);panel.addView(text("Floating tools let you point, draw and write on your recording. Choose Touch to use the app underneath. Undo or clear your marks at any time.",13));
        start=button("● Start screen recording",this::requestRecording);panel.addView(start);
        stop=button("■ Stop & save",()->startService(new Intent(this,ScreenRecordingService.class).setAction(ScreenRecordingService.STOP)));panel.addView(stop);
        mode=button("Expand / minimize presenter camera",()->startService(new Intent(this,ScreenRecordingService.class).setAction(ScreenRecordingService.TOGGLE_CAMERA)));panel.addView(mode);
        flip=button("⇄ Switch front / back presenter camera",()->startService(new Intent(this,ScreenRecordingService.class).setAction(ScreenRecordingService.FLIP_CAMERA)));panel.addView(flip);
        pairWindows=button("Pair Windows · scan QR",()->startActivityForResult(new Intent(this,QrScanActivity.class).putExtra("desktop",true),57));panel.addView(pairWindows);
        play=button("▶ Play latest recording",this::playLatest);panel.addView(play);
        panel.addView(button("Open Studio / edit recordings",()->{if(ScreenRecordingService.running||ScreenRecordingService.starting){Toast.makeText(this,"Stop and save the screen recording first.",Toast.LENGTH_SHORT).show();return;}startActivity(new Intent(this,EditorActivity.class));}));
        panel.addView(text("Android asks what to record each time. Stop from the recording notification or this page. Locking the phone stops and saves the recording. Microphone narration is optional; app audio is captured only in live mode and only when the other app permits it. Protected screens may appear black or hide the camera overlay.",12));refresh();
    }
    private String[] qualityLabels(){android.util.DisplayMetrics metrics=new android.util.DisplayMetrics();getWindowManager().getDefaultDisplay().getRealMetrics(metrics);int[] a=ScreenRecordingConfig.size(metrics.widthPixels,metrics.heightPixels,720),b=ScreenRecordingConfig.size(metrics.widthPixels,metrics.heightPixels,1080);return new String[]{a[0]+" × "+a[1]+" · smaller files",b[0]+" × "+b[1]+" · sharper detail"};}
    private void requestRecording(){
        if(requesting||ScreenRecordingService.running||ScreenRecordingService.starting)return;
        getPreferences(0).edit().putBoolean("live",live.isChecked()).putBoolean("appAudio",appAudio.isChecked()).putBoolean("annotations",annotations.isChecked()).putInt("quality",quality.getSelectedItemPosition()).putBoolean("microphone",microphone.isChecked()).putBoolean("presenter",face.isChecked()).putBoolean("mirror",mirror.isChecked()).putInt("bubbleSize",bubbleSize.getProgress()).apply();
        if(face.isChecked()&&checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.CAMERA},55);return;}
        if((face.isChecked()||annotations.isChecked())&&!Settings.canDrawOverlays(this)){
            new AlertDialog.Builder(this).setTitle("Allow floating recording tools").setMessage("Enable Display over other apps for Solo Camera to show the presenter camera or drawing tools over your demo. You can cancel and turn those options off to record without floating tools.").setPositiveButton("Open settings",(dialog,which)->{awaitingOverlay=true;requesting=true;startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())),54);}).setNegativeButton("Cancel",null).show();return;
        }
        if((microphone.isChecked()||(live.isChecked()&&appAudio.isChecked()))&&checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},51);return;}
        if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},52);return;}
        requestConsent();
    }
    private void requestConsent(){
        requesting=true;pendingLive=live.isChecked();pendingAppAudio=live.isChecked()&&appAudio.isChecked();pendingAnnotations=annotations.isChecked();pendingMicrophone=microphone.isChecked();pendingPresenter=face.isChecked();pendingMirror=mirror.isChecked();pendingBubbleSize=bubbleSize.getProgress();
        android.util.DisplayMetrics metrics=new android.util.DisplayMetrics();getWindowManager().getDefaultDisplay().getRealMetrics(metrics);pendingSize=ScreenRecordingConfig.size(metrics.widthPixels,metrics.heightPixels,quality.getSelectedItemPosition()==1?1080:720);pendingDensity=metrics.densityDpi;refresh();
        try{MediaProjectionManager manager=getSystemService(MediaProjectionManager.class);Intent capture=Build.VERSION.SDK_INT>=34&&(pendingPresenter||pendingAnnotations||pendingLive)?manager.createScreenCaptureIntent(MediaProjectionConfig.createConfigForDefaultDisplay()):manager.createScreenCaptureIntent();startActivityForResult(capture,53);}catch(Exception e){requesting=false;status.setText("Screen recording is unavailable: "+e.getMessage());}
    }
    @Override public void onRequestPermissionsResult(int code,String[] names,int[] grants){
        super.onRequestPermissionsResult(code,names,grants);
        if(code==51||code==55){String permission=code==51?Manifest.permission.RECORD_AUDIO:Manifest.permission.CAMERA;if(checkSelfPermission(permission)==PackageManager.PERMISSION_GRANTED)requestRecording();else{if(code==51)microphone.setChecked(false);else face.setChecked(false);if(code==51)appAudio.setChecked(false);Toast.makeText(this,"Permission denied. You can record without "+(code==51?"narration":"the presenter camera")+".",Toast.LENGTH_LONG).show();}}else if(code==52)requestConsent();
    }
    @Override protected void onActivityResult(int code,int result,Intent data){
        super.onActivityResult(code,result,data);
        if(code==57){if(result==RESULT_OK&&data!=null&&ScreenRecordingService.livePeer!=null){try{String[] target=DesktopPairingCode.decode(data.getStringExtra("pairing"));new AlertDialog.Builder(this).setTitle("Share screen and selected audio with Windows?").setMessage("Connect to the computer at "+target[0]+" on your local network?").setPositiveButton("Connect",(d,w)->{ScreenLivePeer peer=ScreenRecordingService.livePeer;if(peer!=null)peer.register(target);}).setNegativeButton("Cancel",null).show();}catch(Exception e){Toast.makeText(this,"Invalid Windows QR code",Toast.LENGTH_SHORT).show();}}return;}
        if(code==54){awaitingOverlay=false;requesting=false;if(Settings.canDrawOverlays(this))requestRecording();else Toast.makeText(this,"Display over other apps is needed for floating recording tools.",Toast.LENGTH_LONG).show();return;}
        if(code!=53)return;requesting=false;
        if(result!=RESULT_OK||data==null){Toast.makeText(this,"Screen recording canceled",Toast.LENGTH_SHORT).show();refresh();return;}
        if(pendingSize==null){Toast.makeText(this,"Please start a new screen recording.",Toast.LENGTH_SHORT).show();return;}
        try{Intent service=new Intent(this,ScreenRecordingService.class).putExtra("live",pendingLive).putExtra("appAudio",pendingAppAudio).putExtra("annotations",pendingAnnotations).putExtra("consent",data).putExtra("microphone",pendingMicrophone).putExtra("presenter",pendingPresenter).putExtra("mirror",pendingMirror).putExtra("bubbleSize",pendingBubbleSize).putExtra("width",pendingSize[0]).putExtra("height",pendingSize[1]).putExtra("density",pendingDensity);startForegroundService(service);status.setText("Starting screen recording…");}catch(Exception e){status.setText("Could not start: "+e.getMessage());}
    }
    private void refresh(){
        if(start==null)return;boolean busy=ScreenRecordingService.running||ScreenRecordingService.starting;start.setEnabled(!busy&&!requesting);stop.setEnabled(busy);live.setEnabled(!busy&&!requesting);appAudio.setEnabled(!busy&&!requesting);pairWindows.setVisibility(ScreenRecordingService.livePeer!=null?View.VISIBLE:View.GONE);annotations.setEnabled(!busy&&!requesting);microphone.setEnabled(!busy&&!requesting);quality.setEnabled(!busy&&!requesting);face.setEnabled(!busy&&!requesting);mirror.setEnabled(!busy&&!requesting);bubbleSize.setEnabled(!busy&&!requesting);flip.setVisibility(ScreenRecordingService.faceEnabled?View.VISIBLE:View.GONE);mode.setVisibility(ScreenRecordingService.faceEnabled?View.VISIBLE:View.GONE);mode.setEnabled(ScreenRecordingService.running&&!ScreenRecordingService.presenterPaused);flip.setEnabled(ScreenRecordingService.running);flip.setText("⇄ Switch to "+(ScreenRecordingService.presenterFront?"back":"front")+" presenter camera");play.setEnabled(!busy&&!ScreenRecordingService.preferences(this).getString("lastUri","").isEmpty());
        if(ScreenRecordingService.running){long seconds=(SystemClock.elapsedRealtime()-ScreenRecordingService.startedAt)/1000;status.setText(String.format(java.util.Locale.US,"● Recording · %02d:%02d%s",seconds/60,seconds%60,ScreenRecordingService.faceEnabled?(ScreenRecordingService.presenterPaused?" · presenter paused for camera/scanner":(ScreenRecordingService.presenterFront?" · front":" · back")+(ScreenRecordingService.presenterFullscreen?" full-screen camera":" camera circle")):""));ScreenLivePeer peer=ScreenRecordingService.livePeer;if(peer!=null){status.append("\nOBS: "+peer.status);if(peer.approval!=null&&liveApproval==null){String address=peer.approvalAddress;liveApproval=new AlertDialog.Builder(this).setTitle("Allow live screen connection?").setMessage("Share your screen and selected audio with "+address+"?").setPositiveButton("Allow",(d,w)->peer.answer(true)).setNegativeButton("Reject",(d,w)->peer.answer(false)).create();liveApproval.setOnDismissListener(d->liveApproval=null);liveApproval.show();}}String audioError=ScreenRecordingService.preferences(this).getString("liveAudioError","");if(!audioError.isEmpty())status.append("\n"+audioError);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);hadRecording=true;}else{getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);String error=ScreenRecordingService.preferences(this).getString("error","");status.setText(ScreenRecordingService.starting?"Starting screen recording…":requesting?"Waiting for Android approval…":!error.isEmpty()?error:hadRecording?"Saved · ready to play or edit":"Ready to record");String liveError=ScreenRecordingService.preferences(this).getString("liveAudioError","");if(!liveError.isEmpty())status.append("\n"+liveError);}
    }
    private void playLatest(){String saved=ScreenRecordingService.preferences(this).getString("lastUri","");if(saved.isEmpty())return;try{startActivity(new Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse(saved),"video/mp4").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));}catch(Exception e){Toast.makeText(this,"Open Studio to play this recording.",Toast.LENGTH_LONG).show();}}
    @Override protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putBoolean("live",pendingLive);out.putBoolean("appAudio",pendingAppAudio);out.putBoolean("annotations",pendingAnnotations);out.putBoolean("requesting",requesting);out.putBoolean("awaitingOverlay",awaitingOverlay);out.putBoolean("microphone",pendingMicrophone);out.putBoolean("presenter",pendingPresenter);out.putBoolean("mirror",pendingMirror);out.putInt("bubbleSize",pendingBubbleSize);out.putIntArray("size",pendingSize);out.putInt("density",pendingDensity);}
    @Override protected void onResume(){super.onResume();main.post(tick);}
    @Override protected void onPause(){if(liveApproval!=null){liveApproval.dismiss();liveApproval=null;}main.removeCallbacks(tick);super.onPause();}
}
