package studio.solocamera;

import android.app.AlertDialog;
import android.content.*;
import android.graphics.Color;
import android.os.*;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.UUID;

public final class PrompterView extends LinearLayout {
    public interface Changed { void changed(); }
    private final android.content.SharedPreferences prefs;
    private final TextView text,marker,speedValue,fontValue; private final ScrollView scroll; private final ImageButton play;
    private final SeekBar speedSlider,fontSlider;private final LinearLayout settings;
    private final Handler ticker=new Handler(Looper.getMainLooper());
    private Changed changed; private String revision=UUID.randomUUID().toString(),title="My script";
    private boolean playing,mirrored;private float position;private int speed=35,font=26;private long last;
    public PrompterView(Context context) {
        super(context);setOrientation(VERTICAL);setPadding(12,4,12,4);setBackgroundColor(0xff14191f);
        prefs=context.getSharedPreferences("scripts",Context.MODE_PRIVATE);
        settings=new LinearLayout(context);settings.setOrientation(VERTICAL);settings.setVisibility(GONE);
        LinearLayout bar=new LinearLayout(context);bar.setGravity(Gravity.CENTER_VERTICAL);
        ImageButton edit=button("script","Edit script",()->edit());bar.addView(edit,new LinearLayout.LayoutParams(0,dp(42),1));
        play=button("play","Play script",()->{setPlaying(!playing);notifyChanged();});bar.addView(play,new LinearLayout.LayoutParams(0,dp(42),1));
        bar.addView(button("reset","Restart script",()->{position=0;scrollToPosition();notifyChanged();}),new LinearLayout.LayoutParams(0,dp(42),1));
        bar.addView(button("next","Next paragraph",()->nextParagraph()),new LinearLayout.LayoutParams(0,dp(42),1));
        bar.addView(button("mirror","Mirror script",()->toggleMirror()),new LinearLayout.LayoutParams(0,dp(42),1));
        bar.addView(button("settings","Script speed and text size",()->settings.setVisibility(settings.getVisibility()==GONE?VISIBLE:GONE)),new LinearLayout.LayoutParams(0,dp(42),1));addView(bar);
        FrameLayout stage=new FrameLayout(context);scroll=new ScrollView(context);scroll.setFillViewport(true);
        text=new TextView(context);text.setTextColor(Color.WHITE);text.setTextSize(font);text.setLineSpacing(8,1.1f);text.setPadding(22,60,22,180);
        text.setText(prefs.getString("currentText","Write your next story.\n\nTap Script to paste or write your talking points.\n\nPlace this monitor close to the camera lens, then press Play when you are ready."));
        title=prefs.getString("currentTitle","My script");scroll.addView(text);stage.addView(scroll,new FrameLayout.LayoutParams(-1,-1));
        marker=new TextView(context);marker.setText("›");marker.setTextSize(32);marker.setTextColor(0xffffc857);
        FrameLayout.LayoutParams mark=new FrameLayout.LayoutParams(24,50);mark.topMargin=52;stage.addView(marker,mark);addView(stage,new LinearLayout.LayoutParams(-1,0,1));
        scroll.setOnTouchListener((v,event)->{if(event.getAction()==MotionEvent.ACTION_DOWN){setPlaying(false);notifyChanged();}if(event.getAction()==MotionEvent.ACTION_UP)postDelayed(()->{position=scroll.getScrollY();notifyChanged();},150);return false;});
        speedValue=new TextView(context);fontValue=new TextView(context);speedSlider=new SeekBar(context);fontSlider=new SeekBar(context);
        sliderRow(settings,speedValue,speedSlider,"Scroll speed",175,speed-5);sliderRow(settings,fontValue,fontSlider,"Text size",44,font-16);
        speedSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar s){}public void onProgressChanged(SeekBar s,int p,boolean user){if(user){speed=p+5;updateSettings();scheduleChanged();}}public void onStopTrackingTouch(SeekBar s){ticker.removeCallbacks(deferredChanged);notifyChanged();}});
        fontSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar s){}public void onProgressChanged(SeekBar s,int p,boolean user){if(user){font=p+16;text.setTextSize(font);updateSettings();scheduleChanged();}}public void onStopTrackingTouch(SeekBar s){ticker.removeCallbacks(deferredChanged);notifyChanged();}});
        updateSettings();addView(settings);
    }
    private int dp(float value){return (int)(value*getResources().getDisplayMetrics().density);}
    private void toggleMirror(){mirrored=!mirrored;text.setScaleX(mirrored?-1:1);notifyChanged();}
    private ImageButton button(String icon,String description,Runnable action){ImageButton b=new ImageButton(getContext());b.setImageDrawable(new CameraIcon(icon,0xffedf0f3));b.setPadding(dp(9),dp(9),dp(9),dp(9));b.setBackgroundColor(Color.TRANSPARENT);b.setContentDescription(description);b.setTooltipText(description);b.setOnClickListener(v->action.run());return b;}
    private void sliderRow(LinearLayout parent,TextView label,SeekBar slider,String description,int max,int value){LinearLayout row=new LinearLayout(getContext());row.setGravity(Gravity.CENTER_VERTICAL);label.setTextColor(0xff929ca8);label.setTextSize(10);slider.setContentDescription(description);slider.setMax(max);slider.setProgress(value);slider.setProgressTintList(android.content.res.ColorStateList.valueOf(0xffffc857));slider.setThumbTintList(android.content.res.ColorStateList.valueOf(0xffffc857));row.addView(label,new LinearLayout.LayoutParams(dp(82),dp(40)));row.addView(slider,new LinearLayout.LayoutParams(0,dp(40),1));parent.addView(row);}
    private void updateSettings(){speedValue.setText("Speed  "+speed);fontValue.setText("Size  "+font);}
    private final Runnable deferredChanged=this::notifyChanged;
    private void scheduleChanged(){ticker.removeCallbacks(deferredChanged);ticker.postDelayed(deferredChanged,80);}
    public void onChanged(Changed c){changed=c;}
    private void notifyChanged(){if(changed!=null)changed.changed();}
    public boolean isPlaying(){return playing;}
    public void setPlaying(boolean value){playing=value;play.setImageDrawable(new CameraIcon(playing?"pause":"play",playing?0xffffc857:0xffedf0f3));play.setContentDescription(playing?"Pause script":"Play script");last=SystemClock.uptimeMillis();}
    public void pauseAndNotify(){setPlaying(false);notifyChanged();}
    public void playAndNotify(){setPlaying(true);notifyChanged();}
    private void nextParagraph(){setPlaying(false);String body=text.getText().toString();android.text.Layout layout=text.getLayout();if(layout==null)return;int line=layout.getLineForVertical(Math.max(0,scroll.getScrollY()));int start=layout.getLineStart(line);int next=body.indexOf("\n\n",start+1);if(next>=0){int target=layout.getLineForOffset(Math.min(body.length(),next+2));position=layout.getLineTop(target);scrollToPosition();}notifyChanged();}
    private void scrollToPosition(){scroll.scrollTo(0,Math.max(0,(int)position));}
    private final Runnable tick=new Runnable(){public void run(){long now=SystemClock.uptimeMillis();if(playing){position+=Math.min(100,now-last)/1000f*speed*getResources().getDisplayMetrics().density;int max=Math.max(0,text.getHeight()-scroll.getHeight());position=Math.min(position,max);scrollToPosition();if(position>=max&&max>0){setPlaying(false);notifyChanged();}}last=now;ticker.postDelayed(this,32);}};
    @Override protected void onAttachedToWindow(){super.onAttachedToWindow();last=SystemClock.uptimeMillis();ticker.post(tick);}
    @Override protected void onDetachedFromWindow(){ticker.removeCallbacks(tick);ticker.removeCallbacks(deferredChanged);super.onDetachedFromWindow();}
    public JSONObject snapshot() {
        JSONObject o=new JSONObject();try{o.put("type","prompter");o.put("revision",revision);o.put("title",title);o.put("script",text.getText().toString());o.put("position",scroll.getScrollY()/getResources().getDisplayMetrics().density);o.put("speed",speed);o.put("font",font);o.put("playing",playing);o.put("mirrored",mirrored);}catch(Exception ignored){}return o;
    }
    public void apply(JSONObject o) throws Exception {
        String script=o.getString("script");if(script.length()>50000)throw new Exception("Script exceeds 50,000 characters");
        String rev=o.getString("revision");if(!revision.equals(rev)||!text.getText().toString().equals(script)){revision=rev;text.setText(script);}
        title=o.optString("title","Remote script");speed=Math.max(5,Math.min(180,o.getInt("speed")));font=Math.max(16,Math.min(60,o.getInt("font")));text.setTextSize(font);
        mirrored=o.optBoolean("mirrored");text.setScaleX(mirrored?-1:1);position=(float)Math.max(0,o.getDouble("position"))*getResources().getDisplayMetrics().density;speedSlider.setProgress(speed-5);fontSlider.setProgress(font-16);updateSettings();
        post(()->scrollToPosition());setPlaying(o.getBoolean("playing"));
    }
    private JSONObject saved(){try{return new JSONObject(prefs.getString("library","{}"));}catch(Exception e){return new JSONObject();}}
    private void edit(){
        setPlaying(false);notifyChanged();
        LinearLayout form=new LinearLayout(getContext());form.setOrientation(VERTICAL);form.setPadding(20,0,20,0);
        EditText name=new EditText(getContext());name.setHint("Script title");name.setSingleLine();name.setText(title);form.addView(name);
        EditText body=new EditText(getContext());body.setGravity(Gravity.TOP);body.setMinLines(6);body.setMaxLines(10);body.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);body.setText(text.getText());form.addView(body);
        AlertDialog dialog=new AlertDialog.Builder(getContext()).setTitle("Teleprompter script").setView(form).setPositiveButton("Save",null).setNeutralButton("Open saved",(d,w)->openSaved()).setNegativeButton("Cancel",null).create();
        dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            String script=body.getText().toString(),label=name.getText().toString().trim();
            if(label.isEmpty()){name.setError("Enter a title");return;}if(script.length()>50000){body.setError("Maximum 50,000 characters");return;}
            title=label;revision=UUID.randomUUID().toString();text.setText(script);position=0;scrollToPosition();
            JSONObject library=saved();try{library.put(title,script);}catch(Exception ignored){}
            prefs.edit().putString("library",library.toString()).putString("currentTitle",title).putString("currentText",script).apply();notifyChanged();dialog.dismiss();
        }));dialog.show();
    }
    private void openSaved(){JSONObject library=saved();java.util.ArrayList<String> names=new java.util.ArrayList<>();library.keys().forEachRemaining(names::add);if(names.isEmpty()){new AlertDialog.Builder(getContext()).setMessage("Save a named script first.").setPositiveButton("OK",null).show();return;}java.util.Collections.sort(names);new AlertDialog.Builder(getContext()).setTitle("Saved scripts").setItems(names.toArray(new String[0]),(d,w)->{title=names.get(w);text.setText(library.optString(title));revision=UUID.randomUUID().toString();position=0;scrollToPosition();prefs.edit().putString("currentTitle",title).putString("currentText",text.getText().toString()).apply();notifyChanged();}).show();}
}
