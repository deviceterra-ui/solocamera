package studio.solocamera;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;

/** Persistent camera HUD and a live adjustment dock. No dialogs or numeric entry. */
public final class CameraControlsView extends LinearLayout {
    public interface Commands { void send(String action,Object value); }
    private static final int BG=0xff111419,PANEL=0xff1b2027,TEXT=0xffedf0f3,MUTED=0xff929ca8,YELLOW=0xffffc857;
    private final Commands commands;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final GridLayout strip;
    private final LinearLayout dock,modes,presets;
    private final TextView title,readout,left,right,hint;
    private final SeekBar slider;
    private final Map<String,TextView> values=new LinkedHashMap<>(),labels=new LinkedHashMap<>();
    private final Map<String,LinearLayout> tiles=new LinkedHashMap<>();
    private final Map<String,Object> pending=new HashMap<>();
    private final Map<String,Long> pendingUntil=new HashMap<>();
    private JSONObject state=new JSONObject();
    private boolean usable,dragging,updating,logarithmic;
    private String selected="",capabilitySignature="";
    private double minimum,maximum;
    private String queuedAction;private Object queuedValue;private boolean scheduled;
    private final Runnable dispatch=this::dispatchQueued;
    private void dispatchQueued(){scheduled=false;if(queuedAction!=null){String action=queuedAction;Object value=queuedValue;queuedAction=null;commands.send(action,value);}}

    public CameraControlsView(Context context,Commands commands){
        super(context);this.commands=commands;setOrientation(VERTICAL);setBackgroundColor(BG);
        dock=new LinearLayout(context);dock.setOrientation(VERTICAL);dock.setPadding(dp(12),dp(3),dp(12),dp(3));dock.setBackgroundColor(PANEL);dock.setVisibility(GONE);
        LinearLayout header=new LinearLayout(context);header.setGravity(Gravity.CENTER_VERTICAL);
        title=text("",11,MUTED);title.setLetterSpacing(.08f);header.addView(title,new LinearLayout.LayoutParams(0,dp(32),1));
        readout=text("",20,YELLOW);readout.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));readout.setGravity(Gravity.CENTER);header.addView(readout,new LinearLayout.LayoutParams(-2,dp(32)));
        ImageButton close=icon("close","Close adjustment",()->select(""));header.addView(close,new LinearLayout.LayoutParams(dp(44),dp(36)));dock.addView(header);
        LinearLayout range=new LinearLayout(context);range.setGravity(Gravity.CENTER_VERTICAL);
        left=text("",10,MUTED);left.setGravity(Gravity.CENTER);right=text("",10,MUTED);right.setGravity(Gravity.CENTER);
        slider=new SeekBar(context);slider.setMax(1000);slider.setProgressTintList(ColorStateList.valueOf(YELLOW));slider.setThumbTintList(ColorStateList.valueOf(YELLOW));slider.setProgressBackgroundTintList(ColorStateList.valueOf(0xff424a55));
        range.addView(left,new LinearLayout.LayoutParams(dp(46),dp(44)));range.addView(slider,new LinearLayout.LayoutParams(0,dp(44),1));range.addView(right,new LinearLayout.LayoutParams(dp(46),dp(44)));dock.addView(range);
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onStartTrackingTouch(SeekBar bar){dragging=true;}
            public void onProgressChanged(SeekBar bar,int progress,boolean user){
                if(!user||updating||!usable)return;
                Object value=sliderValue(progress);String action=selected.equals("wb")?"wbFine":selected.equals("look")?"lookStrength":selected;previewValue(action,value);if(!selected.equals("fps"))submit(action,value,false);
            }
            public void onStopTrackingTouch(SeekBar bar){dragging=false;if(usable){Object value=sliderValue(bar.getProgress());String action=selected.equals("wb")?"wbFine":selected.equals("look")?"lookStrength":selected;previewValue(action,value);submit(action,value,true);}}
        });
        modes=new LinearLayout(context);modes.setGravity(Gravity.CENTER_VERTICAL);
        presets=new LinearLayout(context);presets.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout choices=new LinearLayout(context);choices.setGravity(Gravity.CENTER_VERTICAL);choices.addView(modes,new LinearLayout.LayoutParams(-2,dp(36)));
        HorizontalScrollView quick=new HorizontalScrollView(context);quick.setHorizontalScrollBarEnabled(false);quick.addView(presets);choices.addView(quick,new LinearLayout.LayoutParams(0,dp(36),1));dock.addView(choices);
        hint=text("",11,MUTED);hint.setGravity(Gravity.CENTER_VERTICAL);hint.setMaxLines(1);dock.addView(hint,new LinearLayout.LayoutParams(-1,dp(24)));addView(dock);
        strip=new GridLayout(context);strip.setColumnCount(4);strip.setRowCount(2);
        String[] keys={"iso","shutter","focus","wb","zoom","ev","aperture","lens","fps","look"};
        String[] names={"ISO","SHUTTER","FOCUS","WB","ZOOM","EV","IRIS","LENS","FPS","LOOK"};
        for(int i=0;i<keys.length;i++){
            String key=keys[i];LinearLayout tile=new LinearLayout(context);tile.setOrientation(VERTICAL);tile.setGravity(Gravity.CENTER);tile.setPadding(dp(2),dp(3),dp(2),dp(3));tile.setClickable(true);tile.setFocusable(true);tile.setOnClickListener(v->select(selected.equals(key)?"":key));tile.setTooltipText(names[i]);
            LinearLayout caption=new LinearLayout(context);caption.setGravity(Gravity.CENTER);
            ImageView image=new ImageView(context);image.setImageDrawable(new CameraIcon(key,MUTED));caption.addView(image,new LinearLayout.LayoutParams(dp(14),dp(14)));
            TextView label=text(names[i],9,MUTED);label.setPadding(dp(4),0,0,0);caption.addView(label);tile.addView(caption);
            TextView value=text("—",16,TEXT);value.setGravity(Gravity.CENTER);value.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));tile.addView(value,new LinearLayout.LayoutParams(-1,dp(30)));
            TextView mode=text("",8,MUTED);mode.setGravity(Gravity.CENTER);tile.addView(mode,new LinearLayout.LayoutParams(-1,dp(12)));
            tiles.put(key,tile);values.put(key,value);labels.put(key,mode);
        }
        addView(strip,new LinearLayout.LayoutParams(-1,-2));layoutTiles(getResources().getDisplayMetrics().widthPixels);
    }
    private void layoutTiles(int width){int columns=width>=dp(600)?10:5;strip.removeAllViews();strip.setColumnCount(columns);strip.setRowCount(10/columns);int index=0;for(LinearLayout tile:tiles.values()){GridLayout.LayoutParams p=new GridLayout.LayoutParams(GridLayout.spec(index/columns),GridLayout.spec(index%columns));p.width=width/columns;p.height=dp(62);strip.addView(tile,p);index++;}}
    @Override protected void onSizeChanged(int w,int h,int oldw,int oldh){super.onSizeChanged(w,h,oldw,oldh);if(w>0&&w!=oldw)layoutTiles(w);}
    private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density);}
    private TextView text(String s,int size,int color){TextView t=new TextView(getContext());t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setGravity(Gravity.CENTER_VERTICAL);return t;}
    private void setText(TextView view,String value){if(!android.text.TextUtils.equals(view.getText(),value))view.setText(value);}
    private GradientDrawable background(boolean selected){GradientDrawable d=new GradientDrawable();d.setColor(selected?0xff302a1e:BG);d.setCornerRadius(dp(5));if(selected)d.setStroke(dp(1),0xff5d4c25);return d;}
    private ImageButton icon(String name,String description,Runnable action){ImageButton b=new ImageButton(getContext());b.setImageDrawable(new CameraIcon(name,TEXT));b.setBackgroundColor(android.graphics.Color.TRANSPARENT);b.setPadding(dp(10),dp(6),dp(10),dp(6));b.setContentDescription(description);b.setTooltipText(description);b.setOnClickListener(v->action.run());return b;}
    private void chip(LinearLayout parent,String label,String icon,boolean active,boolean enabled,Runnable action){
        TextView chip=text(label,11,active?YELLOW:TEXT);chip.setGravity(Gravity.CENTER);chip.setPadding(dp(9),0,dp(9),0);chip.setBackground(background(active));chip.setClickable(true);chip.setFocusable(true);chip.setEnabled(enabled);chip.setAlpha(enabled?1:.35f);chip.setContentDescription(label);chip.setOnClickListener(v->action.run());
        if(icon!=null){CameraIcon drawable=new CameraIcon(icon,active?YELLOW:TEXT);drawable.setBounds(0,0,dp(16),dp(16));chip.setCompoundDrawables(drawable,null,null,null);chip.setCompoundDrawablePadding(dp(5));}
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,dp(32));p.rightMargin=dp(5);parent.addView(chip,p);
    }
    public void select(String key){
        if(!selected.equals(key)){handler.removeCallbacks(dispatch);dispatch.run();}
        selected=key;dragging=false;rebuildDock();updateTiles();
    }
    public boolean closeDock(){if(selected.isEmpty())return false;select("");return true;}
    public View adjustmentDock(){return dock;}
    public void sideDock(boolean side){if(dock.getParent()!=null)((ViewGroup)dock.getParent()).removeView(dock);if(!side)addView(dock,0,new LinearLayout.LayoutParams(-1,-2));}
    public void update(JSONObject s,boolean usable){
        state=s;this.usable=usable;
        long now=SystemClock.uptimeMillis();
        for(String key:new ArrayList<>(pending.keySet()))if(matches(key,pending.get(key))||pendingUntil.getOrDefault(key,0L)<now){pending.remove(key);pendingUntil.remove(key);}
        String signature=s.optString("cameraId")+s.optString("manualSupported")+s.optString("isoMin")+s.optString("isoMax")+s.optString("wbs")+s.optBoolean("wbFineReady")+s.optBoolean("wbManual")+s.optString("apertures")+s.optString("fpsRates")+s.optBoolean("lookSupported")+s.optBoolean("manual")+s.optBoolean("busy")+s.optBoolean("recording")+usable;
        if(!signature.equals(capabilitySignature)&&!dragging){capabilitySignature=signature;rebuildDock();}
        updateTiles();
        if(!selected.isEmpty()&&!dragging&&!pending.containsKey(selected))syncSlider();
        if(!selected.isEmpty())setText(readout,adjustmentReadout());
        slider.setEnabled(usable&&supported(selected)&&maximum>minimum);
        updateModeAppearance();
    }
    private void updateTiles(){
        for(String key:tiles.keySet()){
            Object value=current(key);setText(values.get(key),state.has("cameraId")?format(key,value):"—");
            String mode="";
            if(key.equals("iso")||key.equals("shutter"))mode=state.optBoolean("manual")||pending.containsKey("iso")||pending.containsKey("shutter")?"MANUAL":"AUTO";
            if(key.equals("focus"))mode=((Number)value).doubleValue()<0?"AUTO":"MANUAL";
            if(key.equals("aperture")&&!supported(key))mode="FIXED";
            if(key.equals("wb")&&(state.optBoolean("wbManual")||pending.containsKey("wbFine")))mode="MANUAL";
            if(pending.containsKey(key)||(key.equals("wb")&&pending.containsKey("wbFine")))mode="APPLYING";
            setText(labels.get(key),mode);labels.get(key).setTextColor(pending.containsKey(key)?YELLOW:MUTED);
            values.get(key).setTextColor(selected.equals(key)?YELLOW:TEXT);
            boolean active=selected.equals(key);if(!Objects.equals(tiles.get(key).getTag(),active)){tiles.get(key).setTag(active);tiles.get(key).setBackground(background(active));}tiles.get(key).setAlpha(state.has("cameraId")?1:.4f);
            tiles.get(key).setContentDescription(key+", "+values.get(key).getText()+", "+mode);
        }
    }
    private Object current(String key){
        if(pending.containsKey(key))return pending.get(key);
        switch(key){
            case "iso":return state.optBoolean("manual")?state.optInt("iso",100):state.optInt("actualIso",100);
            case "shutter":return state.optBoolean("manual")?state.optLong("shutter",20000000):state.optLong("actualShutter",20000000);
            case "focus":return state.optDouble("focus",-1);
            case "zoom":return state.optDouble("zoom",1);
            case "ev":return state.optInt("ev",0);
            case "wb":return state.optInt("wb",1);
            case "wbFine":return state.optDouble("wbFine",0);
            case "aperture":{double a=state.optDouble("actualAperture",0);JSONArray available=state.optJSONArray("apertures");return a>0?a:available!=null&&available.length()>0?available.optDouble(0):0;}
            case "fps":return state.optInt("videoFps",30);
            case "look":return state.optInt("look",0);
            case "lookStrength":return state.optDouble("lookStrength",70);
            case "lens":return state.optString("cameraId","—");
            default:return 0;
        }
    }
    private boolean matches(String key,Object target){
        Object actual;
        switch(key){case "fps":actual=state.optInt("videoFps");break;case "iso":actual=state.optInt("iso");break;case "shutter":actual=state.optLong("shutter");break;case "aperture":actual=state.optDouble("requestedAperture",0);break;case "lens":actual=state.optString("cameraId");break;default:actual=state.opt(key);}
        if(actual instanceof Number&&target instanceof Number)return Math.abs(((Number)actual).doubleValue()-((Number)target).doubleValue())<.001;
        return Objects.equals(actual,target);
    }
    private String format(String key,Object value){
        double number=value instanceof Number?((Number)value).doubleValue():0;
        switch(key){
            case "fps":return (int)number+" fps";
            case "look":return CreatorLooks.NAMES[Math.max(0,Math.min(CreatorLooks.NAMES.length-1,(int)number))];
            case "lookStrength":return Math.round(number)+"%";
            case "iso":return String.valueOf(Math.max(0,(int)number));
            case "shutter":return number>0?"1/"+Math.round(1e9/number):"—";
            case "focus":return number<0?"AF":number<.01?"∞":String.format(Locale.US,"%.1fm",1/number);
            case "zoom":return String.format(Locale.US,"%.1f×",number);
            case "ev":return String.format(Locale.US,"%+.1f",number*state.optDouble("evStep",1));
            case "wb":return state.optBoolean("wbManual")||pending.containsKey("wbFine")?format("wbFine",current("wbFine")):wbName((int)number);
            case "wbFine":return String.format(Locale.US,"%+.1f",number);
            case "aperture":return number>0?String.format(Locale.US,"f/%.1f",number):"—";
            case "lens":{JSONArray lenses=state.optJSONArray("lenses");if(lenses!=null)for(int i=0;i<lenses.length();i++){JSONObject lens=lenses.optJSONObject(i);if(lens!=null&&lens.optString("id").equals(value))return lens.optString("name").startsWith("Front")?"Front":"Rear "+value;}return String.valueOf(value);}
            default:return "";
        }
    }
    private String adjustmentReadout(){return selected.equals("look")?format("look",current("look"))+" · "+format("lookStrength",current("lookStrength")):format(selected,current(selected));}
    private static String wbName(int mode){String[] names={"Off","Auto","Tungsten","Fluoro","Warm","Daylight","Cloudy","Twilight","Shade"};return mode>=0&&mode<names.length?names[mode]:"WB "+mode;}
    private static String wbIcon(int mode){return mode==1?"auto":mode==2?"incandescent":mode==3||mode==4?"fluorescent":mode==6||mode==8?"cloudy":"daylight";}
    private boolean supported(String key){
        switch(key){
            case "fps":{JSONArray rates=state.optJSONArray("fpsRates");return !state.optBoolean("recording")&&!state.optBoolean("busy")&&rates!=null&&rates.length()>1;}
            case "look":case "lookStrength":return state.optBoolean("lookSupported");
            case "iso":case "shutter":return state.optBoolean("manualSupported");
            case "focus":return state.optDouble("focusMax")>0;
            case "zoom":return state.optDouble("zoomMax",1)>1;
            case "ev":return state.optInt("evMax")>state.optInt("evMin");
            case "wb":return state.optBoolean("wbFineReady");
            case "aperture":{JSONArray a=state.optJSONArray("apertures");return a!=null&&a.length()>1;}
            case "lens":return !state.optBoolean("recording");
            default:return false;
        }
    }
    private void rebuildDock(){
        dock.setVisibility(selected.isEmpty()?GONE:VISIBLE);if(selected.isEmpty())return;
        modes.removeAllViews();presets.removeAllViews();minimum=0;maximum=1;logarithmic=false;
        title.setText(selected.equals("ev")?"EXPOSURE COMPENSATION":selected.equals("wb")?"WHITE BALANCE":selected.equals("aperture")?"APERTURE":selected.toUpperCase(Locale.US));
        slider.setContentDescription(title.getText()+" adjustment");
        if(selected.equals("aperture")&&state.has("cameraId")&&usable&&!supported("aperture")){String fixed=format("aperture",current("aperture"));hint.setText("Physical aperture is fixed at "+fixed+" on this lens. Adjust exposure with ISO, shutter or EV.");}else hint.setText(!state.has("cameraId")?"Connect a camera to adjust":!usable?"Waiting for camera":!supported(selected)?selected.equals("aperture")?"This lens has a fixed aperture":"Not adjustable on this camera":"Slide to adjust live");
        switch(selected){
            case "fps":{
                JSONArray rates=state.optJSONArray("fpsRates");maximum=rates==null?0:Math.max(0,rates.length()-1);
                for(int preset:new int[]{10,24,25,30,50,60})if(rates!=null)for(int i=0;i<rates.length();i++)if(rates.optInt(i)==preset)quick("fps",preset,String.valueOf(preset));
                hint.setText(state.optBoolean("recording")?"Stop recording to change FPS":"Release to apply · custom rates may use manual exposure");break;
            }
            case "look":{
                minimum=0;maximum=100;
                for(int i=0;i<CreatorLooks.NAMES.length;i++){final int choice=i;chip(presets,CreatorLooks.NAMES[i],null,state.optInt("look")==i,usable&&(i==0||supported("look")),()->sendImmediate("look",choice));}
                hint.setText(supported("look")?"Intensity · look is saved in the video":"Color curves unavailable on this camera");break;
            }
            case "iso":
                minimum=state.optDouble("isoMin",100);maximum=state.optDouble("isoMax",100);logarithmic=true;
                exposureModes();for(int value:new int[]{100,200,400,800,1600,3200})if(value>=minimum&&value<=maximum)quick("iso",value,String.valueOf(value));break;
            case "shutter":
                minimum=Math.max(1,Math.ceil(1e9/state.optDouble("shutterMax",33333333)));maximum=Math.max(minimum,Math.floor(1e9/Math.max(1,state.optDouble("shutterMin",100000))));logarithmic=true;
                exposureModes();for(int denominator:new int[]{30,50,60,100,120,250,500,1000}){long value=Math.round(1e9/denominator);if(denominator>=minimum&&denominator<=maximum)quick("shutter",value,"1/"+denominator);}break;
            case "focus":
                minimum=0;maximum=state.optDouble("focusMax",0);
                chip(modes,"AF","auto",state.optDouble("focus",-1)<0,usable,()->sendImmediate("focus",-1));
                chip(modes,"MF","focus",state.optDouble("focus",-1)>=0,usable&&supported(selected),()->sendImmediate("focus",Math.max(0,state.optDouble("focus"))));
                hint.setText(supported(selected)?"∞ far                                              near":"Fixed-focus lens");break;
            case "zoom":
                minimum=1;maximum=Math.max(1,state.optDouble("zoomMax",1));logarithmic=true;
                for(double value:new double[]{1,1.5,2,3,5,10})if(value<=maximum)quick("zoom",value,String.format(Locale.US,"%.1f×",value));break;
            case "ev":
                minimum=state.optInt("evMin");maximum=state.optInt("evMax");quick("ev",0,"Reset");
                chip(modes,"AE lock","lock",state.optBoolean("aeLock"),usable&&state.optBoolean("aeLockSupported")&&!state.optBoolean("manual"),()->commands.send("aeLock",!state.optBoolean("aeLock")));break;
            case "wb":{
                minimum=-100;maximum=100;
                JSONArray a=state.optJSONArray("wbs");
                if(a!=null)for(int i=0;i<a.length();i++){int mode=a.optInt(i);chip(presets,wbName(mode),wbIcon(mode),!state.optBoolean("wbManual")&&state.optInt("wb")==mode,usable,()->{pending.remove("wbFine");sendImmediate("wb",mode);});}
                chip(modes,"Reset",null,false,usable&&state.optBoolean("wbFineReady"),()->sendImmediate("wbFine",0));
                chip(modes,"Lock","lock",state.optBoolean("awbLock"),usable&&!state.optBoolean("wbManual")&&state.optBoolean("awbLockSupported"),()->commands.send("awbLock",!state.optBoolean("awbLock")));
                hint.setText(!state.optBoolean("wbFineSupported")?"Fine adjustment unavailable; presets still work":!state.optBoolean("wbFineReady")?"Waiting for camera white point":"Slide cool / warm; choose a preset to release manual WB");break;
            }
            case "aperture":{JSONArray a=state.optJSONArray("apertures");maximum=a==null?0:Math.max(0,a.length()-1);if(a!=null&&a.length()>1)for(int i=0;i<a.length();i++){double value=a.optDouble(i);quick("aperture",value,format("aperture",value));}break;}
            case "lens":{
                JSONArray a=state.optJSONArray("lenses");if(a!=null)for(int i=0;i<a.length();i++){JSONObject lens=a.optJSONObject(i);if(lens==null)continue;String id=lens.optString("id"),name=lens.optString("name").replace(" · "," ");chip(presets,name,"lens",state.optString("cameraId").equals(id),usable&&!state.optBoolean("recording"),()->sendImmediate("lens",id));}
                hint.setText(state.optBoolean("recording")?"Stop recording to change lenses":"Choose the camera lens");break;
            }
        }
        slider.setVisibility(selected.equals("lens")?GONE:VISIBLE);left.setVisibility(selected.equals("lens")?GONE:VISIBLE);right.setVisibility(selected.equals("lens")?GONE:VISIBLE);
        syncSlider();updateModeAppearance();
    }
    private void exposureModes(){
        chip(modes,"Auto","auto",!state.optBoolean("manual"),usable&&!state.optBoolean("fpsManual"),()->{pending.remove("iso");pending.remove("shutter");commands.send("auto",null);});
        chip(modes,"M",null,state.optBoolean("manual"),usable&&supported(selected),()->sendImmediate(selected,current(selected)));
    }
    private void quick(String action,Object value,String label){chip(presets,label,null,false,usable&&supported(action),()->sendImmediate(action,value));}
    private void updateModeAppearance(){
        if(modes.getChildCount()==0)return;
        if(selected.equals("iso")||selected.equals("shutter")){boolean manual=state.optBoolean("manual")||pending.containsKey("iso")||pending.containsKey("shutter");styleChip((TextView)modes.getChildAt(0),!manual);if(modes.getChildCount()>1)styleChip((TextView)modes.getChildAt(1),manual);}
        if(selected.equals("focus")){boolean auto=((Number)current("focus")).doubleValue()<0;styleChip((TextView)modes.getChildAt(0),auto);if(modes.getChildCount()>1)styleChip((TextView)modes.getChildAt(1),!auto);}
        if(selected.equals("wb")&&modes.getChildCount()>1)styleChip((TextView)modes.getChildAt(1),state.optBoolean("awbLock"));
        if(selected.equals("ev")&&modes.getChildCount()>0)styleChip((TextView)modes.getChildAt(0),state.optBoolean("aeLock"));
        if(selected.equals("look"))for(int i=0;i<presets.getChildCount();i++)styleChip((TextView)presets.getChildAt(i),((Number)current("look")).intValue()==i);
        if(selected.equals("wb")){JSONArray a=state.optJSONArray("wbs");if(a!=null)for(int i=0;i<presets.getChildCount()&&i<a.length();i++)styleChip((TextView)presets.getChildAt(i),!state.optBoolean("wbManual")&&!pending.containsKey("wbFine")&&a.optInt(i)==((Number)current("wb")).intValue());}
    }
    private void styleChip(TextView chip,boolean active){if(Objects.equals(chip.getTag(),active))return;chip.setTag(active);chip.setTextColor(active?YELLOW:TEXT);chip.setBackground(background(active));}
    private void syncSlider(){
        if(selected.isEmpty())return;Object value=current(selected);double raw=value instanceof Number?((Number)value).doubleValue():0;
        if(selected.equals("fps")){JSONArray rates=state.optJSONArray("fpsRates");int index=0;if(rates!=null)for(int i=0;i<rates.length();i++)if(rates.optInt(i)==(int)raw)index=i;raw=index;}
        if(selected.equals("look"))raw=((Number)current("lookStrength")).doubleValue();
        if(selected.equals("shutter"))raw=raw>0?1e9/raw:minimum;
        if(selected.equals("focus"))raw=Math.max(0,raw);
        if(selected.equals("wb"))raw=((Number)current("wbFine")).doubleValue();
        if(selected.equals("aperture")){JSONArray a=state.optJSONArray(selected.equals("wb")?"wbs":"apertures");double closest=Double.MAX_VALUE;int index=0;if(a!=null)for(int i=0;i<a.length();i++){double diff=Math.abs(a.optDouble(i)-raw);if(diff<closest){closest=diff;index=i;}}raw=index;}
        updating=true;slider.setProgress(SliderScale.progress(raw,1000,minimum,maximum,logarithmic));updating=false;
        if(selected.equals("fps")){JSONArray rates=state.optJSONArray("fpsRates");left.setText(rates!=null&&rates.length()>0?String.valueOf(rates.optInt(0)):"—");right.setText(rates!=null&&rates.length()>0?String.valueOf(rates.optInt(rates.length()-1)):"—");}
        else if(selected.equals("look")){left.setText("0%");right.setText("100%");}
        else if(selected.equals("focus")){left.setText("∞");right.setText("Near");}
        else if(selected.equals("shutter")){left.setText("1/"+(long)minimum);right.setText("1/"+(long)maximum);}
        else if(selected.equals("wb")){left.setText("Cool");right.setText("Warm");}
        else if(selected.equals("aperture")){JSONArray a=state.optJSONArray("apertures");left.setText(a!=null&&a.length()>0?format(selected,a.optDouble(0)):"—");right.setText(a!=null&&a.length()>0?format(selected,a.optDouble(a.length()-1)):"—");}
        else{left.setText(format(selected,minimum));right.setText(format(selected,maximum));}
        setText(readout,adjustmentReadout());
    }
    private Object sliderValue(int progress){
        double raw=SliderScale.value(progress,1000,minimum,maximum,logarithmic);
        switch(selected){
            case "fps":{JSONArray rates=state.optJSONArray("fpsRates");return rates==null||rates.length()==0?30:rates.optInt(Math.max(0,Math.min(rates.length()-1,(int)Math.round(raw))));}
            case "look":return (int)Math.round(raw);
            case "iso":case "ev":return (int)Math.round(raw);
            case "shutter":return Math.max(state.optLong("shutterMin",1),Math.min(state.optLong("shutterMax",33333333),Math.round(1e9/raw)));
            case "wb":return Math.round(raw*10)/10.0;
            case "aperture":{JSONArray a=state.optJSONArray(selected.equals("wb")?"wbs":"apertures");return a==null?0:a.opt(Math.max(0,Math.min(a.length()-1,(int)Math.round(raw))));}
            default:return raw;
        }
    }
    private void previewValue(String action,Object value){pending.put(action,value);pendingUntil.put(action,SystemClock.uptimeMillis()+1800);readout.setText(format(action,value));updateTiles();}
    private void sendImmediate(String action,Object value){if(!usable)return;handler.removeCallbacks(dispatch);scheduled=false;queuedAction=null;previewValue(action,value);commands.send(action,value);syncSlider();updateModeAppearance();}
    private void submit(String action,Object value,boolean immediate){
        queuedAction=action;queuedValue=value;
        if(immediate){handler.removeCallbacks(dispatch);dispatch.run();}
        else if(!scheduled){scheduled=true;handler.postDelayed(dispatch,80);}
    }
    @Override protected void onDetachedFromWindow(){handler.removeCallbacks(dispatch);scheduled=false;queuedAction=null;dragging=false;super.onDetachedFromWindow();}
}
