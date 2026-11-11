package studio.solocamera;

import android.content.*;
import org.json.*;
import java.util.*;

/** Small, device-local creator preferences. Project styles are always copied. */
final class EditorCreatorStore {
 private final android.content.SharedPreferences prefs;
 EditorCreatorStore(Context c){prefs=c.getSharedPreferences("creator-library",Context.MODE_PRIVATE);}
 JSONObject brand(){try{return new JSONObject(prefs.getString("brand","{}"));}catch(JSONException e){return new JSONObject();}}
 void brand(JSONObject value){prefs.edit().putString("brand",value.toString()).apply();}
 boolean branded(){return brand().optBoolean("enabled");}
 void applyBrand(EditorProject.Clip c){applyBrand(c,false);}
 void applyBrand(EditorProject.Clip c,boolean force){JSONObject b=brand();if(!force&&!b.optBoolean("enabled"))return;c.font=b.optString("font","font:inter");c.designAccent=b.optInt("accent",0xffffc857);c.textColor=b.optInt("textColor",-1);c.textBackground=b.optInt("background",0xff121e2b);c.designLogo=b.optString("logo","");if(c.titleDesign>0){int id=c.titleDesign-1;if(EditorTitlePresets.group(id)==1){c.text=b.optString("name",c.text);c.designSubtitle=b.optString("role",c.designSubtitle);}else if(EditorSocialTitles.social(id))c.designSubtitle=b.optString("handle",c.designSubtitle);}}
 JSONArray saved(){try{return new JSONArray(prefs.getString("saved","[]"));}catch(JSONException e){return new JSONArray();}}
 String save(String name,EditorProject.Clip clip)throws JSONException{JSONArray all=saved();if(all.length()>=100)throw new IllegalArgumentException("Keep up to 100 saved designs; remove one first");String id="saved:"+UUID.randomUUID();all.put(new JSONObject().put("id",id).put("name",name).put("clip",clip.json()));prefs.edit().putString("saved",all.toString()).apply();return id;}
 void remove(String id){JSONArray out=new JSONArray(),all=saved();for(int i=0;i<all.length();i++)if(!id.equals(all.optJSONObject(i).optString("id")))out.put(all.opt(i));prefs.edit().putString("saved",out.toString()).apply();favorite(id,false);}
 boolean favorite(String id){return prefs.getStringSet("favorites",Collections.emptySet()).contains(id);}
 void favorite(String id,boolean on){Set<String> ids=new HashSet<>(prefs.getStringSet("favorites",Collections.emptySet()));if(on)ids.add(id);else ids.remove(id);prefs.edit().putStringSet("favorites",ids).apply();}
 List<String> recent(){try{JSONArray a=new JSONArray(prefs.getString("recent","[]"));List<String> out=new ArrayList<>();for(int i=0;i<a.length();i++)out.add(a.getString(i));return out;}catch(JSONException e){return Collections.emptyList();}}
 void used(String id){List<String> ids=new ArrayList<>(recent());ids.remove(id);ids.add(0,id);while(ids.size()>20)ids.remove(ids.size()-1);prefs.edit().putString("recent",new JSONArray(ids).toString()).apply();}
}
