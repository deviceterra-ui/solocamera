package studio.solocamera;

import android.content.Context;
import android.graphics.*;
import android.hardware.display.DisplayManager;
import android.view.*;
import android.widget.*;
import java.util.ArrayList;

/** Event-driven screen ink: no screenshots, frame loop or additional recording encoder. */
final class ScreenAnnotations implements AutoCloseable,DisplayManager.DisplayListener {
    private final Context context;
    private final WindowManager windows;
    private final DisplayManager displays;
    private int rotation;
    private final Ink ink;
    private final LinearLayout panel;
    private final ScrollView toolbar;
    private final WindowManager.LayoutParams canvasParams, panelParams;
    private final ArrayList<Stroke> strokes=new ArrayList<>();
    private final ArrayList<Button> tools=new ArrayList<>();
    private int mode,color=0xffffd54f;
    private float width=5,pointerX=-1,pointerY=-1;
    private Stroke current;
    private boolean canvasAdded,panelAdded,closed,expanded=true;
    private static final int[] COLORS={0xffffd54f,0xffff5252,0xff40c4ff,0xff69f0ae,0xffffffff};
    private static final class Stroke {final Path path=new Path();final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);int points;}
    ScreenAnnotations(Context service){
        displays=service.getSystemService(DisplayManager.class);Display display=displays.getDisplay(Display.DEFAULT_DISPLAY);rotation=display.getRotation();
        Context visual=service.createDisplayContext(display);
        context=android.os.Build.VERSION.SDK_INT>=30?visual.createWindowContext(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,null):visual;
        windows=context.getSystemService(WindowManager.class);ink=new Ink(context);
        canvasParams=params(-1,-1);canvasParams.flags|=WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;canvasParams.alpha=.8f;canvasParams.setTitle("Solo Camera screen annotations");
        panel=new LinearLayout(context);panel.setOrientation(LinearLayout.VERTICAL);panel.setPadding(dp(4),dp(4),dp(4),dp(4));panel.setBackgroundColor(0xed202833);
        toolbar=new ScrollView(context);toolbar.setFillViewport(false);toolbar.addView(panel);panelParams=params(dp(64),Math.min(dp(478),context.getResources().getDisplayMetrics().heightPixels-dp(112)));panelParams.gravity=Gravity.TOP|Gravity.RIGHT;panelParams.y=dp(80);panelParams.setTitle("Solo Camera annotation tools");
        TextView grip=new TextView(context);grip.setText("⠿");grip.setTextColor(-1);grip.setTextSize(24);grip.setGravity(Gravity.CENTER);grip.setContentDescription("Drag annotation toolbar");panel.addView(grip,new LinearLayout.LayoutParams(-1,dp(32)));
        grip.setOnTouchListener(new View.OnTouchListener(){float y;int initial;public boolean onTouch(View v,android.view.MotionEvent e){if(e.getActionMasked()==0){y=e.getRawY();initial=panelParams.y;return true;}if(e.getActionMasked()==2){android.util.DisplayMetrics m=context.getResources().getDisplayMetrics();panelParams.y=Math.max(dp(24),Math.min(Math.max(dp(24),m.heightPixels-toolbar.getHeight()-dp(32)),initial+Math.round(e.getRawY()-y)));windows.updateViewLayout(toolbar,panelParams);return true;}return true;}});
        add("↕","Collapse or expand annotation toolbar",()->{expanded=!expanded;for(Button b:tools)b.setVisibility(expanded?View.VISIBLE:View.GONE);panelParams.height=expanded?Math.min(dp(478),context.getResources().getDisplayMetrics().heightPixels-dp(112)):dp(86);if(panelAdded)windows.updateViewLayout(toolbar,panelParams);});
        tool("☝","Touch: operate the app",0);tool("◎","Pointer: point without drawing",1);tool("✎","Pen: draw or write",2);tool("▰","Highlighter",3);
        Button colors=add("●","Change ink color",()->{int i=0;while(i<COLORS.length&&COLORS[i]!=color)i++;color=COLORS[(i+1)%COLORS.length];refresh();});tools.add(colors);
        tools.add(add("↔","Change stroke width",()->{width=width==5?10:width==10?18:5;refresh();}));
        tools.add(add("↶","Undo last stroke",()->{current=null;if(!strokes.isEmpty())strokes.remove(strokes.size()-1);ink.invalidate();}));
        tools.add(add("⌫","Clear all annotations",()->{current=null;strokes.clear();pointerX=-1;ink.invalidate();}));refresh();
    }
    private int dp(float x){return Math.round(x*context.getResources().getDisplayMetrics().density);}
    private WindowManager.LayoutParams params(int w,int h){return new WindowManager.LayoutParams(w,h,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);}
    private Button add(String icon,String label,Runnable action){Button b=new Button(context);b.setText(icon);b.setTextSize(22);b.setTextColor(-1);b.setPadding(0,0,0,0);b.setMinWidth(0);b.setMinimumWidth(0);b.setContentDescription(label);b.setOnClickListener(v->action.run());panel.addView(b,new LinearLayout.LayoutParams(-1,dp(46)));return b;}
    private void tool(String icon,String label,int selected){Button b=add(icon,label,()->select(selected));b.setTag(selected);tools.add(b);}
    private void select(int value){mode=value;current=null;pointerX=-1;if(mode==0)canvasParams.flags|=WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;else canvasParams.flags&=~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;if(canvasAdded)windows.updateViewLayout(ink,canvasParams);refresh();ink.invalidate();}
    private void refresh(){for(Button b:tools){Object tag=b.getTag();b.setTextColor("Change ink color".contentEquals(b.getContentDescription())?color:-1);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(tag instanceof Integer&&(Integer)tag==mode?0xff126d86:0xff35404e));if("Change stroke width".contentEquals(b.getContentDescription()))b.setText(width==5?"·":width==10?"•":"●");}}
    void show(){try{windows.addView(ink,canvasParams);canvasAdded=true;windows.addView(toolbar,panelParams);panelAdded=true;displays.registerDisplayListener(this,new android.os.Handler(android.os.Looper.getMainLooper()));Toast.makeText(context,"Use Pointer, Pen or Highlighter. Tap Touch to operate your app. Drag the grip to move tools.",Toast.LENGTH_LONG).show();}catch(RuntimeException e){close();throw e;}}
    private final class Ink extends View {
        final Paint pointer=new Paint(Paint.ANTI_ALIAS_FLAG);
        Ink(Context c){super(c);setContentDescription("Draw on recording screen");}
        @Override protected void onDraw(Canvas c){for(Stroke s:strokes)c.drawPath(s.path,s.paint);if(current!=null)c.drawPath(current.path,current.paint);if(pointerX>=0){pointer.setColor(color);pointer.setStyle(Paint.Style.FILL);c.drawCircle(pointerX,pointerY,dp(12),pointer);pointer.setColor(-1);pointer.setStyle(Paint.Style.STROKE);pointer.setStrokeWidth(dp(2));c.drawCircle(pointerX,pointerY,dp(15),pointer);}}
        @Override public boolean onTouchEvent(MotionEvent e){if(mode==0)return false;int action=e.getActionMasked();if(action==MotionEvent.ACTION_DOWN){if(mode==1){pointerX=e.getX();pointerY=e.getY();}else{current=new Stroke();current.paint.setColor(color);current.paint.setAlpha(mode==3?90:255);current.paint.setStyle(Paint.Style.STROKE);current.paint.setStrokeCap(Paint.Cap.ROUND);current.paint.setStrokeJoin(Paint.Join.ROUND);current.paint.setStrokeWidth(dp(mode==3?width*4:width));current.path.moveTo(e.getX(),e.getY());current.path.lineTo(e.getX()+.1f,e.getY());}}
            else if(action==MotionEvent.ACTION_MOVE){if(mode==1){pointerX=e.getX();pointerY=e.getY();}else if(current!=null){for(int i=0;i<e.getHistorySize()&&current.points<12000;i++){current.path.lineTo(e.getHistoricalX(i),e.getHistoricalY(i));current.points++;}if(current.points++<12000)current.path.lineTo(e.getX(),e.getY());}}
            else if(action==MotionEvent.ACTION_UP||action==MotionEvent.ACTION_CANCEL){pointerX=-1;if(current!=null){if(action==MotionEvent.ACTION_UP){if(strokes.size()>=200)strokes.remove(0);strokes.add(current);}current=null;}}invalidate();return true;}
    }
    @Override public void onDisplayAdded(int id){}
    @Override public void onDisplayRemoved(int id){}
    @Override public void onDisplayChanged(int id){if(closed||id!=Display.DEFAULT_DISPLAY)return;Display display=displays.getDisplay(id);if(display!=null&&display.getRotation()!=rotation){rotation=display.getRotation();strokes.clear();select(0);panelParams.y=dp(32);panelParams.height=expanded?Math.min(dp(478),context.getResources().getDisplayMetrics().heightPixels-dp(112)):dp(86);if(panelAdded)windows.updateViewLayout(toolbar,panelParams);Toast.makeText(context,"Screen rotated: annotations cleared, Touch mode restored.",Toast.LENGTH_SHORT).show();}}
    @Override public void close(){if(closed)return;closed=true;displays.unregisterDisplayListener(this);if(panelAdded){windows.removeViewImmediate(toolbar);panelAdded=false;}if(canvasAdded){windows.removeViewImmediate(ink);canvasAdded=false;}strokes.clear();current=null;}
}
