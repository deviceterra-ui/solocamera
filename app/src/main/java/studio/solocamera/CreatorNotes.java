package studio.solocamera;

import android.content.Context;
import org.json.*;

/** Small local sidecars: source timestamps survive project trim and speed changes. */
final class CreatorNotes {
    static String key(String uri){try{return android.util.Base64.encodeToString(java.security.MessageDigest.getInstance("SHA-256").digest(uri.getBytes(java.nio.charset.StandardCharsets.UTF_8)),android.util.Base64.NO_WRAP|android.util.Base64.URL_SAFE);}catch(Exception e){throw new IllegalStateException(e);}}
    static JSONObject read(Context c,String uri){try{return new JSONObject(c.getSharedPreferences("creator-takes",0).getString(key(uri),"{}"));}catch(Exception e){return new JSONObject();}}
    static void write(Context c,String uri,JSONObject notes){c.getSharedPreferences("creator-takes",0).edit().putString(key(uri),notes.toString()).apply();}
    static void marker(Context c,String uri,long time,String kind)throws JSONException{JSONObject notes=read(c,uri);JSONArray markers=notes.optJSONArray("markers");if(markers==null)markers=new JSONArray();if(markers.length()>=500)throw new IllegalStateException("Take marker limit reached");markers.put(new JSONObject().put("at",Math.max(0,time)).put("kind",kind));notes.put("markers",markers);write(c,uri,notes);}
}
