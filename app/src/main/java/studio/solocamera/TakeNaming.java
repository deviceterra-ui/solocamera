package studio.solocamera;
import java.text.Normalizer;
final class TakeNaming {
 static String safe(String raw){String text=Normalizer.normalize(raw==null?"":raw,Normalizer.Form.NFKC).replaceAll("[^\\p{L}\\p{N}_ -]"," ").trim().replaceAll("[ -]+","-");return text.length()>32?text.substring(0,32):text;}
 static String filename(String production,String scene,int take,long timestamp){String name=safe(production);if(name.isEmpty())name="take";String shot=safe(scene);return name+(shot.isEmpty()?"":"_"+shot)+String.format(java.util.Locale.US,"_T%03d_%d.mp4",Math.max(1,Math.min(99999,take)),timestamp);}
}
