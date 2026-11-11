package studio.solocamera;

import android.content.Context;
import android.util.AtomicFile;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Local authoring drafts only. A draft is never shown as scheduled until the service confirms it. */
final class PublishDraftStore {
    static final class Draft {
        String id=UUID.randomUUID().toString(),mediaUri="",mediaName="",caption="",state="draft",serverId="",error="";
        long scheduledAt;
        final LinkedHashSet<String> platforms=new LinkedHashSet<>();
        JSONObject channelOptions=new JSONObject();
        JSONObject json()throws JSONException{JSONArray p=new JSONArray();for(String x:platforms)p.put(x);return new JSONObject().put("id",id).put("mediaUri",mediaUri).put("mediaName",mediaName).put("caption",caption).put("state",state).put("serverId",serverId).put("error",error).put("scheduledAt",scheduledAt).put("platforms",p).put("channelOptions",new JSONObject(channelOptions.toString()));}
        static Draft from(JSONObject j)throws JSONException{Draft d=new Draft();d.id=j.optString("id",UUID.randomUUID().toString());d.mediaUri=j.optString("mediaUri");d.mediaName=j.optString("mediaName");d.caption=j.optString("caption");d.state=j.optString("state","draft");d.serverId=j.optString("serverId");d.error=j.optString("error");d.scheduledAt=Math.max(0,j.optLong("scheduledAt"));JSONArray p=j.optJSONArray("platforms");if(p!=null)for(int i=0;i<p.length();i++){String value=p.optString(i);if(Arrays.asList("facebook","linkedin","tiktok","youtube").contains(value))d.platforms.add(value);}JSONObject options=j.optJSONObject("channelOptions");if(options!=null)d.channelOptions=new JSONObject(options.toString());return d;}
    }
    private static File file(Context c){File dir=new File(c.getFilesDir(),"publishing");if(!dir.exists()&&!dir.mkdirs())throw new IllegalStateException("Cannot create publishing storage");return new File(dir,"drafts.json");}
    static synchronized ArrayList<Draft> load(Context context){ArrayList<Draft> result=new ArrayList<>();AtomicFile atomic=new AtomicFile(file(context));if(!atomic.getBaseFile().exists())return result;try(FileInputStream in=atomic.openRead()){ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1){if(bytes.size()+n>2_000_000)throw new IOException("Publishing draft data is too large");bytes.write(buffer,0,n);}JSONArray items=new JSONArray(bytes.toString(StandardCharsets.UTF_8.name()));for(int i=0;i<items.length();i++)result.add(Draft.from(items.getJSONObject(i)));}catch(Exception ignored){}return result;}
    static synchronized void save(Context context,Collection<Draft> drafts)throws Exception{JSONArray items=new JSONArray();for(Draft d:drafts)items.put(d.json());AtomicFile atomic=new AtomicFile(file(context));FileOutputStream out=null;try{out=atomic.startWrite();out.write(items.toString().getBytes(StandardCharsets.UTF_8));atomic.finishWrite(out);}catch(Exception e){if(out!=null)atomic.failWrite(out);throw e;}}
}
