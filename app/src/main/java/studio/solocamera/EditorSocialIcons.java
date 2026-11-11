package studio.solocamera;

import android.content.Context;import android.graphics.*;import org.json.*;import java.io.*;

/** Original Font Awesome 6.7.2 brand paths; never substitutes letters or emoji. */
final class EditorSocialIcons {
 private static final Path[][] paths=new Path[12][];private static final float[][] boxes=new float[12][];private static final int[] colors=new int[12];private static boolean loaded;
 static synchronized void init(Context c){if(loaded)return;try(InputStream in=c.getAssets().open("social-icons/manifest.json")){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];for(int n;(n=in.read(b))!=-1;)out.write(b,0,n);JSONArray list=new JSONArray(out.toString("UTF-8"));if(list.length()!=12)throw new IOException("Missing social icons");for(int i=0;i<12;i++){JSONObject icon=list.getJSONObject(i);JSONArray shape=icon.getJSONArray("paths"),box=icon.getJSONArray("viewBox");paths[i]=new Path[shape.length()];boxes[i]=new float[]{(float)box.getDouble(0),(float)box.getDouble(1),(float)box.getDouble(2),(float)box.getDouble(3)};colors[i]=Color.parseColor(icon.getString("color"));for(int j=0;j<shape.length();j++)paths[i][j]=androidx.core.graphics.PathParser.createPathFromPathData(shape.getString(j));}loaded=true;}catch(Exception e){throw new IllegalStateException("Social icon assets unavailable",e);}}
 static int color(int platform){return colors[platform];}
 static void draw(Canvas canvas,int platform,RectF area){if(!loaded)throw new IllegalStateException("Initialize social icons first");float[] box=boxes[platform];float scale=Math.min(area.width()/box[2],area.height()/box[3]);canvas.save();canvas.translate(area.centerX()-box[2]*scale/2,area.centerY()-box[3]*scale/2);canvas.scale(scale,scale);canvas.translate(-box[0],-box[1]);Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);paint.setColor(Color.WHITE);for(Path path:paths[platform])canvas.drawPath(path,paint);canvas.restore();}
}
