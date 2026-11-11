package studio.solocamera;

import android.content.Context;
import android.graphics.*;
import android.os.*;
import android.text.*;
import android.text.style.*;
import android.view.*;
import android.widget.FrameLayout;
import androidx.media3.common.*;
import androidx.media3.exoplayer.*;
import java.util.*;

/** Interactive preview: decoders own source frames, views own editable geometry.
 * No encoder, temporary movie, or composition rebuild runs on an edit gesture. */
@androidx.annotation.OptIn(markerClass=androidx.media3.common.util.UnstableApi.class)
final class EditorLivePreview extends FrameLayout {
    interface Listener { void frame(); void error(String message); }
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final FrameLayout canvas;
    private final VideoLayer mainLayer;private EditorSafeGuides safeGuides;
    private final ArrayList<VideoLayer> overlays=new ArrayList<>();
    private final ArrayList<CaptionLayer> captions=new ArrayList<>();
    private final Listener listener;
    private EditorProject project;
    private long position,lastTick;
    private boolean playing,released;private final EditorTitleSoundPreview titleSounds;
    private final IdentityHashMap<ExoPlayer,VoiceEnhancementAudioProcessor> cleanupProcessors=new IdentityHashMap<>();private ExoPlayer music;private final IdentityHashMap<EditorProject.Audio,ExoPlayer> audioPlayers=new IdentityHashMap<>();private final IdentityHashMap<EditorProject.Audio,Long> audioSeek=new IdentityHashMap<>();
    private boolean monitorMuted;private CreatorDucking ducking;private final IdentityHashMap<ExoPlayer,SyncAudioProcessor> syncProcessors=new IdentityHashMap<>();
    android.graphics.Rect captureRect(){int[] location=new int[2];canvas.getLocationInWindow(location);return new android.graphics.Rect(location[0],location[1],location[0]+canvas.getWidth(),location[1]+canvas.getHeight());}
    void showGuides(boolean on){if(safeGuides!=null)safeGuides.setVisibility(on?VISIBLE:INVISIBLE);}
    void muteMonitoring(boolean value){monitorMuted=value;render(false);}
    private String musicUri="",compositeEffectsKey="";
    private long musicSeek=Long.MIN_VALUE;
    int decoderCreations,renderedFrames;

    EditorLivePreview(Context context,Listener listener){
        super(context);titleSounds=new EditorTitleSoundPreview(context);this.listener=listener;setBackgroundColor(Color.BLACK);
        canvas=new FrameLayout(context);canvas.setClipChildren(true);canvas.setClipToPadding(true);
        addView(canvas,new LayoutParams(1,1,Gravity.CENTER));
        mainLayer=new VideoLayer();canvas.addView(mainLayer,new LayoutParams(-1,-1));
        handler.post(pulse);
    }
    void bind(EditorProject value){project=value;ducking=new CreatorDucking(value);position=Math.min(position,Math.max(0,value.duration()));
        reconcileLayers();
        layoutCanvas();render(true);lastTick=SystemClock.elapsedRealtime();
    }
    private void reconcileLayers(){
        while(overlays.size()>project.videoTracks()){VideoLayer layer=overlays.remove(overlays.size()-1);layer.release();canvas.removeView(layer);}
        while(overlays.size()<project.videoTracks()){VideoLayer layer=new VideoLayer();overlays.add(layer);canvas.addView(layer,overlays.size(),new LayoutParams(-1,-1));}
        while(captions.size()>project.captions.size()){CaptionLayer layer=captions.remove(captions.size()-1);canvas.removeView(layer);}
        while(captions.size()<project.captions.size()){CaptionLayer layer=new CaptionLayer();captions.add(layer);canvas.addView(layer,new LayoutParams(-1,-1));}
    }
    String diagnostics(){String result="duration="+(project==null?0:project.duration())+" released="+released+" position="+position+" playing="+playing+" main="+(mainLayer.decoder==null?"none":mainLayer.decoder.getPlaybackState()+"/"+mainLayer.decoder.getCurrentPosition());for(VideoLayer layer:overlays)result+=" overlay="+(layer.decoder==null?"none":layer.decoder.getPlaybackState()+"/"+layer.decoder.getCurrentPosition());return result+" titleSounds="+titleSounds.diagnostics();}
    long position(){return position;}
    boolean isPlaying(){return playing;}
    void play(){if(released||project==null)return;if(position>=project.duration()-1){mainLayer.release();for(VideoLayer layer:overlays)layer.release();seekTo(0);}playing=true;lastTick=SystemClock.elapsedRealtime();render(false);}
    void pause(){playing=false;titleSounds.pause();mainLayer.pause();for(VideoLayer layer:overlays)layer.pause();if(music!=null)music.pause();for(ExoPlayer audio:audioPlayers.values())audio.pause();}
    void seekTo(long time){if(project==null)return;position=Math.max(0,Math.min(time,project.duration()));lastTick=SystemClock.elapsedRealtime();render(true);}
    void refresh(){if(project==null||released)return;layoutCanvas();render(false);}
    private final Runnable pulse=new Runnable(){public void run(){if(released)return;long now=SystemClock.elapsedRealtime();
        if(project!=null){if(playing){long elapsed=lastTick==0?0:Math.min(100,now-lastTick);if(ready())position=Math.min(project.duration(),position+elapsed);if(position>=project.duration())pause();}render(false);}lastTick=now;handler.postDelayed(this,33);
    }};
    private boolean ready(){for(ExoPlayer audio:audioPlayers.values())if(audio.getPlaybackState()==Player.STATE_BUFFERING)return false;if(mainLayer.getVisibility()==VISIBLE&&!mainLayer.ready())return false;for(VideoLayer layer:overlays)if(layer.getVisibility()==VISIBLE&&!layer.ready())return false;return true;}
    private void layoutCanvas(){if(project==null||getWidth()==0||getHeight()==0)return;int[] size=EditorComposition.size(project);double ratio=(double)size[0]/size[1];int w=getWidth(),h=(int)(w/ratio);if(h>getHeight()){h=getHeight();w=(int)(h*ratio);}LayoutParams lp=(LayoutParams)canvas.getLayoutParams();if(lp.width!=w||lp.height!=h){lp.width=w;lp.height=h;canvas.setLayoutParams(lp);}}
    @Override protected void onMeasure(int widthSpec,int heightSpec){super.onMeasure(widthSpec,heightSpec);if(project!=null){int[] size=EditorComposition.size(project);double ratio=(double)size[0]/size[1];int w=getMeasuredWidth(),h=(int)(w/ratio);if(h>getMeasuredHeight()){h=getMeasuredHeight();w=(int)(h*ratio);}canvas.measure(MeasureSpec.makeMeasureSpec(Math.max(1,w),MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(Math.max(1,h),MeasureSpec.EXACTLY));}}
    @Override protected void onLayout(boolean changed,int l,int t,int r,int b){int w=canvas.getMeasuredWidth(),h=canvas.getMeasuredHeight();canvas.layout((getWidth()-w)/2,(getHeight()-h)/2,(getWidth()+w)/2,(getHeight()+h)/2);}
    @Override protected void onSizeChanged(int w,int h,int oldW,int oldH){super.onSizeChanged(w,h,oldW,oldH);layoutCanvas();}
    private void render(boolean seek){if(project==null||released)return;reconcileLayers();long t=Math.min(position,Math.max(0,project.duration()-1));applyCompositeEffects(t);long offset=0;EditorProject.Clip active=null;
        for(int i=0;i<project.clips.size();i++){EditorProject.Clip c=project.clips.get(i);offset=project.offset(i);if(t>=offset&&t<offset+c.length()){active=c;break;}}
        if(active==null)mainLayer.hide();else mainLayer.show(active,t-offset,seek,1,0,0,1,false);
        ArrayList<EditorProject.Overlay> visible=new ArrayList<>();for(EditorProject.Overlay o:project.overlays)if(t>=o.at&&t<o.at+o.clip.length())visible.add(o);visible.sort((a,b)->Integer.compare(a.track,b.track));for(int i=0;i<overlays.size();i++){if(i>=visible.size())overlays.get(i).hide();else{EditorProject.Overlay o=visible.get(i);overlays.get(i).show(o.clip,t-o.at,seek,o.scale,o.x,o.y,o.alpha,true);}}
        for(int i=0;i<captions.size();i++){EditorProject.Caption c=project.captions.get(i);CaptionLayer layer=captions.get(i);if(t<c.at||t>=c.end)layer.setVisibility(INVISIBLE);else{layer.setVisibility(VISIBLE);layer.show(c,t-c.at);if(c.style.privacyBlackout)layer.bringToFront();}}
        if(safeGuides==null){safeGuides=new EditorSafeGuides(getContext());canvas.addView(safeGuides,new LayoutParams(-1,-1));}safeGuides.bringToFront();safeGuides.mode(project.safeGuide);syncMusic(t,seek);syncAudio(t,seek);
        // Decorative accents follow the editor clock; their buffering must never stop video.
        titleSounds.sync(project,t,seek,playing,monitorMuted);boolean run=playing&&ready();titleSounds.run(run);for(ExoPlayer audio:audioPlayers.values())audio.setPlayWhenReady(run);mainLayer.run(run);for(VideoLayer layer:overlays)layer.run(run);if(music!=null)music.setPlayWhenReady(run&&!project.music.isEmpty()&&t>=project.musicAt&&t<Math.min(project.duration(),project.musicEnd));
    }
    private void applyCompositeEffects(long time){StringBuilder key=new StringBuilder();for(EditorProject.EffectClip fx:project.effects)if(time>=fx.at&&time<fx.end)key.append(fx.preset).append(':').append(fx.intensity).append(';');String next=key.toString();if(next.equals(compositeEffectsKey))return;compositeEffectsKey=next;if(next.isEmpty()){canvas.setLayerType(View.LAYER_TYPE_NONE,null);return;}android.graphics.Paint paint=new android.graphics.Paint();android.graphics.ColorMatrix combined=new android.graphics.ColorMatrix();for(EditorProject.EffectClip fx:project.effects){if(time<fx.at||time>=fx.end)continue;float a=(float)fx.intensity,contrast=1,red=1,green=1,blue=1,bias=0,saturation=1;switch(fx.preset){case 1:contrast=1+.18f*a;red=1+.04f*a;blue=1-.05f*a;break;case 2:red=1+.07f*a;green=1+.025f*a;blue=1-.06f*a;break;case 3:red=1+.12f*a;blue=1-.1f*a;break;case 4:blue=1+.12f*a;red=1-.08f*a;break;case 5:contrast=1-.2f*a;bias=.025f*a;break;case 6:red=1+.12f*a;green=1+.03f*a;blue=1-.12f*a;break;case 7:contrast=1+.22f*a;break;case 8:contrast=1-.16f*a;saturation=1-.15f*a;break;case 9:saturation=1-.5f*a;break;case 10:red=1+.06f*a;blue=1-.06f*a;break;}android.graphics.ColorMatrix grade=new android.graphics.ColorMatrix(new float[]{red*contrast,0,0,0,bias*255,0,green*contrast,0,0,bias*255,0,0,blue*contrast,0,bias*255,0,0,0,1,0});android.graphics.ColorMatrix sat=new android.graphics.ColorMatrix();sat.setSaturation(saturation);grade.postConcat(sat);combined.postConcat(grade);}paint.setColorFilter(new android.graphics.ColorMatrixColorFilter(combined));canvas.setLayerType(View.LAYER_TYPE_HARDWARE,paint);}
    // Interleaved/high-bitrate recordings can fill the byte target before audio is buffered.
    // Always load the minimum playable duration, or paused decoders and the editor clock deadlock.
    private ExoPlayer createPlayer(){decoderCreations++;VoiceEnhancementAudioProcessor enhancer=new VoiceEnhancementAudioProcessor(0);SyncAudioProcessor syncProcessor=new SyncAudioProcessor(0);ExoPlayer created=new ExoPlayer.Builder(getContext())
        .setRenderersFactory(new DefaultRenderersFactory(getContext()){@Override protected androidx.media3.exoplayer.audio.AudioSink buildAudioSink(Context context,boolean floatOutput,boolean playbackParams){return new androidx.media3.exoplayer.audio.DefaultAudioSink.Builder(context).setEnableFloatOutput(false).setEnableAudioTrackPlaybackParams(false).setAudioProcessors(new androidx.media3.common.audio.AudioProcessor[]{syncProcessor,enhancer}).build();}}.setEnableDecoderFallback(true))
        .setLoadControl(new DefaultLoadControl.Builder().setBufferDurationsMs(300,1800,150,300).setTargetBufferBytes(2*1024*1024).setPrioritizeTimeOverSizeThresholds(true).build()).build();cleanupProcessors.put(created,enhancer);syncProcessors.put(created,syncProcessor);return created;}
    private void syncMusic(long t,boolean seek){boolean audible=!project.music.isEmpty()&&t>=project.musicAt&&t<Math.min(project.duration(),project.musicEnd);
        if(!audible){if(music!=null)music.pause();return;}
        if(music==null){music=createPlayer();music.addListener(new Player.Listener(){public void onPlayerError(PlaybackException e){EditorLivePreview.this.pause();listener.error("Music: "+e.getMessage());}});}
        boolean changed=!musicUri.equals(project.music);if(changed){musicUri=project.music;music.setMediaItem(MediaItem.fromUri(musicUri));music.prepare();music.setRepeatMode(Player.REPEAT_MODE_ONE);}
        long target=(t-project.musicAt)%Math.max(200,project.musicDuration);
        if(changed||seek||(playing&&Math.abs(music.getCurrentPosition()-target)>250)){music.seekTo(target);musicSeek=target;}
        music.setVolume(monitorMuted?0:(float)project.musicVolume);
    }
    private void syncAudio(long t,boolean seek){Iterator<Map.Entry<EditorProject.Audio,ExoPlayer>> iterator=audioPlayers.entrySet().iterator();while(iterator.hasNext()){Map.Entry<EditorProject.Audio,ExoPlayer> e=iterator.next();EditorProject.Audio a=e.getKey();if(!project.audios.contains(a)||t<a.at||t>=a.at+a.length()){cleanupProcessors.remove(e.getValue());syncProcessors.remove(e.getValue());e.getValue().release();audioSeek.remove(a);iterator.remove();}}
        for(EditorProject.Audio a:project.audios){if(t<a.at||t>=a.at+a.length())continue;ExoPlayer audio=audioPlayers.get(a);boolean fresh=audio==null;if(fresh){audio=createPlayer();audio.setTrackSelectionParameters(audio.getTrackSelectionParameters().buildUpon().setTrackTypeDisabled(C.TRACK_TYPE_VIDEO,true).build());audio.setMediaItem(MediaItem.fromUri(a.uri));audio.addListener(new Player.Listener(){public void onPlayerError(PlaybackException e){pause();listener.error("Audio "+a.name+": "+e.getMessage());}});audio.prepare();audioPlayers.put(a,audio);}long target=a.start+t-a.at,now=SystemClock.elapsedRealtime();if(fresh||seek||(playing&&audio.getPlaybackState()==Player.STATE_READY&&now-audioSeek.getOrDefault(a,0L)>750&&Math.abs(audio.getCurrentPosition()-target)>250)){audio.seekTo(target);audioSeek.put(a,now);}cleanupProcessors.get(audio).strength(a.voiceEnhance?a.voiceStrength:0);audio.setVolume(monitorMuted?0:(float)Math.min(1,a.volume*(a.duck?ducking.gain(t):1)));audio.setPlaybackParameters(new PlaybackParameters(1,(float)a.pitch));}
    }
    void release(){if(released)return;pause();released=true;titleSounds.release();handler.removeCallbacks(pulse);mainLayer.release();for(VideoLayer layer:overlays)layer.release();if(music!=null){music.release();music=null;}for(ExoPlayer audio:audioPlayers.values())audio.release();audioPlayers.clear();audioSeek.clear();cleanupProcessors.clear();syncProcessors.clear();canvas.removeAllViews();}

    private final class VideoLayer extends MaskLayer {
        final TextureView texture;Surface surface;ExoPlayer decoder;String uri="",geometry="";EditorProject.Clip clip;long lastSeek=Long.MIN_VALUE,lastSeekWall,hiddenAt;boolean firstFrame;
        VideoLayer(){super();texture=new TextureView(getContext());texture.setOpaque(false);addView(texture,new LayoutParams(-1,-1));}
        boolean ready(){return decoder!=null&&(decoder.getPlaybackState()==Player.STATE_READY||decoder.getPlaybackState()==Player.STATE_ENDED);}
        void pause(){if(decoder!=null)decoder.pause();}
        void run(boolean enabled){if(decoder!=null)decoder.setPlayWhenReady(enabled&&getVisibility()==VISIBLE);}
        void hide(){if(getVisibility()==VISIBLE)hiddenAt=SystemClock.elapsedRealtime();setVisibility(INVISIBLE);pause();if(SystemClock.elapsedRealtime()-hiddenAt>500)release();}
        void release(){if(decoder!=null){cleanupProcessors.remove(decoder);syncProcessors.remove(decoder);decoder.clearVideoSurface();decoder.release();decoder=null;uri="";clip=null;lastSeek=Long.MIN_VALUE;if(surface!=null){surface.release();surface=null;}}}
        void show(EditorProject.Clip c,long local,boolean seek,double scale,double x,double y,double alpha,boolean muted){
            boolean entering=getVisibility()!=VISIBLE;setVisibility(VISIBLE);
            if(decoder==null){decoder=createPlayer();if(texture.isAvailable()){surface=new Surface(texture.getSurfaceTexture());decoder.setVideoSurface(surface);}decoder.addListener(new Player.Listener(){public void onRenderedFirstFrame(){firstFrame=true;renderedFrames++;listener.frame();}public void onPlayerError(PlaybackException e){EditorLivePreview.this.pause();listener.error("Video: "+e.getMessage());}});
                texture.setSurfaceTextureListener(new TextureView.SurfaceTextureListener(){public void onSurfaceTextureAvailable(SurfaceTexture s,int w,int h){if(surface!=null)surface.release();surface=new Surface(s);if(decoder!=null)decoder.setVideoSurface(surface);}public void onSurfaceTextureSizeChanged(SurfaceTexture s,int w,int h){}public boolean onSurfaceTextureDestroyed(SurfaceTexture s){if(decoder!=null)decoder.clearVideoSurface();if(surface!=null){surface.release();surface=null;}return true;}public void onSurfaceTextureUpdated(SurfaceTexture s){renderedFrames++;}});
            }
            boolean sourceChanged=!uri.equals(c.uri),clipChanged=clip!=c;clip=c;
            if(sourceChanged){uri=c.uri;firstFrame=false;decoder.setMediaItem(MediaItem.fromUri(uri));decoder.prepare();}
            long target=Math.max(c.start,Math.min(c.end-1,c.start+Math.round(local*c.speed)));
            if(sourceChanged||clipChanged||entering||seek||(playing&&ready()&&SystemClock.elapsedRealtime()-lastSeekWall>1000&&Math.abs(decoder.getCurrentPosition()-target)>300)){if(seek||entering||lastSeek!=target||sourceChanged||clipChanged||playing){decoder.seekTo(target);lastSeek=target;lastSeekWall=SystemClock.elapsedRealtime();}}
            cleanupProcessors.get(decoder).strength(c.voiceEnhance?c.voiceStrength:0);decoder.setPlaybackParameters(new PlaybackParameters((float)c.speed,(float)c.pitch));syncProcessors.get(decoder).offset(c.audioSyncMs);decoder.setVolume(muted||monitorMuted?0:(float)Math.min(1,c.volume));
            EditorMotion.Key motion=EditorMotion.sample(c,local,false);geometry(c);mask(c,local);float progress=(float)Math.max(0,Math.min(1,local/(double)Math.max(1,c.length())));float anim=c.animation==1?1+.2f*progress:c.animation==2?1.2f-.2f*progress:1;
            transitionState=EditorTransitions.sample(c,local);setScaleX((float)(scale*motion.scale*c.stretchX*transitionState.sx)*anim);setScaleY((float)(scale*motion.scale*c.stretchY*transitionState.sy)*anim);invalidate();
            setTranslationX((float)(x+scale*(motion.x+(c.animation==3?.4*(progress-.5):0)))*getWidth()/2);setTranslationY((float)(-y-scale*motion.y)*getHeight()/2);setAlpha((float)(alpha*motion.alpha*c.opacity));setRotation((float)-motion.rotation);
            double fade=c.fade<=0?1:Math.max(0,Math.min(1,Math.min(local, c.length()-local)/(double)Math.min(c.fade,c.length()/2)));applyColor(c,fade*transitionState.gain,transitionState);applyToneCurve(c);
        }
        void geometry(EditorProject.Clip c){int w=getWidth(),h=getHeight();if(w==0||h==0)return;String key=w+":"+h+":"+c.width+":"+c.height+":"+c.rotation+":"+c.mirror+":"+c.cropX+":"+c.cropY+":"+project.fill;
            if(!key.equals(geometry)){geometry=key;boolean rotated=c.rotation%2!=0;double cw=c.width*(1-2*c.cropX),ch=c.height*(1-2*c.cropY);double rw=rotated?ch:cw,rh=rotated?cw:ch;double fit=project.fill?Math.max(w/rw,h/rh):Math.min(w/rw,h/rh);
                Matrix m=new Matrix();m.setScale((float)(c.width*fit/w)*(c.mirror?-1:1),(float)(c.height*fit/h),w/2f,h/2f);m.postRotate(-c.rotation*90,w/2f,h/2f);texture.setTransform(m);content.set((float)(w-rw*fit)/2,(float)(h-rh*fit)/2,(float)(w+rw*fit)/2,(float)(h+rh*fit)/2);invalidate();}

        }
        String colorKey="";
        void applyToneCurve(EditorProject.Clip c){if(android.os.Build.VERSION.SDK_INT>=31)VideoLayer.this.setRenderEffect(null);}
        void applyColor(EditorProject.Clip c,double fade,EditorTransitions.State transition){String key=c.look+":"+c.intensity+":"+c.brightness+":"+c.contrast+":"+c.saturation+":"+(int)(fade*255)+":"+(int)(transition.r*255)+":"+(int)(transition.g*255)+":"+(int)(transition.b*255);if(key.equals(colorKey))return;colorKey=key;
            double amount=c.intensity,r=1,g=1,b=1,brightness=c.brightness,contrast=c.contrast,saturation=c.saturation;
            switch(c.look){case 1:contrast+=.18*amount;r+=.04*amount;b-=.05*amount;break;case 2:brightness+=.06*amount;saturation+=10*amount;break;case 3:r+=.12*amount;b-=.10*amount;break;case 4:b+=.12*amount;r-=.08*amount;break;case 5:contrast-=.25*amount;brightness+=.04*amount;break;default:break;}
            double curveMix=c.curveStrength,masterGain=1+((c.curveMaster[4]-c.curveMaster[0])-1)*curveMix,masterLift=c.curveMaster[0]*curveMix;double redLift=(masterLift+masterGain*c.curveRed[0]*curveMix),greenLift=(masterLift+masterGain*c.curveGreen[0]*curveMix),blueLift=(masterLift+masterGain*c.curveBlue[0]*curveMix);r*=masterGain*(1+((c.curveRed[4]-c.curveRed[0])-1)*curveMix);g*=masterGain*(1+((c.curveGreen[4]-c.curveGreen[0])-1)*curveMix);b*=masterGain*(1+((c.curveBlue[4]-c.curveBlue[0])-1)*curveMix);
            float gain=(float)((1+contrast)/(1-contrast)),bias=(float)((.5-.5*gain+brightness)*255),biasR=(float)(bias+redLift*255),biasG=(float)(bias+greenLift*255),biasB=(float)(bias+blueLift*255);ColorMatrix cm=new ColorMatrix();cm.setSaturation((float)(1+saturation/100));cm.postConcat(new ColorMatrix(new float[]{(float)(r*gain*fade),0,0,0,(float)(biasR*fade+255*transition.r),0,(float)(g*gain*fade),0,0,(float)(biasG*fade+255*transition.g),0,0,(float)(b*gain*fade),0,(float)(biasB*fade+255*transition.b),0,0,0,1,0}));Paint color=new Paint();color.setColorFilter(new ColorMatrixColorFilter(cm));setLayerType(View.LAYER_TYPE_HARDWARE,color);
        }
    }

    private class MaskLayer extends FrameLayout {
        EditorTransitions.State transitionState=new EditorTransitions.State();final RectF content=new RectF();final Paint maskPaint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);Bitmap maskBitmap;String maskKey="";
        MaskLayer(){super(EditorLivePreview.this.getContext());setWillNotDraw(false);setClipChildren(false);maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));}
        void mask(EditorProject.Clip c,long local){String key=c.comparisonMode+":"+Math.round(EditorCompare.split(c,local)*192)+":"+c.mask+":"+c.feather+":"+c.maskX+":"+c.maskY+":"+c.maskWidth+":"+c.maskHeight+":"+c.maskRotation;if(key.equals(maskKey))return;maskKey=key;if(maskBitmap!=null){maskBitmap.recycle();maskBitmap=null;}if(c.mask!=0||c.comparisonMode!=0){int side=192;int[] pixels=new int[side*side];for(int y=0;y<side;y++)for(int x=0;x<side;x++)pixels[y*side+x]=((int)(255*(c.comparisonMode!=0?EditorCompare.alpha((x+.5)*2/side-1,(y+.5)*2/side-1,c,local):EditorMask.alpha((x+.5)*2/side-1,(y+.5)*2/side-1,c)))<<24)|0xffffff;maskBitmap=Bitmap.createBitmap(pixels,side,side,Bitmap.Config.ARGB_8888);}invalidate();}

        @Override protected void dispatchDraw(Canvas c){int save=c.saveLayer(0,0,getWidth(),getHeight(),null);if(!content.isEmpty()){c.clipRect(content);EditorTransitionDrawing.clip(c,content,transitionState);}super.dispatchDraw(c);if(maskBitmap!=null&&!content.isEmpty())c.drawBitmap(maskBitmap,null,content,maskPaint);c.restoreToCount(save);}
    }
    private final class CaptionLayer extends View {
        final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);Bitmap bitmap,revealBitmap;EditorTitleAnimation titleAnimation;EditorCaptionStyles.Animation captionAnimation;int revealFrame=-1;String key="";EditorProject.Caption caption;long local;
        CaptionLayer(){super(EditorLivePreview.this.getContext());}
        void show(EditorProject.Caption c,long time){caption=c;local=time;String next=c.style.privacyBlackout+":"+c.style.privacyWidth+":"+c.style.privacyHeight+":"+c.style.titleGlow+":"+c.style.titleParticles+":"+c.style.captionStyle+":"+c.style.captionAccent+":"+c.style.designLogo+":"+c.style.maskX+":"+c.style.maskY+":"+c.style.maskWidth+":"+c.style.maskHeight+":"+c.style.maskRotation+":"+c.style.imageUri+":"+c.style.titleDesign+":"+c.style.designAccent+":"+c.style.designSubtitle+":"+c.style.designEyebrow+":"+c.style.backgroundEnabled+":"+c.style.shadowEnabled+":"+c.style.backgroundPadding+":"+c.style.backgroundRadius+":"+c.style.font+":"+c.style.bold+":"+c.style.italic+":"+c.style.alignment+":"+c.style.textBackground+":"+c.style.outline+":"+c.style.outlineColor+":"+c.style.shadowBlur+":"+c.style.shadowX+":"+c.style.shadowY+":"+c.style.shadowColor+":"+c.style.lineSpacing+":"+c.style.text+":"+c.style.textSize+":"+c.style.textColor+":"+getWidth()+":"+getHeight()+":"+c.style.cropX+":"+c.style.cropY+":"+c.style.mask+":"+c.style.feather;
            if(getWidth()>0&&getHeight()>0&&!next.equals(key)){key=next;if(captionAnimation!=null){captionAnimation.release();captionAnimation=null;}if(titleAnimation!=null){titleAnimation.release();titleAnimation=null;}if(revealBitmap!=null&&revealBitmap!=bitmap)revealBitmap.recycle();revealBitmap=null;revealFrame=-1;if(bitmap!=null)bitmap.recycle();bitmap=EditorText.bitmap(c.style,getWidth(),getHeight());}invalidate();}
        @Override protected void onVisibilityChanged(View changed,int visibility){super.onVisibilityChanged(changed,visibility);if(visibility!=VISIBLE&&captionAnimation!=null){captionAnimation.release();captionAnimation=null;}if(visibility!=VISIBLE&&titleAnimation!=null){titleAnimation.release();titleAnimation=null;}}
        @Override protected void onDetachedFromWindow(){if(titleAnimation!=null){titleAnimation.release();titleAnimation=null;}if(captionAnimation!=null){captionAnimation.release();captionAnimation=null;}super.onDetachedFromWindow();}
        @Override protected void onDraw(Canvas canvas){if(bitmap==null||caption==null)return;EditorProject.Clip c=caption.style;EditorMotion.Key motion=EditorMotion.sample(c,local,true,caption.end-caption.at);float progress=(float)(local/(double)Math.max(1,caption.end-caption.at)),anim=c.animation==1?1+.2f*progress:c.animation==2?1.2f-.2f*progress:1;float x=(float)motion.x+(c.animation==3?.4f*(progress-.5f):0),y=(float)motion.y;float alpha=c.fade==0?1:(float)Math.max(0,Math.min(1,Math.min(local,caption.end-caption.at-local)/(double)Math.min(c.fade,(caption.end-caption.at)/2)));paint.setAlpha((int)(255*alpha*motion.alpha));canvas.save();canvas.translate(getWidth()*(1+x)/2,getHeight()*(1-y)/2);canvas.rotate((float)(-c.rotation*90-motion.rotation));canvas.scale((float)(motion.scale*c.stretchX)*anim*(c.mirror?-1:1),(float)(motion.scale*c.stretchY)*anim);Bitmap shown=bitmap;if(c.captionStyle>0&&c.titleDesign==0&&c.imageUri.isEmpty()){if(captionAnimation==null)captionAnimation=new EditorCaptionStyles.Animation(c,getWidth(),getHeight());shown=captionAnimation.frame(local,caption.end-caption.at);}else if(c.titleDesign>0&&c.titleMotion>0){if(titleAnimation==null)titleAnimation=new EditorTitleAnimation(c,getWidth(),getHeight());shown=titleAnimation.frame(local,caption.end-caption.at);}else if(c.textReveal>0){int f=Math.round(progress*30);if(f!=revealFrame){if(revealBitmap!=null&&revealBitmap!=bitmap)revealBitmap.recycle();revealBitmap=EditorTextReveal.frame(bitmap,c.textReveal,f/30f);revealFrame=f;}shown=revealBitmap;}paint.setAlpha(c.privacyBlackout?255:(int)(255*alpha*motion.alpha*c.opacity));canvas.drawBitmap(shown,-shown.getWidth()/2f,-shown.getHeight()/2f,paint);canvas.restore();}
    }
    private static Bitmap captionBitmap(EditorProject.Clip c,int width,int height){String value=EditTiming.wrapCaption(c.text,Math.max(8,(int)(1080*.85/c.textSize)));TextPaint paint=new TextPaint(Paint.ANTI_ALIAS_FLAG);paint.setColor(c.textColor);int lines=value.split("\n",-1).length;paint.setTextSize(Math.max(8,Math.min(Math.round(c.textSize*width/1080f),Math.round(height*.45f/lines/1.2f))));SpannableString text=new SpannableString(value);text.setSpan(new BackgroundColorSpan(0xbb000000),0,text.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);int w=Math.max(1,(int)Math.ceil(Layout.getDesiredWidth(text,paint)));StaticLayout layout=StaticLayout.Builder.obtain(text,0,text.length(),paint,w).setAlignment(Layout.Alignment.ALIGN_CENTER).setIncludePad(true).build();Bitmap raw=Bitmap.createBitmap(w,Math.max(1,layout.getHeight()),Bitmap.Config.ARGB_8888);layout.draw(new Canvas(raw));int left=(int)(w*c.cropX),top=(int)(raw.getHeight()*c.cropY);Bitmap result=Bitmap.createBitmap(raw,left,top,Math.max(1,w-left*2),Math.max(1,raw.getHeight()-top*2));if(c.mask==0)return result;result=result.copy(Bitmap.Config.ARGB_8888,true);int rw=result.getWidth(),rh=result.getHeight();int[] pixels=new int[rw*rh];result.getPixels(pixels,0,rw,0,0,rw,rh);for(int py=0;py<rh;py++)for(int px=0;px<rw;px++){double x=(px+.5)*2/rw-1,y=(py+.5)*2/rh-1,d=c.mask==1?Math.sqrt(x*x+y*y)-.85:c.mask==2?Math.max(Math.abs(x)-.82,Math.abs(y)-.82):y,f=Math.max(.001,c.feather),t=Math.max(0,Math.min(1,(d+f)/(2*f)));int i=py*rw+px;pixels[i]=(pixels[i]&0xffffff)|((int)((pixels[i]>>>24)*(1-t*t*(3-2*t)))<<24);}result.setPixels(pixels,0,rw,0,0,rw,rh);return result;}
}

