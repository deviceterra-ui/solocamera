package studio.solocamera;

import android.content.Context;
import android.opengl.GLES20;
import androidx.media3.common.VideoFrameProcessingException;
import androidx.media3.common.util.*;
import androidx.media3.effect.*;

/** Geometric alpha mask in texture coordinates, shared by preview and export. */
@androidx.annotation.OptIn(markerClass=UnstableApi.class)
final class FeatherMask implements GlEffect {
    private final boolean opaque;private final int shape;private final float feather;private final EditorProject.Clip settings;private final long timelineOffsetUs;
    FeatherMask(EditorProject.Clip c,boolean opaque){this(c,opaque,0);}
    FeatherMask(EditorProject.Clip c,boolean opaque,long timelineOffsetMs){this.opaque=opaque;settings=c;shape=c.mask;feather=(float)c.feather;timelineOffsetUs=timelineOffsetMs*1000;}
    public GlShaderProgram toGlShaderProgram(Context context,boolean hdr)throws VideoFrameProcessingException{return new Program(hdr);}
    private final class Program extends BaseGlShaderProgram {
        private final GlProgram program;private float aspect=1;
        Program(boolean hdr)throws VideoFrameProcessingException{super(hdr,1);try{
            program=new GlProgram("attribute vec4 aPosition; attribute vec4 aTexture; varying vec2 uv; void main(){gl_Position=aPosition;uv=aTexture.xy;}",
                "precision mediump float; uniform sampler2D uTexture; uniform float uFeather; uniform int uShape,uOpaque,uTransition;uniform float uOpen,uAspect,uCompareSplit;uniform int uCompare;uniform float uX,uY,uW,uH,uAngle; varying vec2 uv; void main(){vec4 color=texture2D(uTexture,uv); vec2 q=vec2(uv.x,1.0-uv.y)*2.0-1.0-vec2(uX,uY);float cs=cos(uAngle),sn=sin(uAngle);vec2 p=vec2(cs*q.x+sn*q.y,-sn*q.x+cs*q.y)/vec2(uW,uH); float distance; if(uShape==1){distance=length(p)-0.85;}else if(uShape==2){distance=max(abs(p.x)-0.82,abs(p.y)-0.82);}else{distance=p.y;} float edge=max(0.001,uFeather);float alpha=uShape==0?1.0:1.0-smoothstep(-edge,edge,distance);vec2 v=vec2(uv.x,1.0-uv.y);if(uCompare>0){float axis=uCompare==2?v.y:v.x;alpha=1.0-smoothstep(uCompareSplit-0.003,uCompareSplit+0.003,axis);}if(uTransition==1){vec2 d=(v-0.5)*vec2(uAspect,1.0);alpha*=1.0-step(length(vec2(uAspect,1.0))*0.5*uOpen,length(d));}else if(uTransition==2){alpha*=1.0-step(uOpen,v.x);}else if(uTransition==3){alpha*=step(1.0-uOpen,v.x);}else if(uTransition==4){alpha*=1.0-step(0.5*uOpen,abs(v.y-0.5));}gl_FragColor=uOpaque==1?vec4(color.rgb*alpha,1.0):vec4(color.rgb,color.a*alpha);}");
            program.setBufferAttribute("aPosition",GlUtil.getNormalizedCoordinateBounds(),4);program.setBufferAttribute("aTexture",GlUtil.getTextureCoordinateBounds(),4);
        }catch(GlUtil.GlException e){throw new VideoFrameProcessingException(e);}}
        public Size configure(int width,int height){aspect=(float)width/height;return new Size(width,height);}
        public void drawFrame(int texture,long time)throws VideoFrameProcessingException{try{program.use();program.setSamplerTexIdUniform("uTexture",texture,0);EditorTransitions.State state=EditorTransitions.sample(settings,Math.max(0,time-timelineOffsetUs)/1000);program.setIntUniform("uTransition",state.mask);program.setFloatUniform("uOpen",(float)state.open);program.setFloatUniform("uAspect",aspect);program.setIntUniform("uCompare",settings.comparisonMode);program.setFloatUniform("uCompareSplit",(float)EditorCompare.split(settings,Math.max(0,time-timelineOffsetUs)/1000));program.setIntUniform("uShape",shape);program.setIntUniform("uOpaque",opaque?1:0);program.setFloatUniform("uFeather",feather);program.setFloatUniform("uX",(float)settings.maskX);program.setFloatUniform("uY",(float)settings.maskY);program.setFloatUniform("uW",(float)settings.maskWidth);program.setFloatUniform("uH",(float)settings.maskHeight);program.setFloatUniform("uAngle",(float)Math.toRadians(settings.maskRotation));program.bindAttributesAndUniforms();GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);GlUtil.checkGlError();}catch(GlUtil.GlException e){throw new VideoFrameProcessingException(e,time);}}
        @Override public void release()throws VideoFrameProcessingException{super.release();try{program.delete();}catch(GlUtil.GlException e){throw new VideoFrameProcessingException(e);}}
    }
}
