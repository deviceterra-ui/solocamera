package studio.solocamera;
import android.content.Context;import android.opengl.GLES20;import androidx.media3.common.VideoFrameProcessingException;import androidx.media3.common.util.*;import androidx.media3.effect.*;
/** Per-channel master/R/G/B point curves, applied to the exported picture. */
@androidx.annotation.OptIn(markerClass=UnstableApi.class)
final class EditorCurveGrade implements GlEffect {
 private final EditorProject.Clip c;EditorCurveGrade(EditorProject.Clip clip){c=clip;}
 public GlShaderProgram toGlShaderProgram(Context context,boolean hdr)throws VideoFrameProcessingException{return new Program(hdr);}
 private final class Program extends BaseGlShaderProgram{
  private final GlProgram program;Program(boolean hdr)throws VideoFrameProcessingException{super(hdr,1);try{String v="attribute vec4 aPosition;attribute vec4 aTexture;varying vec2 uv;void main(){gl_Position=aPosition;uv=aTexture.xy;}";String f="precision mediump float;uniform sampler2D uTexture;uniform float m0,m1,m2,m3,m4,r0,r1,r2,r3,r4,g0,g1,g2,g3,g4,b0,b1,b2,b3,b4;varying vec2 uv;float curve(float x,float a,float z,float q,float w,float e){if(x<.25)return mix(a,z,x*4.0);if(x<.5)return mix(z,q,(x-.25)*4.0);if(x<.75)return mix(q,w,(x-.5)*4.0);return mix(w,e,(x-.75)*4.0);}void main(){vec4 c=texture2D(uTexture,uv);vec3 v=vec3(curve(c.r,m0,m1,m2,m3,m4),curve(c.g,m0,m1,m2,m3,m4),curve(c.b,m0,m1,m2,m3,m4));v=vec3(curve(v.r,r0,r1,r2,r3,r4),curve(v.g,g0,g1,g2,g3,g4),curve(v.b,b0,b1,b2,b3,b4));gl_FragColor=vec4(mix(c.rgb,v,uStrength),c.a);}";f=f.replace("uniform float m0","uniform float uStrength;uniform float m0");program=new GlProgram(v,f);program.setBufferAttribute("aPosition",GlUtil.getNormalizedCoordinateBounds(),4);program.setBufferAttribute("aTexture",GlUtil.getTextureCoordinateBounds(),4);}catch(GlUtil.GlException e){throw new VideoFrameProcessingException(e);}}
  public Size configure(int width,int height){return new Size(width,height);}public void drawFrame(int texture,long time)throws VideoFrameProcessingException{try{program.use();program.setSamplerTexIdUniform("uTexture",texture,0);uniforms("m",c.curveMaster);uniforms("r",c.curveRed);uniforms("g",c.curveGreen);uniforms("b",c.curveBlue);program.setFloatUniform("uStrength",(float)c.curveStrength);program.bindAttributesAndUniforms();GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);GlUtil.checkGlError();}catch(GlUtil.GlException e){throw new VideoFrameProcessingException(e,time);}}
  void uniforms(String prefix,double[] values)throws GlUtil.GlException{for(int i=0;i<5;i++)program.setFloatUniform(prefix+i,(float)values[i]);}
  @Override public void release()throws VideoFrameProcessingException{super.release();try{program.delete();}catch(GlUtil.GlException e){throw new VideoFrameProcessingException(e);}}
 }
}
