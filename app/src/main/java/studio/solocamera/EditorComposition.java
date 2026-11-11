package studio.solocamera;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.*;
import android.util.Pair;
import androidx.media3.common.*;
import androidx.media3.common.audio.*;
import androidx.media3.effect.*;
import androidx.media3.transformer.*;
import java.util.*;

@androidx.annotation.OptIn(markerClass=androidx.media3.common.util.UnstableApi.class)
final class EditorComposition {
    static Composition audio(EditorProject project){
        ArrayList<EditedMediaItemSequence> sequences=new ArrayList<>();EditedMediaItemSequence.Builder main=new EditedMediaItemSequence.Builder().experimentalSetForceAudioTrack(true);long cursor=0;
        for(int i=0;i<project.clips.size();i++){EditorProject.Clip clip=project.clips.get(i);long at=project.offset(i);if(at>cursor)main.addGap((at-cursor)*1000);EditedMediaItem source=item(clip,project,at);main.addItem(source.buildUpon().setRemoveVideo(true).setEffects(new Effects(source.effects.audioProcessors,Collections.emptyList())).build());cursor=at+clip.length();}
        if(project.duration()>cursor)main.addGap((project.duration()-cursor)*1000);sequences.add(main.build());
        for(EditorProject.Audio a:project.audios){EditedMediaItemSequence.Builder track=new EditedMediaItemSequence.Builder().experimentalSetForceAudioTrack(true);if(a.at>0)track.addGap(a.at*1000);MediaItem media=new MediaItem.Builder().setUri(a.uri).setClippingConfiguration(new MediaItem.ClippingConfiguration.Builder().setStartPositionMs(a.start).setEndPositionMs(a.end).build()).build();ArrayList<AudioProcessor> processors=new ArrayList<>();processors.add(new GainAudioProcessor((float)a.volume));if(a.duck)processors.add(new DuckingAudioProcessor(project,a.at));if(a.pitch!=1){SonicAudioProcessor pitch=new SonicAudioProcessor();pitch.setPitch((float)a.pitch);processors.add(pitch);}if(a.voiceEnhance)processors.add(new VoiceEnhancementAudioProcessor(a.voiceStrength));track.addItem(new EditedMediaItem.Builder(media).setDurationUs(a.duration*1000).setRemoveVideo(true).setEffects(new Effects(processors,Collections.emptyList())).build());sequences.add(track.build());}
        sequences.addAll(EditorTitleSounds.sequences(project));return new Composition.Builder(sequences).build();
    }
    static Composition build(EditorProject project){
        ArrayList<EditedMediaItem> items=new ArrayList<>();int[] size=size(project);
        long cursor=0;for(int i=0;i<project.clips.size();i++){EditorProject.Clip clip=project.clips.get(i);long at=project.offset(i);addBlack(items,clip,at-cursor,size);items.add(item(clip,project,at));cursor=at+clip.length();}
        EditedMediaItemSequence.Builder mainSequence=new EditedMediaItemSequence.Builder().experimentalSetForceAudioTrack(true);for(EditedMediaItem item:items)mainSequence.addItem(item);
        // CompositionPlayer cannot reliably seek into a trailing generated video gap.
        // Use bounded, real decoded frames painted black instead; overlays/text continue.
        long tail=project.duration()-project.videoDuration();EditorProject.Clip last=project.referenceClip();
        if(project.clips.isEmpty()){MediaItem blackImage=new MediaItem.Builder().setUri("asset:///editor-black.png").setImageDurationMs(Math.max(200,project.duration())).build();mainSequence.addItem(new EditedMediaItem.Builder(blackImage).setDurationUs(Math.max(200,project.duration())*1000).setFrameRate(30).setRemoveAudio(true).setEffects(new Effects(Collections.emptyList(),Collections.singletonList(Presentation.createForWidthAndHeight(size[0],size[1],Presentation.LAYOUT_SCALE_TO_FIT)))).build());tail=0;}
        while(tail>0){long chunk=Math.min(tail,last.duration);MediaItem pad=new MediaItem.Builder().setUri(last.uri).setClippingConfiguration(new MediaItem.ClippingConfiguration.Builder().setEndPositionMs(chunk).build()).build();ArrayList<Effect> black=new ArrayList<>();black.add((RgbMatrix)(time,hdr)->new float[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1});black.add(Presentation.createForWidthAndHeight(size[0],size[1],Presentation.LAYOUT_SCALE_TO_FIT));mainSequence.addItem(new EditedMediaItem.Builder(pad).setDurationUs(last.duration*1000).setRemoveAudio(true).setEffects(new Effects(Collections.emptyList(),black)).build());tail-=chunk;}
        EditedMediaItemSequence sequence=mainSequence.build();
        ArrayList<EditedMediaItemSequence> sequences=new ArrayList<>();
        ArrayList<ArrayList<EditorProject.Overlay>> visible=new ArrayList<>();
        project.normalizeTracks();
        // One decoder input per occupied track, rather than per individual clip.
        for(int row=project.videoTracks()-1;row>=0;row--){ArrayList<EditorProject.Overlay> track=new ArrayList<>();for(EditorProject.Overlay o:project.overlays)if(o.track==row&&o.at<project.duration())track.add(o);if(track.isEmpty())continue;track.sort((a,b)->Long.compare(a.at,b.at));visible.add(track);
            ArrayList<EditedMediaItem> layerItems=new ArrayList<>();long layerAt=0;
            for(EditorProject.Overlay o:track){addBlack(layerItems,o.clip,Math.max(0,o.at-layerAt),size);EditorProject.Clip c=o.clip.copy();if(o.at<layerAt)c.start+=Math.round((layerAt-o.at)*c.speed);if(c.start>=c.end)continue;layerItems.add(item(c,project,o.at).buildUpon().setRemoveAudio(true).build());layerAt=Math.max(layerAt,o.at)+c.length();}
            addBlack(layerItems,track.get(0).clip,project.duration()-layerAt,size);EditedMediaItemSequence.Builder builder=new EditedMediaItemSequence.Builder();for(EditedMediaItem item:layerItems)builder.addItem(item);sequences.add(builder.build());
        }
        sequences.add(sequence);
        if(!project.music.isEmpty()&&project.musicAt<project.duration()){
            long at=project.musicAt,end=Math.min(project.duration(),project.musicEnd);EditedMediaItemSequence.Builder musicSequence=new EditedMediaItemSequence.Builder().experimentalSetForceAudioTrack(true);if(at>0)musicSequence.addGap(at*1000);
            while(at<end){long length=Math.min(projectMusicDuration(project),end-at);MediaItem source=new MediaItem.Builder().setUri(project.music).setClippingConfiguration(new MediaItem.ClippingConfiguration.Builder().setEndPositionMs(length).build()).build();musicSequence.addItem(new EditedMediaItem.Builder(source).setDurationUs(projectMusicDuration(project)*1000).setRemoveVideo(true).setEffects(new Effects(Collections.singletonList(new GainAudioProcessor((float)project.musicVolume)),Collections.emptyList())).build());at+=length;}
            if(end>project.musicAt)sequences.add(musicSequence.build());
        }

        for(EditorProject.Audio a:project.audios){EditedMediaItemSequence.Builder sound=new EditedMediaItemSequence.Builder().experimentalSetForceAudioTrack(true);if(a.at>0)sound.addGap(a.at*1000);MediaItem media=new MediaItem.Builder().setUri(a.uri).setClippingConfiguration(new MediaItem.ClippingConfiguration.Builder().setStartPositionMs(a.start).setEndPositionMs(a.end).build()).build();ArrayList<AudioProcessor> effects=new ArrayList<>();effects.add(new GainAudioProcessor((float)a.volume));if(a.duck)effects.add(new DuckingAudioProcessor(project,a.at));if(a.pitch!=1){SonicAudioProcessor pitch=new SonicAudioProcessor();pitch.setPitch((float)a.pitch);effects.add(pitch);}if(a.voiceEnhance)effects.add(new VoiceEnhancementAudioProcessor(a.voiceStrength));sound.addItem(new EditedMediaItem.Builder(media).setDurationUs(a.duration*1000).setRemoveVideo(true).setEffects(new Effects(effects,Collections.emptyList())).build());sequences.add(sound.build());}
        sequences.addAll(EditorTitleSounds.sequences(project));Composition.Builder result=new Composition.Builder(sequences).setEffects(globalEffects(project,size));
        if(!visible.isEmpty())result.setVideoCompositorSettings(new VideoCompositorSettings(){
            public androidx.media3.common.util.Size getOutputSize(List<androidx.media3.common.util.Size> inputs){return new androidx.media3.common.util.Size(size[0],size[1]);}
            public OverlaySettings getOverlaySettings(int id,long timeUs){if(id>=visible.size())return new StaticOverlaySettings.Builder().build();EditorProject.Overlay o=null;for(EditorProject.Overlay candidate:visible.get(id))if(timeUs>=candidate.at*1000&&timeUs<(candidate.at+candidate.clip.length())*1000){o=candidate;break;}if(o==null)return new StaticOverlaySettings.Builder().setAlphaScale(0).build();boolean showing=true;return new StaticOverlaySettings.Builder().setBackgroundFrameAnchor((float)o.x,(float)o.y).setScale((float)o.scale,(float)o.scale).setAlphaScale(showing?(float)o.alpha:0).build();}
        });
        return result.setHdrMode(Composition.HDR_MODE_TONE_MAP_HDR_TO_SDR_USING_OPEN_GL).build();
    }
    private static void addBlack(List<EditedMediaItem> items,EditorProject.Clip source,long duration,int[] size){
        while(duration>0){long chunk=Math.min(duration,source.duration);MediaItem pad=new MediaItem.Builder().setUri(source.uri).setClippingConfiguration(new MediaItem.ClippingConfiguration.Builder().setEndPositionMs(chunk).build()).build();ArrayList<Effect> effects=new ArrayList<>();effects.add((RgbMatrix)(time,hdr)->new float[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1});effects.add(Presentation.createForWidthAndHeight(size[0],size[1],Presentation.LAYOUT_SCALE_TO_FIT));items.add(new EditedMediaItem.Builder(pad).setDurationUs(source.duration*1000).setRemoveAudio(true).setEffects(new Effects(Collections.emptyList(),effects)).build());duration-=chunk;}
    }
    private static Effects globalEffects(EditorProject project,int[] size){ArrayList<Effect> effects=new ArrayList<>();effects.add(Presentation.createForWidthAndHeight(size[0],size[1],Presentation.LAYOUT_SCALE_TO_FIT));for(EditorProject.EffectClip fx:project.effects){final int preset=fx.preset;final long start=fx.at*1000,end=fx.end*1000;final float strength=(float)fx.intensity;effects.add((RgbMatrix)(time,hdr)->{float amount=time<start||time>=end?0:strength;float contrast=1,red=1,green=1,blue=1,biasR=0,biasG=0,biasB=0,sat=1;switch(preset){case 1:contrast=1+.18f*amount;red=1+.04f*amount;blue=1-.05f*amount;break;case 2:red=1+.07f*amount;green=1+.025f*amount;blue=1-.06f*amount;break;case 3:red=1+.12f*amount;blue=1-.1f*amount;break;case 4:blue=1+.12f*amount;red=1-.08f*amount;break;case 5:contrast=1-.2f*amount;biasR=biasG=biasB=.025f*amount;break;case 6:red=1+.12f*amount;green=1+.03f*amount;blue=1-.12f*amount;break;case 7:contrast=1+.22f*amount;break;case 8:contrast=1-.16f*amount;sat=1-.15f*amount;break;case 9:sat=1-.5f*amount;break;case 10:red=1+.06f*amount;blue=1-.06f*amount;break;case 11:sat=0;break;default:break;}float lr=.213f,lg=.715f,lb=.072f;return new float[]{red*contrast*(sat+(1-sat)*lr),green*contrast*((1-sat)*lg),blue*contrast*((1-sat)*lb),0,red*contrast*((1-sat)*lr),green*contrast*(sat+(1-sat)*lg),blue*contrast*((1-sat)*lb),0,red*contrast*((1-sat)*lr),green*contrast*((1-sat)*lg),blue*contrast*(sat+(1-sat)*lb),0,0,0,0,1};});}ArrayList<TextureOverlay> overlays=new ArrayList<>();
        for(EditorProject.Caption caption:EditorPrivacy.ordered(project)){
                SpannableString text=new SpannableString(EditTiming.wrapCaption(caption.style.text,Math.max(8,(int)(1080*.85/caption.style.textSize))));text.setSpan(new ForegroundColorSpan(caption.style.textColor),0,text.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);text.setSpan(new BackgroundColorSpan(0xbb000000),0,text.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);int lines=1;for(int i=0;i<text.length();i++)if(text.charAt(i)=='\n')lines++;int font=Math.max(8,Math.min(Math.round(caption.style.textSize*size[0]/1080f),Math.round(size[1]*.45f/lines/1.2f)));text.setSpan(new AbsoluteSizeSpan(font),0,text.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            float y=caption.style.textPlaced?(float)caption.style.textY:caption.style.textPosition==0?.7f:caption.style.textPosition==2?-.7f:0;
            overlays.add(new TextOverlay(){android.graphics.Bitmap shaped;EditorTitleAnimation titleAnimation;EditorCaptionStyles.Animation captionAnimation;int revealFrame=-1;android.graphics.Bitmap revealBitmap;
                @Override public android.graphics.Bitmap getBitmap(long timeUs){if(caption.style.captionStyle>0&&caption.style.titleDesign==0&&caption.style.imageUri.isEmpty()){long local=timeUs/1000-caption.at;if(local<0||local>=caption.end-caption.at){if(shaped==null)shaped=android.graphics.Bitmap.createBitmap(1,1,android.graphics.Bitmap.Config.ARGB_8888);return shaped;}if(captionAnimation==null)captionAnimation=new EditorCaptionStyles.Animation(caption.style,size[0],size[1]);return captionAnimation.frame(timeUs/1000-caption.at,caption.end-caption.at);}if(caption.style.titleDesign>0&&caption.style.titleMotion>0){long local=timeUs/1000-caption.at;if(local<0||local>=caption.end-caption.at){if(shaped==null)shaped=android.graphics.Bitmap.createBitmap(1,1,android.graphics.Bitmap.Config.ARGB_8888);return shaped;}if(titleAnimation==null)titleAnimation=new EditorTitleAnimation(caption.style,size[0],size[1]);return titleAnimation.frame(timeUs/1000-caption.at,caption.end-caption.at);}if(shaped==null)shaped=EditorText.bitmap(caption.style,size[0],size[1]);if(caption.style.textReveal==0)return shaped;float p=Math.max(0,Math.min(1,(timeUs-caption.at*1000f)/Math.max(1,(caption.end-caption.at)*1000f)));int frame=Math.round(p*30);if(frame!=revealFrame){if(revealBitmap!=null&&revealBitmap!=shaped)revealBitmap.recycle();revealBitmap=EditorTextReveal.frame(shaped,caption.style.textReveal,frame/30f);revealFrame=frame;}return revealBitmap;}
                @Override public void release() throws androidx.media3.common.VideoFrameProcessingException{super.release();captionAnimation=null;titleAnimation=null;revealBitmap=null;shaped=null;}
                public SpannableString getText(long timeUs){return text;}public OverlaySettings getOverlaySettings(long timeUs){EditorMotion.Key motion=EditorMotion.sample(caption.style,timeUs/1000-caption.at,true,caption.end-caption.at);float progress=Math.max(0,Math.min(1,(timeUs-caption.at*1000)/(float)((caption.end-caption.at)*1000))),scale=caption.style.animation==1?1+.2f*progress:caption.style.animation==2?1.2f-.2f*progress:1,x=(float)motion.x+(caption.style.animation==3?.4f*(progress-.5f):0);float alpha=timeUs>=caption.at*1000&&timeUs<caption.end*1000?1:0;if(caption.style.fade>0&&!caption.style.privacyBlackout){long fade=Math.min(caption.style.fade*1000,(caption.end-caption.at)*500);alpha*=Math.max(0,Math.min(1,Math.min((timeUs-caption.at*1000)/(float)fade,(caption.end*1000-timeUs)/(float)fade)));}return new StaticOverlaySettings.Builder().setBackgroundFrameAnchor(caption.style.privacyBlackout?x:Math.max(-.9f,Math.min(.9f,x)),(float)motion.y).setScale(scale*(float)(motion.scale*caption.style.stretchX)*(caption.style.mirror?-1:1),scale*(float)(motion.scale*caption.style.stretchY)).setRotationDegrees((float)(caption.style.rotation*90+motion.rotation)).setAlphaScale((float)(alpha*(caption.style.privacyBlackout?1:motion.alpha*caption.style.opacity))).build();}});
        }
        if(!overlays.isEmpty())effects.add(new OverlayEffect(overlays));return new Effects(Collections.emptyList(),effects);
    }
    static EditedMediaItem item(EditorProject.Clip clip,EditorProject project){return item(clip,project,0);}
    static EditedMediaItem item(EditorProject.Clip clip,EditorProject project,long clipAtMs){int[] size=size(project);
            MediaItem media=new MediaItem.Builder().setUri(clip.uri).setClippingConfiguration(new MediaItem.ClippingConfiguration.Builder().setStartPositionMs(clip.start).setEndPositionMs(clip.end).build()).build();
            ArrayList<AudioProcessor> audio=new ArrayList<>();if(clip.audioSyncMs!=0)audio.add(new SyncAudioProcessor(clip.audioSyncMs));ArrayList<Effect> video=new ArrayList<>();
            // Constant speed must not depend on audio callbacks: silent clips and sequence boundaries can stall that experimental path.
            if(clip.speed!=1){SonicAudioProcessor rate=new SonicAudioProcessor();rate.setSpeed((float)clip.speed);audio.add(rate);video.add(new SpeedChangeEffect((float)clip.speed));}
            if(clip.pitch!=1){SonicAudioProcessor voice=new SonicAudioProcessor();voice.setPitch((float)clip.pitch);audio.add(voice);}if(clip.voiceEnhance)audio.add(new VoiceEnhancementAudioProcessor(clip.voiceStrength));
            if(clip.volume!=1)audio.add(new GainAudioProcessor((float)clip.volume));
            if(clip.rotation!=0||clip.mirror)video.add(new ScaleAndRotateTransformation.Builder().setRotationDegrees(clip.rotation*90).setScale(clip.mirror?-1:1,1).build());
            if(clip.fade>0){final long length=clip.length()*1000,fade=Math.min(clip.fade*1000,length/2),origin=clipAtMs*1000;video.add((RgbMatrix)(time,hdr)->{long local=Math.max(0,time-origin);float gain=Math.max(0,Math.min(1,Math.min(local/(float)fade,(length-local)/(float)fade)));return new float[]{gain,0,0,0,0,gain,0,0,0,0,gain,0,0,0,0,1};});}
            double amount=clip.intensity;
            switch(clip.look){
                case 1:video.add(new Contrast((float)(.18*amount)));video.add(new RgbAdjustment.Builder().setRedScale((float)(1+.04*amount)).setBlueScale((float)(1-.05*amount)).build());break;
                case 2:video.add(new Brightness((float)(.06*amount)));video.add(new HslAdjustment.Builder().adjustSaturation((float)(10*amount)).build());break;
                case 3:video.add(new RgbAdjustment.Builder().setRedScale((float)(1+.12*amount)).setBlueScale((float)(1-.10*amount)).build());break;
                case 4:video.add(new RgbAdjustment.Builder().setBlueScale((float)(1+.12*amount)).setRedScale((float)(1-.08*amount)).build());break;
                case 5:video.add(new Contrast((float)(-.25*amount)));video.add(new Brightness((float)(.04*amount)));break;
                case 6:video.add(new RgbAdjustment.Builder().setRedScale((float)(1+.12*amount)).setBlueScale((float)(1-.12*amount)).build());video.add(new Brightness((float)(.04*amount)));break;
                case 7:video.add(new RgbAdjustment.Builder().setRedScale((float)(1+.16*amount)).setGreenScale((float)(1+.02*amount)).setBlueScale((float)(1-.15*amount)).build());video.add(new Contrast((float)(.08*amount)));break;
                case 8:video.add(new Contrast((float)(.22*amount)));video.add(new Brightness((float)(-.04*amount)));break;
                case 9:video.add(new Contrast((float)(-.18*amount)));video.add(new HslAdjustment.Builder().adjustSaturation((float)(-12*amount)).build());break;
                case 10:video.add(new Contrast((float)(.16*amount)));video.add(new HslAdjustment.Builder().adjustSaturation((float)(-100*amount)).build());break;
                case 11:video.add(new Contrast((float)(-.12*amount)));video.add(new RgbAdjustment.Builder().setRedScale((float)(1+.07*amount)).setBlueScale((float)(1-.07*amount)).build());break;
                default:break;
            }
            if(clip.curveStrength>0)video.add(new EditorCurveGrade(clip));
            if(clip.brightness!=0)video.add(new Brightness((float)clip.brightness));
            if(clip.contrast!=0)video.add(new Contrast((float)clip.contrast));
            if(clip.saturation!=0)video.add(new HslAdjustment.Builder().adjustSaturation((float)clip.saturation).build());
            if(clip.cropX>0||clip.cropY>0)video.add(new Crop((float)(-1+clip.cropX*2),(float)(1-clip.cropX*2),(float)(-1+clip.cropY*2),(float)(1-clip.cropY*2)));
            if(clip.comparisonMode!=0||clip.mask!=0||(clip.transitionIn>=16&&clip.transitionIn<=19)||(clip.transitionOut>=16&&clip.transitionOut<=19))video.add(new FeatherMask(clip,project.clips.contains(clip),clipAtMs));
            video.add(Presentation.createForWidthAndHeight(size[0],size[1],project.fill?Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP:Presentation.LAYOUT_SCALE_TO_FIT));
            if(clip.transitionIn>=3||clip.transitionOut>=3||!clip.keys.isEmpty()||clip.zoom!=1||clip.motionRotation!=0||clip.panX!=0||clip.panY!=0||clip.animation!=0||clip.stretchX!=1||clip.stretchY!=1){final long length=clip.length()*1000,origin=clipAtMs*1000;video.add((MatrixTransformation)(time)->{long local=Math.max(0,time-origin);EditorMotion.Key motion=EditorMotion.sample(clip,local/1000,false);float progress=Math.max(0,Math.min(1,local/(float)length)),scale=(float)motion.scale;float x=(float)motion.x,y=(float)motion.y;if(clip.animation==1)scale*=1+.2f*progress;if(clip.animation==2)scale*=1.2f-.2f*progress;if(clip.animation==3)x+=.4f*(progress-.5f);android.graphics.Matrix matrix=new android.graphics.Matrix();EditorTransitions.State transition=EditorTransitions.sample(clip,local/1000);matrix.setScale(scale*(float)(clip.stretchX*transition.sx),scale*(float)(clip.stretchY*transition.sy));matrix.postRotate((float)motion.rotation);matrix.postTranslate(x,y);return matrix;});}
            if(clip.fadeIn>0||clip.fadeOut>0){final long origin=clipAtMs*1000;video.add((RgbMatrix)(time,hdr)->{EditorTransitions.State state=EditorTransitions.sample(clip,Math.max(0,time-origin)/1000);float gain=(float)state.gain;return new float[]{gain,0,0,0,0,gain,0,0,0,0,gain,0,(float)state.r,(float)state.g,(float)state.b,1};});}
            if(!clip.keys.isEmpty()||clip.opacity<1){final long origin=clipAtMs*1000;video.add((RgbMatrix)(time,hdr)->{float a=(float)(clip.opacity*EditorMotion.sample(clip,Math.max(0,time-origin)/1000,false).alpha);return new float[]{1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,a};});}
            return new EditedMediaItem.Builder(media).setDurationUs(clip.duration*1000).setEffects(new Effects(audio,video)).build();
    }
    // Kept in the draft so the preview need not perform blocking metadata reads.
    static long projectMusicDuration(EditorProject p){return Math.max(200,p.musicDuration);}
    static int[] size(EditorProject p){
        EditorProject.Clip first=p.referenceClip();double ratio=p.aspect==1?9.0/16:p.aspect==2?16.0/9:p.aspect==3?1:p.aspect==4?4.0/3:(double)first.width/first.height;if(p.aspect==0&&first.rotation%2!=0)ratio=1/ratio;
        ratio=Math.max(.25,Math.min(4,ratio));int width,height;if(ratio>=1){height=p.height;width=(int)Math.round(height*ratio);}else{width=p.height;height=(int)Math.round(width/ratio);}return new int[]{Math.max(2,width/2*2),Math.max(2,height/2*2)};
    }
}


