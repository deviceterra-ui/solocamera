package studio.solocamera;



import android.Manifest;

import android.app.*;

import android.content.pm.PackageManager;

import android.graphics.*;
import android.graphics.drawable.GradientDrawable;

import android.os.*;

import android.view.*;

import android.widget.*;

import org.json.*;

import java.net.*;

import java.util.*;

import java.util.concurrent.atomic.AtomicBoolean;



public final class MainActivity extends Activity {

    private static final int BACK=0xff090b0e, PANEL=0xff1b2027, TEXT=0xffedf0f3, ACCENT=0xffffc857;

    private final Handler main=new Handler(Looper.getMainLooper());private final AtomicBoolean rendering=new AtomicBoolean();
    private final java.util.concurrent.ExecutorService previewDecoder=java.util.concurrent.Executors.newSingleThreadExecutor();

    private LinearLayout root,work,toolbar,body,captureBar;private FrameLayout stage;private ImageView image;private TextView status,frameStatus,recordTime,formatBadge,audio;

    private RecordButton record;private ImageButton scriptToggle,gridToggle,torchToggle,stabilizationToggle,flipToggle;private PrompterView prompter;private CheckBox linked,sync;

    private CameraControlsView cameraControls;private PreviewGuides guides;

    private Object cameraOwnership;private CameraEngine camera;private volatile PeerLink peer;private volatile JSONObject state=new JSONObject();

    private volatile String role="";private String key="",connection="Not paired";private boolean scriptVisible=false,overlay=false,wasRecording=false,active=false,gridVisible;

    private int previewFps,previewCount;private long previewSampleStart;
    private long lastState,lastFrame;private volatile int epoch;private int countdown=0;private boolean counting;private int countdownToken;

    private volatile boolean zebraEnabled,peakingEnabled;private volatile int zebraThreshold=235;private long lastSignal;private CreatorRecorder micCheck;private android.media.ToneGenerator countdownTone;private AlertDialog pairingDialog;private boolean returningToPair;private String[] desktopPairing;



    @Override public void onCreate(Bundle saved){super.onCreate(saved);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);getWindow().setStatusBarColor(BACK);getWindow().setNavigationBarColor(BACK);countdown=getSharedPreferences("creator-camera",0).getInt("countdown",0);home();}

    private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density);}

    private TextView label(String value,int size){TextView t=new TextView(this);t.setText(value);t.setTextColor(TEXT);t.setTextSize(size);t.setPadding(dp(8),dp(5),dp(8),dp(5));return t;}

    private void setText(TextView view,String value){if(!android.text.TextUtils.equals(view.getText(),value))view.setText(value);}

    private Button button(String title,Runnable action){Button b=new Button(this);b.setText(title);b.setTextSize(12);b.setTextColor(TEXT);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(PANEL));b.setOnClickListener(v->action.run());return b;}

    private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}

    private ImageButton iconButton(String icon,String description,Runnable action){ImageButton b=new ImageButton(this);b.setImageDrawable(new CameraIcon(icon,TEXT));b.setBackgroundColor(android.graphics.Color.TRANSPARENT);b.setPadding(dp(10),dp(10),dp(10),dp(10));b.setContentDescription(description);b.setTooltipText(description);b.setOnClickListener(v->action.run());return b;}

    private void openEditor(){if(state.optBoolean("recording")||state.optBoolean("busy")){toast("Stop recording before opening the editor.");return;}startActivityForResult(new android.content.Intent(this,EditorActivity.class),30);}

    private void home(){
        root=column();root.setBackgroundColor(BACK);setContentView(root);ScrollView page=new ScrollView(this);page.setFillViewport(true);LinearLayout content=column();content.setPadding(dp(22),dp(28),dp(22),dp(22));page.addView(content);root.addView(page,new LinearLayout.LayoutParams(-1,-1));
        TextView eyebrow=label("SOLO CAMERA  ·  CREATOR STUDIO",11);eyebrow.setTextColor(0xffffc857);eyebrow.setTypeface(android.graphics.Typeface.DEFAULT,android.graphics.Typeface.BOLD);content.addView(eyebrow);TextView title=label("Take your story.",30);title.setTypeface(android.graphics.Typeface.DEFAULT,android.graphics.Typeface.BOLD);title.setPadding(dp(7),dp(7),dp(7),dp(4));content.addView(title);TextView subtitle=label("Shoot solo. Monitor remotely. Finish your edit.",14);subtitle.setTextColor(0xffaeb8c4);content.addView(subtitle,new LinearLayout.LayoutParams(-1,dp(38)));
        content.addView(label("START",10),new LinearLayout.LayoutParams(-1,dp(32)));
        content.addView(roleCard("camera","Camera","Shoot with full camera controls and the teleprompter.",0xffffc857,()->chooseRole("camera")),new LinearLayout.LayoutParams(-1,dp(100)));
        content.addView(roleCard("monitor","Monitor","Pair a second phone to frame and control your shot.",0xff74c9df,()->chooseRole("monitor")),new LinearLayout.LayoutParams(-1,dp(100)));
        content.addView(roleCard("monitor","Webcam","Scan the Windows QR code to use this phone as a webcam.",0xff88dba8,()->startActivityForResult(new android.content.Intent(this,QrScanActivity.class).putExtra("desktop",true),40)),new LinearLayout.LayoutParams(-1,dp(100)));
        content.addView(roleCard("screen-record","Screen recording","Record app demos and tutorials, then edit them in Studio.",0xffff8c9a,()->startActivity(new android.content.Intent(this,ScreenRecordingActivity.class))),new LinearLayout.LayoutParams(-1,dp(100)));
        content.addView(roleCard("film","Studio","Browse takes, edit projects, and export your video.",0xffc2a4ff,this::openEditor),new LinearLayout.LayoutParams(-1,dp(100)));
        LinearLayout schedule=new LinearLayout(this);schedule.setGravity(Gravity.CENTER_VERTICAL);schedule.setPadding(dp(14),dp(8),dp(14),dp(8));schedule.setBackground(cardBackground(0xff20232b,0xff34313f));ImageView calendar=new ImageView(this);calendar.setImageDrawable(new CameraIcon("calendar",0xffba9cfa));schedule.addView(calendar,new LinearLayout.LayoutParams(dp(36),dp(36)));LinearLayout labels=column();labels.addView(label("Schedule & publish",14));TextView planned=label("Plan, queue, and publish your finished videos",11);planned.setTextColor(0xffa7a2b2);labels.addView(planned);LinearLayout.LayoutParams scheduleText=new LinearLayout.LayoutParams(0,-2,1);scheduleText.leftMargin=dp(12);schedule.addView(labels,scheduleText);TextView badge=label("OPEN  ›",9);badge.setTextColor(0xffba9cfa);schedule.addView(badge);LinearLayout.LayoutParams slp=new LinearLayout.LayoutParams(-1,dp(70));slp.topMargin=dp(12);content.addView(schedule,slp);schedule.setOnClickListener(v->startActivity(new android.content.Intent(this,PublishActivity.class)));
        TextView pairing=label("For remote monitoring, connect the monitor phone to the camera phone’s hotspot or put both phones on the same Wi‑Fi.",12);pairing.setTextColor(0xff8994a2);pairing.setPadding(dp(8),dp(20),dp(8),dp(8));content.addView(pairing);TextView note=label("Android 10+  ·  Keep the camera app visible while recording.",10);note.setTextColor(0xff687482);content.addView(note);
    }

    private GradientDrawable cardBackground(int fill,int stroke){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{fill,0xff171b22});d.setCornerRadius(dp(18));d.setStroke(dp(1),stroke);return d;}
    private View roleCard(String icon,String title,String detail,int accent,Runnable action){LinearLayout card=new LinearLayout(this);card.setGravity(Gravity.CENTER_VERTICAL);card.setPadding(dp(15),dp(10),dp(12),dp(10));card.setBackground(cardBackground(0xff222831,0xff363e49));ImageView symbol=new ImageView(this);symbol.setImageDrawable(new CameraIcon(icon,accent));symbol.setBackground(cardBackground(0xff171c23,accent));symbol.setPadding(dp(13),dp(13),dp(13),dp(13));card.addView(symbol,new LinearLayout.LayoutParams(dp(58),dp(58)));LinearLayout words=column();words.addView(label(title,16));TextView text=label(detail,11);text.setTextColor(0xffaab4c0);words.addView(text);LinearLayout.LayoutParams textParams=new LinearLayout.LayoutParams(0,-2,1);textParams.leftMargin=dp(14);textParams.rightMargin=dp(8);card.addView(words,textParams);TextView arrow=label("›",28);arrow.setTextColor(accent);card.addView(arrow);card.setOnClickListener(v->action.run());return card;}

    private void chooseRole(String next){

        cleanup();role=next;state=new JSONObject();wasRecording=false;lastState=0;lastFrame=0;buildWorkspace();

        if(role.equals("camera")){

            if(checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED||checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)

                requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO},10);

            else startCamera();

        }else pair();

    }

    private void buildWorkspace(){

        root=column();root.setBackgroundColor(BACK);

        root.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets;});setContentView(root);

        toolbar=new LinearLayout(this);toolbar.setGravity(Gravity.CENTER_VERTICAL);

        toolbar.addView(iconButton("back","Leave camera workspace",()->exitRole()),new LinearLayout.LayoutParams(dp(44),dp(44)));

        TextView brand=label(role.equals("camera")?"SOLO / CAMERA":"SOLO / MONITOR",11);brand.setLetterSpacing(.1f);brand.setGravity(Gravity.CENTER_VERTICAL);toolbar.addView(brand,new LinearLayout.LayoutParams(0,-1,1));

        toolbar.addView(iconButton("film","Playback and editing",this::openEditor),new LinearLayout.LayoutParams(dp(44),dp(44)));

        toolbar.addView(iconButton("pair","Pair phones",()->pair()),new LinearLayout.LayoutParams(dp(44),dp(44)));

        gridToggle=iconButton("grid","Composition grid",()->{gridVisible=!gridVisible;guides.grid(gridVisible);gridToggle.setImageDrawable(new CameraIcon("grid",gridVisible?ACCENT:TEXT));});toolbar.addView(gridToggle,new LinearLayout.LayoutParams(dp(44),dp(44)));

        gridToggle.setOnLongClickListener(v->{previewTools();return true;});
        scriptToggle=iconButton("script","Teleprompter",()->{scriptVisible=!scriptVisible;scriptToggle.setImageDrawable(new CameraIcon("script",scriptVisible?ACCENT:TEXT));arrange();});toolbar.addView(scriptToggle,new LinearLayout.LayoutParams(dp(44),dp(44)));

        toolbar.addView(iconButton("settings","Shooting setup",()->recordingSetup()),new LinearLayout.LayoutParams(dp(44),dp(44)));

        status=label("Preparing camera",10);status.setSingleLine();status.setEllipsize(android.text.TextUtils.TruncateAt.END);status.setTextColor(0xff929ca8);status.setOnClickListener(v->showStatus());

        body=new LinearLayout(this);work=column();

        stage=new FrameLayout(this);stage.setBackgroundColor(0xff000000);image=new ImageView(this);image.setScaleType(ImageView.ScaleType.FIT_CENTER);stage.addView(image,new FrameLayout.LayoutParams(-1,-1));

        guides=new PreviewGuides(this,image);restoreGuides();stage.addView(guides,new FrameLayout.LayoutParams(-1,-1));

        image.setContentDescription("Camera preview. Tap to focus");image.setOnTouchListener((v,event)->{if(event.getAction()!=MotionEvent.ACTION_UP)return true;android.graphics.drawable.Drawable drawable=image.getDrawable();if(drawable==null)return true;float[] point={event.getX(),event.getY()};Matrix inverse=new Matrix();image.getImageMatrix().invert(inverse);inverse.mapPoints(point);double x=point[0]/drawable.getIntrinsicWidth(),y=point[1]/drawable.getIntrinsicHeight();if(x<0||x>1||y<0||y>1)return true;int rotation=state.optInt("rotation");double rawX=x,rawY=y;if(rotation==90){rawX=y;rawY=1-x;}else if(rotation==180){rawX=1-x;rawY=1-y;}else if(rotation==270){rawX=1-y;rawY=x;}try{JSONObject p=new JSONObject();p.put("x",rawX);p.put("y",rawY);command("tap",p);guides.focus(event.getX(),event.getY());}catch(Exception ignored){}v.performClick();return true;});

        frameStatus=label("Waiting for preview",11);frameStatus.setBackgroundColor(0xb0090b0e);FrameLayout.LayoutParams f=new FrameLayout.LayoutParams(-2,-2,Gravity.TOP|Gravity.CENTER_HORIZONTAL);f.topMargin=dp(8);stage.addView(frameStatus,f);

        prompter=new PrompterView(this);prompter.onChanged(()->sendScript());arrange();

        linked=new CheckBox(this);linked.setText("Scroll when recording starts");linked.setTextColor(TEXT);

        sync=new CheckBox(this);sync.setText(role.equals("monitor")?"Send script to camera screen":"Allow monitor to display a script here");sync.setTextColor(TEXT);sync.setOnCheckedChangeListener((b,checked)->{if(checked)sendScript();});

        cameraControls=new CameraControlsView(this,this::command);

        captureBar=new LinearLayout(this);captureBar.setGravity(Gravity.CENTER);captureBar.setBackgroundColor(BACK);

        torchToggle=iconButton("torch","Toggle camera light",()->command("torch",!state.optBoolean("torch")));

        stabilizationToggle=iconButton("stabilization","Cycle available stabilization modes",()->cycleStabilization());

        flipToggle=iconButton("flip","Switch front / back camera",()->command("flip",null));
        formatBadge=label("VIDEO",10);formatBadge.setGravity(Gravity.CENTER);formatBadge.setTextColor(0xffffc857);formatBadge.setContentDescription("Video resolution and quality");formatBadge.setOnClickListener(v->videoSetup());

        record=new RecordButton(this);record.setOnClickListener(v->recordPressed());

        recordTime=label("00:00",12);recordTime.setGravity(Gravity.CENTER);recordTime.setTypeface(android.graphics.Typeface.MONOSPACE);recordTime.setContentDescription("Take markers: tap to mark good, hold to mark mistake");recordTime.setOnClickListener(v->command("marker","good"));recordTime.setOnLongClickListener(v->{command("marker","mistake");return true;});

        audio=label("MIC",10);audio.setContentDescription("Microphone input and recording route");audio.setOnClickListener(v->microphoneSetup());audio.setGravity(Gravity.CENTER);audio.setTextColor(0xff929ca8);

        layoutWorkspace();

    }

    private void detach(View v){if(v.getParent()!=null)((ViewGroup)v.getParent()).removeView(v);}

    private void layoutWorkspace(){

        boolean landscape=getResources().getConfiguration().orientation==android.content.res.Configuration.ORIENTATION_LANDSCAPE;

        for(View v:new View[]{toolbar,status,body,work,cameraControls,captureBar,torchToggle,stabilizationToggle,flipToggle,formatBadge,record,recordTime,audio})detach(v);root.removeAllViews();body.removeAllViews();captureBar.removeAllViews();

        root.addView(toolbar,new LinearLayout.LayoutParams(-1,dp(44)));root.addView(status,new LinearLayout.LayoutParams(-1,dp(24)));

        body.setOrientation(landscape?LinearLayout.HORIZONTAL:LinearLayout.VERTICAL);body.addView(work,new LinearLayout.LayoutParams(landscape?0:-1,landscape?-1:0,1));root.addView(body,new LinearLayout.LayoutParams(-1,0,1));

        cameraControls.sideDock(landscape);if(landscape)body.addView(cameraControls.adjustmentDock(),new LinearLayout.LayoutParams(dp(Math.max(180,Math.min(240,getResources().getDisplayMetrics().widthPixels/getResources().getDisplayMetrics().density*.28f))),-1));

        root.addView(cameraControls,new LinearLayout.LayoutParams(-1,-2));

        captureBar.setOrientation(landscape?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);

        if(landscape){captureBar.addView(formatBadge,new LinearLayout.LayoutParams(-1,dp(36)));captureBar.addView(record,new LinearLayout.LayoutParams(dp(72),dp(72)));captureBar.addView(recordTime,new LinearLayout.LayoutParams(-1,dp(24)));LinearLayout actions=new LinearLayout(this);actions.addView(torchToggle,new LinearLayout.LayoutParams(dp(40),dp(40)));actions.addView(stabilizationToggle,new LinearLayout.LayoutParams(dp(40),dp(40)));captureBar.addView(actions);captureBar.addView(flipToggle,new LinearLayout.LayoutParams(dp(44),dp(40)));captureBar.addView(audio,new LinearLayout.LayoutParams(-1,dp(30)));body.addView(captureBar,new LinearLayout.LayoutParams(dp(80),-1));}

        else{LinearLayout left=column();left.setGravity(Gravity.CENTER);LinearLayout actions=new LinearLayout(this);actions.addView(torchToggle,new LinearLayout.LayoutParams(dp(44),dp(40)));actions.addView(stabilizationToggle,new LinearLayout.LayoutParams(dp(44),dp(40)));actions.addView(flipToggle,new LinearLayout.LayoutParams(dp(44),dp(40)));left.addView(actions);left.addView(formatBadge,new LinearLayout.LayoutParams(-1,dp(22)));captureBar.addView(left,new LinearLayout.LayoutParams(0,-1,1));captureBar.addView(record,new LinearLayout.LayoutParams(dp(76),dp(76)));LinearLayout right=column();right.setGravity(Gravity.CENTER);right.addView(recordTime);right.addView(audio);captureBar.addView(right,new LinearLayout.LayoutParams(0,-1,1));root.addView(captureBar,new LinearLayout.LayoutParams(-1,dp(80)));}

        arrange();

    }

    private void cycleStabilization(){if(state.optBoolean("recording")){toast("Stop recording to change stabilization");return;}ArrayList<Integer> modes=new ArrayList<>();modes.add(0);if(state.optBoolean("videoStabilization"))modes.add(1);if(state.optBoolean("opticalStabilization"))modes.add(2);int index=modes.indexOf(state.optInt("stabilization"));command("stabilization",modes.get((index+1)%modes.size()));}

    private void showStatus(){new AlertDialog.Builder(this).setTitle("Camera status").setMessage(connection+"\nPreview: "+previewFps+" fps\nCamera cadence: "+state.optInt("sensorFps")+" fps measured / "+state.optInt("videoFps")+" requested\nRecording microphone: "+state.optString("audioRoute","Not recording")+"\n\n"+state.optString("notice")+"\n"+state.optString("format")+"\nBattery "+state.optInt("battery")+"% · Free "+state.optLong("freeMB")+" MB\n\nKeep the camera app visible while recording.").setPositiveButton("Done",null).show();}

    private void arrange(){

        if(stage.getParent()!=null)((android.view.ViewGroup)stage.getParent()).removeView(stage);

        if(prompter.getParent()!=null)((android.view.ViewGroup)prompter.getParent()).removeView(prompter);

        work.removeAllViews();

        if(overlay){work.addView(stage,new LinearLayout.LayoutParams(-1,-1));if(scriptVisible){prompter.setAlpha(.88f);FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(-1,-1);p.topMargin=dp(32);stage.addView(prompter,p);}}

        else{prompter.setAlpha(1);boolean landscape=getResources().getConfiguration().orientation==android.content.res.Configuration.ORIENTATION_LANDSCAPE;work.setOrientation(landscape?LinearLayout.HORIZONTAL:LinearLayout.VERTICAL);work.addView(stage,new LinearLayout.LayoutParams(landscape?0:-1,landscape?-1:0,1));if(scriptVisible)work.addView(prompter,new LinearLayout.LayoutParams(landscape?0:-1,landscape?-1:0,1));}

    }

    @Override public void onRequestPermissionsResult(int code,String[] permissions,int[] grants){super.onRequestPermissionsResult(code,permissions,grants);if(code==10&&role.equals("camera")){if(checkSelfPermission(Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)startCamera();else{toast("Camera permission denied. You can still use Monitor mode.");exitRole();}}}

    private void startCamera(){

        if(!active||camera!=null)return;cameraOwnership=new Object();CameraOwnership.claim(cameraOwnership,"Camera workspace");int current=epoch;

        camera=new CameraEngine(this,new CameraEngine.Listener(){

            public void state(JSONObject s){main.post(()->{if(epoch!=current)return;receiveState(s);if(peer!=null)peer.send(s);});}

            public void frame(byte[] bytes,int rotation){if(epoch!=current)return;displayFrame(bytes,rotation,current);PeerLink p=peer;if(p!=null)p.frame(bytes);}

            public void error(String error){main.post(()->{if(epoch==current)toast(error);});}

        });camera.open();camera.orientation(getWindowManager().getDefaultDisplay().getRotation());if(desktopPairing!=null){String[] target=desktopPairing;desktopPairing=null;registerDesktop(target);}

    }

    private void registerDesktop(String[] target){
        if(peer!=null){peer.close();peer=null;}key=SecureChannel.newSecret();String cameraKey=key;int current=epoch;peer=newPeer();peer.host(cameraKey);connection="Connecting to Windows…";refresh();
        new Thread(()->{try(java.net.Socket socket=new java.net.Socket()){socket.connect(new java.net.InetSocketAddress(target[0],Integer.parseInt(target[1])),7000);try(SecureChannel channel=SecureChannel.client(socket,target[2])){socket.setSoTimeout(8000);if(!"APPROVED".equals(new String(channel.receive(),java.nio.charset.StandardCharsets.UTF_8)))throw new Exception("Not approved");channel.send(cameraKey.getBytes(java.nio.charset.StandardCharsets.UTF_8));if(!"REGISTERED".equals(new String(channel.receive(),java.nio.charset.StandardCharsets.UTF_8)))throw new Exception("Not registered");main.post(()->{if(epoch==current)toast("Windows found. Approve the camera connection to go live.");});}}catch(Exception e){main.post(()->{if(epoch==current){connection="Windows pairing failed";refresh();toast("Unable to reach Windows. Use the same Wi-Fi or hotspot, check the Windows firewall, and scan a fresh code.");}});}},"desktop-pairing").start();
    }
    private void receiveState(JSONObject s){

        state=s;lastState=SystemClock.elapsedRealtime();boolean recording=s.optBoolean("recording");

        if(linked.isChecked()&&recording!=wasRecording){if(recording)prompter.playAndNotify();else prompter.pauseAndNotify();}

        wasRecording=recording;refresh();

    }

    private void refresh(){if(role.isEmpty()||status==null)return;

        long now=SystemClock.elapsedRealtime();boolean stale=role.equals("monitor")&&(peer==null||!peer.connected()||now-lastState>5000);

        String time=String.format(Locale.US,"%02d:%02d",state.optLong("elapsed")/60000,state.optLong("elapsed")/1000%60);

        boolean available=!stale&&!state.optBoolean("busy")&&state.optBoolean("ready");

        String notice=state.optString("notice");String prefix=stale?"Disconnected":state.optBoolean("busy")?"Workingâ€¦":notice.startsWith("Saved")?"Saved":state.optBoolean("recording")?"REC":"Ready";

        setText(status,prefix+"  ·  "+state.optInt("battery")+"%  ·  "+String.format(Locale.US,"%.1f GB",state.optLong("freeMB")/1000.0)+" free"+(state.optInt("thermal")>=3?"  ·  THERMAL WARNING":"")+(peer!=null&&peer.connected()?"  ·  LINK":""));

        record.setEnabled(available);record.state(state.optBoolean("recording"),state.optBoolean("busy"));if(!counting)record.countdown(0);

        setText(recordTime,state.optBoolean("recording")?time:"00:00");recordTime.setTextColor(state.optBoolean("recording")?0xffff4d55:TEXT);

        setText(formatBadge,state.optString("videoLabel","VIDEO"));flipToggle.setEnabled(available&&!state.optBoolean("recording")&&state.optBoolean("canFlip"));flipToggle.setAlpha(flipToggle.isEnabled()?1:.35f);formatBadge.setEnabled(available&&!state.optBoolean("recording"));setText(audio,state.optBoolean("recording")?Math.round(state.optDouble("audioDb",-96))+" dBFS":"MIC");

        torchToggle.setEnabled(available&&state.optBoolean("torchSupported"));torchToggle.setAlpha(torchToggle.isEnabled()?1:.35f);if(!Objects.equals(torchToggle.getTag(),state.optBoolean("torch"))){torchToggle.setTag(state.optBoolean("torch"));torchToggle.setImageDrawable(new CameraIcon("torch",state.optBoolean("torch")?ACCENT:TEXT));}

        stabilizationToggle.setEnabled(available&&!state.optBoolean("recording")&&(state.optBoolean("videoStabilization")||state.optBoolean("opticalStabilization")));stabilizationToggle.setAlpha(stabilizationToggle.isEnabled()?1:.35f);if(!Objects.equals(stabilizationToggle.getTag(),state.optInt("stabilization"))){stabilizationToggle.setTag(state.optInt("stabilization"));stabilizationToggle.setImageDrawable(new CameraIcon("stabilization",state.optInt("stabilization")>0?ACCENT:TEXT));}stabilizationToggle.setContentDescription("Stabilization: "+(state.optInt("stabilization")==1?"electronic":state.optInt("stabilization")==2?"optical":"off"));

        cameraControls.update(state,available);

        frameStatus.setVisibility(now-lastFrame>2500?View.VISIBLE:View.GONE);setText(frameStatus,role.equals("monitor")&&(peer==null||!peer.connected())?connection:role.equals("monitor")&&now-lastState>5000?"Link stalled  -  reconnect to camera":lastFrame==0?"Connected  -  waiting for camera frames": "Preview paused  -  check camera phone");frameStatus.setTextColor(0xffffbd94);audio.setTextColor(state.optDouble("audioDb",-96)>-2?0xffff4d55:0xff929ca8);

        if(notice.startsWith("Rejected:")||notice.startsWith("Camera failure"))setText(status,notice);

    }

    private void displayFrame(byte[] bytes,int rotation,int current){
        if(!rendering.compareAndSet(false,true))return;
        try{previewDecoder.execute(()->{
            try{Bitmap bitmap=BitmapFactory.decodeByteArray(bytes,0,bytes.length);if(bitmap==null){rendering.set(false);return;}
                if(rotation!=0){Matrix m=new Matrix();m.postRotate(rotation);Bitmap rotated=Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),m,true);if(rotated!=bitmap)bitmap.recycle();bitmap=rotated;}
                Bitmap analysis=null;long analysisTime=SystemClock.elapsedRealtime();if((zebraEnabled||peakingEnabled)&&analysisTime-lastSignal>=180&&state.optInt("thermal")<3){lastSignal=analysisTime;int w=Math.min(240,bitmap.getWidth()),h=Math.max(1,bitmap.getHeight()*w/bitmap.getWidth());Bitmap small=Bitmap.createScaledBitmap(bitmap,w,h,false);int[] pixels=new int[w*h];small.getPixels(pixels,0,w,0,0,w,h);if(small!=bitmap)small.recycle();CreatorSignal.overlay(pixels,w,h,zebraEnabled,peakingEnabled,zebraThreshold);analysis=Bitmap.createBitmap(pixels,w,h,Bitmap.Config.ARGB_8888);}final Bitmap signal=analysis;
                final Bitmap frame=bitmap;main.post(()->{try{if(current==epoch&&image!=null){image.setImageBitmap(frame);if(guides!=null){if(signal!=null)guides.signal(signal);else if(!zebraEnabled&&!peakingEnabled||state.optInt("thermal")>=3)guides.signal(null);}lastFrame=SystemClock.elapsedRealtime();if(previewSampleStart==0)previewSampleStart=lastFrame;previewCount++;if(lastFrame-previewSampleStart>=1000){previewFps=(int)Math.round(previewCount*1000.0/(lastFrame-previewSampleStart));previewCount=0;previewSampleStart=lastFrame;}}else{frame.recycle();if(signal!=null)signal.recycle();}}finally{rendering.set(false);}});
            }catch(Exception e){rendering.set(false);}
        });}catch(java.util.concurrent.RejectedExecutionException e){rendering.set(false);}
    }

    private void command(String action,Object value){

        try{JSONObject o=new JSONObject();o.put("type","command");o.put("id",UUID.randomUUID().toString());o.put("action",action);if(value!=null)o.put("value",value);

            if(role.equals("camera")){if(camera!=null)camera.command(o);}

            else if(peer==null||!peer.connected()||SystemClock.elapsedRealtime()-lastState>5000||!peer.send(o))toast("Reconnect before controlling the camera.");

        }catch(Exception e){toast(e.getMessage());}

    }

    private void recordPressed(){

        if(counting){counting=false;countdownToken++;refresh();return;}

        if(state.optBoolean("recording")){if(getPreferences(0).getBoolean("confirm-stop",false))new AlertDialog.Builder(this).setTitle("Stop this take?").setMessage("The recording will be saved. Keep recording if this moment is still important.").setPositiveButton("Stop & save",(d,w)->command("stop",null)).setNegativeButton("Keep recording",null).show();else command("stop",null);return;}

        if(micCheck!=null){toast("Finish the microphone check first");return;}if(countdown==0){command("start",null);return;}

        counting=true;int token=++countdownToken;countDown(countdown,token);

    }

    private void countDown(int remaining,int token){if(!counting||token!=countdownToken||!active)return;if(remaining==0){counting=false;record.countdown(0);command("start",null);refresh();return;}record.countdown(remaining);if(getSharedPreferences("creator-camera",0).getBoolean("beep",true)){try{if(countdownTone==null)countdownTone=new android.media.ToneGenerator(android.media.AudioManager.STREAM_NOTIFICATION,65);countdownTone.startTone(android.media.ToneGenerator.TONE_PROP_BEEP,120);}catch(Exception ignored){}}main.postDelayed(()->countDown(remaining-1,token),1000);}

    private void sendScript(){if(sync!=null&&sync.isChecked()&&role.equals("monitor")&&peer!=null&&peer.connected())peer.send(prompter.snapshot());}

    private ScrollView scrollPanel(View child){ScrollView scroll=new ScrollView(this);scroll.addView(child);return scroll;}
    private void countdownSettings(){LinearLayout panel=column();String[] names={"Off","3 seconds","5 seconds","10 seconds"};for(int i=0;i<names.length;i++){final int seconds=new int[]{0,3,5,10}[i];panel.addView(button(names[i]+(seconds==countdown?" ✓":""),()->{countdown=seconds;getSharedPreferences("creator-camera",0).edit().putInt("countdown",seconds).apply();toast("Countdown set to "+seconds+" seconds");}));}Switch beep=new Switch(this);beep.setText("Audible countdown");beep.setTextColor(TEXT);beep.setChecked(getSharedPreferences("creator-camera",0).getBoolean("beep",true));beep.setOnCheckedChangeListener((b,on)->getSharedPreferences("creator-camera",0).edit().putBoolean("beep",on).apply());panel.addView(beep);new AlertDialog.Builder(this).setTitle("Solo countdown").setView(panel).setPositiveButton("Done",null).show();}
    private void restoreGuides(){android.content.SharedPreferences p=getSharedPreferences("creator-camera",0);zebraEnabled=p.getBoolean("zebra",false);peakingEnabled=p.getBoolean("peaking",false);zebraThreshold=p.getInt("zebraThreshold",235);gridVisible=p.getBoolean("grid",gridVisible);guides.grid(gridVisible);if(gridToggle!=null)gridToggle.setImageDrawable(new CameraIcon("grid",gridVisible?ACCENT:TEXT));guides.safeMargins(p.getFloat("safeRight",.18f),p.getFloat("safeBottom",.22f));guides.social(p.getFloat("ratio",0),p.getBoolean("safe",true));}
    private void previewTools(){LinearLayout panel=column();android.content.SharedPreferences p=getSharedPreferences("creator-camera",0);Switch grid=new Switch(this);grid.setText("Rule-of-thirds grid");grid.setChecked(gridVisible);grid.setTextColor(TEXT);grid.setOnCheckedChangeListener((v,on)->{gridVisible=on;guides.grid(on);p.edit().putBoolean("grid",on).apply();});panel.addView(grid);Spinner aspect=new Spinner(this);String[] labels={"Original frame","Vertical · 9:16","Square · 1:1","Landscape · 16:9"};float[] ratios={0,9f/16,1,16f/9};aspect.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,labels));int chosen=0;for(int i=0;i<ratios.length;i++)if(Math.abs(p.getFloat("ratio",0)-ratios[i])<.001)chosen=i;aspect.setSelection(chosen);panel.addView(aspect);aspect.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> a){}public void onItemSelected(android.widget.AdapterView<?> a,View v,int i,long id){p.edit().putFloat("ratio",ratios[i]).apply();guides.social(ratios[i],p.getBoolean("safe",true));}});Switch safe=new Switch(this);safe.setText("Social safe area");safe.setTextColor(TEXT);safe.setChecked(p.getBoolean("safe",true));safe.setOnCheckedChangeListener((v,on)->{p.edit().putBoolean("safe",on).apply();guides.social(p.getFloat("ratio",0),on);});panel.addView(safe);for(String side:new String[]{"Right","Bottom"}){String key=side.equals("Right")?"safeRight":"safeBottom";panel.addView(label(side+" safe clearance",12));SeekBar margin=new SeekBar(this);margin.setMax(35);margin.setProgress(Math.round(p.getFloat(key,side.equals("Right")?.18f:.22f)*100)-5);margin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}public void onProgressChanged(SeekBar b,int value,boolean user){if(user){p.edit().putFloat(key,(value+5)/100f).apply();guides.safeMargins(p.getFloat("safeRight",.18f),p.getFloat("safeBottom",.22f));}}});panel.addView(margin);}Switch zebra=new Switch(this);zebra.setText("Zebra · bright areas");zebra.setTextColor(TEXT);zebra.setChecked(zebraEnabled);zebra.setOnCheckedChangeListener((v,on)->{zebraEnabled=on;p.edit().putBoolean("zebra",on).apply();if(!on)guides.signal(null);});panel.addView(zebra);SeekBar threshold=new SeekBar(this);threshold.setContentDescription("Zebra brightness threshold");threshold.setMax(55);threshold.setProgress(zebraThreshold-200);threshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}public void onProgressChanged(SeekBar b,int value,boolean user){if(user){zebraThreshold=value+200;p.edit().putInt("zebraThreshold",zebraThreshold).apply();}}});panel.addView(threshold);Switch peaking=new Switch(this);peaking.setText("Focus peaking · green edges");peaking.setTextColor(TEXT);peaking.setChecked(peakingEnabled);peaking.setOnCheckedChangeListener((v,on)->{peakingEnabled=on;p.edit().putBoolean("peaking",on).apply();if(!on)guides.signal(null);});panel.addView(peaking);panel.addView(label("Guides are monitoring aids and are not burned into recordings. Safe areas are approximate and can vary by platform. Exposure/focus analysis uses the small preview, not sensor RAW data, and pauses when the phone is hot.",12));new AlertDialog.Builder(this).setTitle("Preview guides").setView(scrollPanel(panel)).setPositiveButton("Done",null).show();}
    private void preflight(){LinearLayout panel=column();panel.addView(label((state.optBoolean("ready")?"✓ Camera ready":"! Camera not ready")+"\n"+(state.optBoolean("audioInputAvailable",true)?"✓ Selected microphone available":"! Selected microphone disconnected")+"\nStorage: "+String.format(Locale.US,"%.1f GB",state.optLong("freeMB")/1000.)+(state.optLong("freeMB")<500?" · low":"")+"\nBattery: "+state.optInt("battery")+"%"+(state.optInt("battery")<20?" · charge before a long take":"")+"\nTemperature: "+(state.optInt("thermal")>=3?"hot · let the phone cool":"normal")+"\n"+state.optString("format"),14));panel.addView(button("Test microphone · 5 seconds",this::testMicrophone));panel.addView(label("Microphone levels need a short sound check. While recording, the MIC display shows dBFS; near 0 dBFS risks clipping. Tap the recording timer to mark a good moment; hold it to mark a mistake.",12));new AlertDialog.Builder(this).setTitle("Ready to shoot?").setView(panel).setPositiveButton("Done",null).show();}
    private void testMicrophone(){if(!role.equals("camera")){toast("Run the microphone check on the camera phone");return;}if(state.optBoolean("recording")||micCheck!=null){toast("Stop recording before checking the microphone");return;}TextView meter=label("Speak at your normal recording volume",17);AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Microphone check").setView(meter).setPositiveButton("Close",null).create();CreatorRecorder probe=new CreatorRecorder();micCheck=probe;dialog.setOnDismissListener(d->{probe.close();if(micCheck==probe)micCheck=null;});dialog.show();try{probe.start(this,state.optInt("audioInputId"));}catch(Exception e){meter.setText(e.getMessage());probe.close();micCheck=null;return;}main.post(new Runnable(){int peak;public void run(){if(micCheck!=probe||!dialog.isShowing())return;try{peak=Math.max(peak,probe.amplitude());double db=peak==0?-96:20*Math.log10(peak/32767.);meter.setText(probe.route()+"\nPeak: "+Math.round(db)+" dBFS\n"+(peak>=31000?"Too loud · reduce receiver gain":peak<200?"Very quiet · check the mic":"Signal detected")+"\n"+(probe.elapsed()<5000?"Checking…":"Check complete"));if(probe.elapsed()>=5000){probe.close();micCheck=null;}else main.postDelayed(this,100);}catch(Exception e){probe.close();micCheck=null;meter.setText("Microphone unavailable: "+e.getMessage());}}});}
    private void shootingPresets(){android.content.SharedPreferences prefs=getSharedPreferences("creator-shooting-presets",0);ArrayList<String> names=new ArrayList<>(prefs.getAll().keySet());Collections.sort(names);LinearLayout panel=column();panel.addView(button("＋ Save current shooting setup",()->{EditText field=new EditText(this);field.setHint("Talking head, product demo…");new AlertDialog.Builder(this).setTitle("Save shooting preset").setView(field).setPositiveButton("Save",(d,w)->{String name=field.getText().toString().trim();if(name.isEmpty()||!state.optBoolean("ready")){toast("Enter a name and wait for the camera");return;}try{JSONObject value=new JSONObject(state.toString());value.put("localUi",new JSONObject().put("countdown",countdown).put("grid",gridVisible).put("scriptVisible",scriptVisible).put("overlay",overlay).put("linked",linked.isChecked()).put("prompter",prompter.snapshot()).put("ratio",getSharedPreferences("creator-camera",0).getFloat("ratio",0)).put("safe",getSharedPreferences("creator-camera",0).getBoolean("safe",true)).put("safeRight",getSharedPreferences("creator-camera",0).getFloat("safeRight",.18f)).put("safeBottom",getSharedPreferences("creator-camera",0).getFloat("safeBottom",.22f)).put("zebra",zebraEnabled).put("peaking",peakingEnabled).put("zebraThreshold",zebraThreshold).put("beep",getSharedPreferences("creator-camera",0).getBoolean("beep",true)));prefs.edit().putString(name,value.toString()).apply();toast("Shooting preset saved");}catch(Exception e){toast(e.getMessage());}}).setNegativeButton("Cancel",null).show();}));for(String name:names){Button b=button(name,()->{try{JSONObject value=new JSONObject(prefs.getString(name,"{}"));if(state.optBoolean("recording")||!state.optString("cameraId").equals(value.optString("cameraId"))){toast("Stop recording and select the preset’s original lens first");return;}command("shootingPreset",value);JSONObject ui=value.optJSONObject("localUi");if(ui!=null){countdown=ui.optInt("countdown");gridVisible=ui.optBoolean("grid");scriptVisible=ui.optBoolean("scriptVisible");overlay=ui.optBoolean("overlay");linked.setChecked(ui.optBoolean("linked"));getSharedPreferences("creator-camera",0).edit().putInt("countdown",countdown).putBoolean("grid",gridVisible).putFloat("ratio",(float)ui.optDouble("ratio")).putBoolean("safe",ui.optBoolean("safe",true)).putFloat("safeRight",(float)ui.optDouble("safeRight",.18)).putFloat("safeBottom",(float)ui.optDouble("safeBottom",.22)).putBoolean("zebra",ui.optBoolean("zebra")).putBoolean("peaking",ui.optBoolean("peaking")).putInt("zebraThreshold",ui.optInt("zebraThreshold",235)).putBoolean("beep",ui.optBoolean("beep",true)).apply();if(ui.optJSONObject("prompter")!=null)prompter.apply(ui.getJSONObject("prompter"));restoreGuides();arrange();}}catch(Exception e){toast("Preset unavailable: "+e.getMessage());}});b.setOnLongClickListener(v->{new AlertDialog.Builder(this).setTitle("Delete "+name+"?").setPositiveButton("Delete",(d,w)->{prefs.edit().remove(name).apply();panel.removeView(b);}).setNegativeButton("Cancel",null).show();return true;});panel.addView(b);}panel.addView(label("Presets restore settings for this lens and microphone. Hold a saved preset to delete it.",12));ScrollView scroll=new ScrollView(this);scroll.addView(panel);new AlertDialog.Builder(this).setTitle("Shooting presets").setView(scroll).setPositiveButton("Done",null).show();}
    private void microphoneSetup(){
        if(state.optBoolean("recording")){toast("Recording input: "+state.optString("audioRoute","Unknown")+". Stop before changing microphone.");return;}
        LinearLayout panel=column();panel.setPadding(dp(12),0,dp(12),0);panel.addView(label("Select the microphone on the camera phone. USB receivers can add their own delay; check sync with a visible clap before a take. Last recording route: "+state.optString("audioRoute","Not recorded"),13));
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Microphone input").setPositiveButton("Done",null).create();
        Switch voice=new Switch(this);voice.setText("Voice processing");voice.setTextColor(TEXT);voice.setChecked(state.optBoolean("voiceProcessing"));voice.setOnCheckedChangeListener((v,on)->command("voiceProcessing",on));panel.addView(voice);
        panel.addView(label("Requests Android voice processing. Noise reduction and automatic level control depend on the phone and microphone; this is not AI cleanup. For loud input, lower gain on your USB receiver. Compare a short test with this off and on.",12));
        panel.addView(button("System default",()->{command("audioInput",0);dialog.dismiss();}));
        JSONArray inputs=state.optJSONArray("audioInputs");if(inputs!=null)for(int i=0;i<inputs.length();i++){JSONObject input=inputs.optJSONObject(i);if(input==null)continue;int id=input.optInt("id");panel.addView(button(input.optString("name")+(id==state.optInt("audioInputId")?" ✓":""),()->{command("audioInput",id);dialog.dismiss();}));}
        ScrollView scroll=new ScrollView(this);scroll.addView(panel);FrameLayout viewport=new FrameLayout(this);viewport.addView(scroll,new FrameLayout.LayoutParams(-1,(int)(getResources().getDisplayMetrics().heightPixels*.55)));dialog.setView(viewport);dialog.show();
    }
    private void videoSetup(){
        if(state.optBoolean("recording")||state.optBoolean("busy")){toast("Stop recording before changing video quality.");return;}
        JSONArray options=state.optJSONArray("videoOptions");if(options==null||options.length()==0){toast("Connect a ready camera to choose video quality.");return;}
        LinearLayout panel=column();panel.setPadding(dp(12),0,dp(12),0);
        panel.addView(label("Choose resolution / frame rate. Higher quality uses more storage. "+state.optString("format"),13));
        Switch quality=new Switch(this);quality.setText("High bitrate / better detail");quality.setChecked(state.optBoolean("highQuality",true));quality.setTextColor(TEXT);panel.addView(quality);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Video quality").setPositiveButton("Done",null).create();
        panel.addView(button("Maximum available",()->{JSONObject option=options.optJSONObject(options.length()-1);if(option!=null){setVideo(option.optString("id"),true);dialog.dismiss();}}));
        panel.addView(button("Frame rate slider",()->{dialog.dismiss();cameraControls.select("fps");}));
        for(int i=options.length()-1;i>=0;i--){JSONObject option=options.optJSONObject(i);if(option==null)continue;String id=option.optString("id");String name=VideoQuality.label(option.optInt("width"),option.optInt("height"))+" / "+option.optInt("fps")+" FPS";
            Button choice=button(name+ (id.equals(state.optString("videoOption"))?"  ✓":""),()->{setVideo(id,quality.isChecked());dialog.dismiss();});panel.addView(choice);
        }
        quality.setOnCheckedChangeListener((v,on)->setVideo(state.optString("videoOption"),on));
        ScrollView scroll=new ScrollView(this);scroll.addView(panel);FrameLayout viewport=new FrameLayout(this);viewport.addView(scroll,new FrameLayout.LayoutParams(-1,(int)(getResources().getDisplayMetrics().heightPixels*.60)));dialog.setView(viewport);dialog.show();
    }
    private void setVideo(String id,boolean high){try{JSONObject value=new JSONObject();value.put("id",id);value.put("highQuality",high);command("video",value);}catch(JSONException ignored){}}
    private void shotLocks(boolean locked){if(!state.optBoolean("manual")&&state.optBoolean("aeLockSupported"))command("aeLock",locked);if(!state.optBoolean("wbManual")&&state.optBoolean("awbLockSupported"))command("awbLock",locked);}
    private void eventTools(){LinearLayout panel=column();panel.setPadding(dp(14),0,dp(14),0);panel.addView(label("Production slate",17));panel.addView(label("Name takes by event or film, scene and take number. The next take advances automatically on the camera phone. Set names before recording.",12));EditText production=new EditText(this);production.setHint("Production / event, e.g. JJ Wedding");production.setSingleLine();production.setText(state.optString("production"));panel.addView(production);EditText scene=new EditText(this);scene.setHint("Scene / moment, e.g. Speeches");scene.setSingleLine();scene.setText(state.optString("scene"));panel.addView(scene);NumberPicker take=new NumberPicker(this);take.setMinValue(1);take.setMaxValue(99999);take.setValue(state.optInt("nextTake",1));take.setWrapSelectorWheel(false);panel.addView(take,new LinearLayout.LayoutParams(-1,dp(110)));Button save=button("Apply slate",()->{try{JSONObject value=new JSONObject();value.put("production",production.getText().toString());value.put("scene",scene.getText().toString());value.put("take",take.getValue());command("slate",value);toast("Slate sent to camera");}catch(Exception ignored){}});save.setEnabled(!state.optBoolean("recording"));panel.addView(save);Switch protect=new Switch(this);protect.setText("Confirm before stopping a take");protect.setTextColor(TEXT);protect.setChecked(getPreferences(0).getBoolean("confirm-stop",false));protect.setOnCheckedChangeListener((b,on)->getPreferences(0).edit().putBoolean("confirm-stop",on).apply());panel.addView(protect);panel.addView(label("Venue lighting · anti-flicker",17));panel.addView(label("In auto exposure, choose the venue’s mains frequency for fluorescent/LED lighting. Manual exposure needs a matching shutter speed; some LEDs still require adjustment.",12));JSONArray modes=state.optJSONArray("antiBandingModes");String[] names={"Off","50 Hz","60 Hz","Auto"};if(modes!=null)for(int i=0;i<modes.length();i++){int mode=modes.optInt(i);if(mode>=0&&mode<names.length)panel.addView(button(names[mode]+(state.optInt("antiBanding")==mode?" ✓":""),()->command("antiBanding",mode)));}panel.addView(button("Lock exposure + white balance",()->shotLocks(true)));panel.addView(button("Release exposure + white balance",()->shotLocks(false)));panel.addView(label("For interviews, frame and light the subject before locking exposure and white balance. For moving coverage, leave auto controls active. Film frame rates remain in the FPS control; only supported rates are available.",12));ScrollView scroll=new ScrollView(this);scroll.addView(panel);new AlertDialog.Builder(this).setTitle("Event / film tools").setView(scroll).setPositiveButton("Done",null).show();}
    private void recordingSetup(){LinearLayout panel=column();panel.setPadding(dp(12),0,dp(12),0);detach(linked);detach(sync);panel.addView(button("Preview: "+(state.optBoolean("livePreview",true)?"Live priority":"Detail priority")+" · "+state.optString("previewSize"),()->new AlertDialog.Builder(this).setTitle("Live preview quality").setMessage("Changes the monitor feed only. Recorded video keeps its selected resolution and quality. Stop recording before changing this setting.").setPositiveButton("Live priority",(d,w)->command("previewProfile",true)).setNeutralButton("Detail priority",(d,w)->command("previewProfile",false)).setNegativeButton("Cancel",null).show()));panel.addView(button("Pre-recording check",this::preflight));panel.addView(button("Shooting presets",this::shootingPresets));panel.addView(button("Framing / focus / exposure guides",this::previewTools));panel.addView(button("Countdown & sound",this::countdownSettings));panel.addView(button("Mark good take",()->command("marker","good")));panel.addView(button("Mark mistake / retake",()->command("marker","mistake")));panel.addView(button("Event / film tools",this::eventTools));panel.addView(button("Video resolution / quality",()->videoSetup()));panel.addView(button("Microphone input / USB receiver",()->microphoneSetup()));panel.addView(linked);panel.addView(sync);CheckBox layout=new CheckBox(this);layout.setText("Script over preview");layout.setChecked(overlay);layout.setOnCheckedChangeListener((b,on)->{overlay=on;arrange();});panel.addView(layout);panel.addView(button("Countdown: "+countdown+" seconds",()->new AlertDialog.Builder(this).setTitle("Recording countdown").setItems(new String[]{"None","3 seconds","5 seconds","10 seconds"},(d,w)->{countdown=new int[]{0,3,5,10}[w];getSharedPreferences("creator-camera",0).edit().putInt("countdown",countdown).apply();toast("Countdown: "+countdown+" seconds");}).show()));if(role.equals("camera"))panel.addView(button("Recover interrupted takes",()->command("recover",null)));ScrollView scroll=new ScrollView(this);scroll.addView(panel);new AlertDialog.Builder(this).setTitle("Shooting setup").setView(scroll).setPositiveButton("Done",null).show();}

    private void pair(){

        if(role.isEmpty())return;

        if(role.equals("camera")){

            if(peer==null){key=SecureChannel.newSecret();connection="Listening for approved monitor";peer=newPeer();peer.host(key);}

            LinearLayout content=column();content.setPadding(dp(12),dp(8),dp(12),dp(8));

            content.addView(label("HOTSPOT PAIRING\n1. Enable this camera phone hotspot.\n2. Connect the monitor to this hotspot using its Wi-Fi settings.\n3. Return here, scan this code on the monitor, then approve control.\n\nNo router or internet needed. Keep this code private.",14));

            content.addView(button("Set up camera hotspot",()->networkSettings(true)),0);

            ImageView qr=new ImageView(this);qr.setAdjustViewBounds(true);qr.setScaleType(ImageView.ScaleType.FIT_CENTER);content.addView(qr,0,new LinearLayout.LayoutParams(-1,dp(240)));

            String[] hosts=addresses().trim().split("\\n");

            LinearLayout choices=new LinearLayout(this);HorizontalScrollView scroll=new HorizontalScrollView(this);scroll.addView(choices);content.addView(scroll);

            boolean generated=false;

            for(String host:hosts){try{String payload=PairingCode.encode(host,key);Runnable show=()->showPairingQr(qr,payload);choices.addView(button(host,show));if(!generated){show.run();generated=true;}}catch(IllegalArgumentException ignored){}}

            if(!generated)content.addView(label("Connect Wi-Fi or enable a hotspot, then reopen pairing.",14));

            ScrollView outer=new ScrollView(this);outer.addView(content);FrameLayout viewport=new FrameLayout(this);viewport.addView(outer,new FrameLayout.LayoutParams(-1,(int)(getResources().getDisplayMetrics().heightPixels*.53)));

            pairingDialog=new AlertDialog.Builder(this).setTitle("Pair monitor").setView(viewport).setPositiveButton("Done",null).setNeutralButton("Manual details",(d,w)->new AlertDialog.Builder(this).setTitle("Manual pairing").setMessage(addresses()+"\nPort: "+PeerLink.PORT+"\nKey: "+key).setPositiveButton("Done",null).show()).setNegativeButton("Revoke / new code",(d,w)->{if(peer!=null)peer.close();peer=null;pair();}).show();

            pairingDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        }else{

            LinearLayout form=column();form.addView(label("Connect this monitor to the camera phone hotspot first. Choose Stay connected if Android says there is no internet. Then scan the camera code.",14));form.addView(button("Connect to camera hotspot",()->networkSettings(false)));form.addView(button("Scan QR code",()->{if(pairingDialog!=null)pairingDialog.dismiss();startActivityForResult(new android.content.Intent(this,QrScanActivity.class),20);}));form.setPadding(dp(16),0,dp(16),0);EditText host=new EditText(this);host.setHint("Camera IP address, e.g. 192.168.1.20");host.setSingleLine();host.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_URI);host.setText(getPreferences(0).getString("host",""));form.addView(host);

            EditText secret=new EditText(this);secret.setHint("32-character pairing key");secret.setSingleLine();secret.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);form.addView(secret);

            form.addView(label("Use the address and key shown on the camera phone. Approve the connection on that phone.",13));

            AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Connect to camera").setView(form).setPositiveButton("Connect",null).setNegativeButton("Cancel",null).create();

            dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String h=host.getText().toString().trim(),k=secret.getText().toString().trim();if(h.isEmpty()){host.setError("Camera address required");return;}if(!k.matches("[a-fA-F0-9]{32}")){secret.setError("Use the full 32-character key");return;}if(peer!=null)peer.close();peer=newPeer();lastState=0;connection="Connecting; approve on camera";getPreferences(0).edit().putString("host",h).apply();peer.join(h,k);dialog.dismiss();refresh();}));pairingDialog=dialog;dialog.show();

        }

    }

    private void showPairingQr(ImageView view,String payload){

        try{com.google.zxing.common.BitMatrix matrix=new com.google.zxing.qrcode.QRCodeWriter().encode(payload,com.google.zxing.BarcodeFormat.QR_CODE,640,640);

            Bitmap bitmap=Bitmap.createBitmap(640,640,Bitmap.Config.ARGB_8888);int[] pixels=new int[640*640];for(int y=0;y<640;y++)for(int x=0;x<640;x++)pixels[y*640+x]=matrix.get(x,y)?Color.BLACK:Color.WHITE;bitmap.setPixels(pixels,0,640,0,0,640,640);view.setImageBitmap(bitmap);

        }catch(com.google.zxing.WriterException e){toast("Unable to generate pairing code. Use manual details.");}

    }

    @Override protected void onActivityResult(int request,int result,android.content.Intent data){

        super.onActivityResult(request,result,data);
        if(request==40){if(result==RESULT_OK&&data!=null){try{String[] target=DesktopPairingCode.decode(data.getStringExtra("pairing"));new AlertDialog.Builder(this).setTitle("Connect to Windows webcam?").setMessage("Share this phone camera with the computer at "+target[0]+"? Your camera feed stays on your local network.").setPositiveButton("Connect",(d,w)->{desktopPairing=target;chooseRole("camera");}).setNegativeButton("Cancel",null).show();}catch(IllegalArgumentException e){toast("Invalid Windows pairing code");}}return;}
        if(request==30){if(result==RESULT_OK)chooseRole("camera");return;}

        if(request==20&&role.equals("monitor")){

            if(result==RESULT_OK&&data!=null){try{String[] code=PairingCode.decode(data.getStringExtra("pairing"));if(peer!=null)peer.close();peer=newPeer();lastState=0;connection="Connecting; approve on camera";peer.join(code[0],code[1]);refresh();}catch(IllegalArgumentException invalid){toast("Invalid Solo Camera code");pair();}}

            else pair();

        }

    }

    private PeerLink newPeer(){int current=epoch;return new PeerLink(new PeerLink.Listener(){

        public void approval(String address,java.util.function.Consumer<Boolean> answer){main.post(()->{if(current!=epoch||!active){answer.accept(false);return;}if(pairingDialog!=null)pairingDialog.dismiss();new AlertDialog.Builder(MainActivity.this).setTitle("Allow this monitor?").setMessage(address+" has the pairing key and requests camera control and preview.").setPositiveButton("Allow",(d,w)->answer.accept(true)).setNegativeButton("Deny",(d,w)->answer.accept(false)).setOnCancelListener(d->answer.accept(false)).show();});}

        public void connected(){main.post(()->{if(current!=epoch)return;connection="Encrypted monitor connection";if(role.equals("camera"))command("state",null);else{commandStateRequest();sendScript();}refresh();});}

        public void disconnected(String reason){main.post(()->{if(current!=epoch)return;connection=reason;lastState=0;if(role.equals("monitor"))counting=false;refresh();});}

        public void frame(byte[] bytes){if(current==epoch&&role.equals("monitor"))displayFrame(bytes,state.optInt("rotation"),current);}

        public void message(JSONObject message){main.post(()->{

            if(current!=epoch)return;String type=message.optString("type");

            if(role.equals("monitor")&&type.equals("state"))receiveState(message);

            else if(role.equals("camera")&&type.equals("command")&&camera!=null)camera.command(message);

            else if(role.equals("camera")&&type.equals("prompter")&&sync.isChecked()){try{prompter.apply(message);}catch(Exception e){toast("Script rejected: "+e.getMessage());}}

        });}

    },host->LocalNetwork.socket(MainActivity.this,host));}

    private void commandStateRequest(){try{JSONObject o=new JSONObject();o.put("type","command");o.put("action","state");if(peer!=null)peer.send(o);}catch(Exception ignored){}}

    private String addresses(){return LocalNetwork.addresses();}

    private void networkSettings(boolean hotspot){

        if(state.optBoolean("recording")||state.optBoolean("busy")){toast("Finish the take before changing the network.");return;}

        if(pairingDialog!=null)pairingDialog.dismiss();

        returningToPair=true;

        android.content.Intent intent=new android.content.Intent(hotspot?"android.settings.TETHER_SETTINGS":android.provider.Settings.ACTION_WIFI_SETTINGS);

        try{startActivity(intent);}catch(android.content.ActivityNotFoundException e){try{startActivity(new android.content.Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));}catch(android.content.ActivityNotFoundException unavailable){returningToPair=false;toast("Open the phone Settings to enable hotspot or connect Wi-Fi.");}}

    }

    private void toast(String message){Toast.makeText(this,message,Toast.LENGTH_LONG).show();}

    private void exitRole(){if(state.optBoolean("recording")||state.optBoolean("busy")){new AlertDialog.Builder(this).setMessage("Leave camera mode? An active take will stop and save.").setPositiveButton("Leave",(d,w)->{cleanup();role="";home();}).setNegativeButton("Stay",null).show();}else{cleanup();role="";home();}}

    private void cleanup(){if(micCheck!=null){micCheck.close();micCheck=null;}if(countdownTone!=null){countdownTone.release();countdownTone=null;}if(guides!=null)guides.signal(null);epoch++;previewFps=0;previewCount=0;previewSampleStart=0;counting=false;countdownToken++;if(peer!=null){peer.close();peer=null;}if(camera!=null){Object releasedOwner=cameraOwnership;cameraOwnership=null;camera.close(()->main.postDelayed(()->CameraOwnership.release(releasedOwner),250));camera=null;}else if(cameraOwnership!=null){CameraOwnership.release(cameraOwnership);cameraOwnership=null;}if(prompter!=null)prompter.setPlaying(false);connection="Not paired";}

    private final Runnable heartbeat=new Runnable(){public void run(){if(active){if(camera!=null)camera.orientation(getWindowManager().getDefaultDisplay().getRotation());refresh();main.postDelayed(this,1000);}}};

    @Override protected void onResume(){super.onResume();active=true;if(role.equals("camera")&&checkSelfPermission(Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)startCamera();main.post(heartbeat);if(returningToPair){returningToPair=false;main.post(()->{if(active&&!role.isEmpty())pair();});}}

    @Override protected void onStop(){active=false;main.removeCallbacks(heartbeat);cleanup();super.onStop();}

    @Override protected void onDestroy(){cleanup();previewDecoder.shutdownNow();super.onDestroy();}

    @Override public void onConfigurationChanged(android.content.res.Configuration configuration){super.onConfigurationChanged(configuration);if(work!=null&&!role.isEmpty())layoutWorkspace();}

    @Override public void onBackPressed(){if(cameraControls!=null&&!role.isEmpty()&&cameraControls.closeDock())return;if(!role.isEmpty())exitRole();else super.onBackPressed();}

}






