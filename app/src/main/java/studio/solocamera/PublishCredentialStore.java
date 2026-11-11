package studio.solocamera;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** Keeps the short-lived first-party service session encrypted with an Android Keystore key. */
final class PublishCredentialStore {
    private static final String ALIAS="solo_camera_publish_session_v1";
    private static final String PREFS="publishing_session";
    private PublishCredentialStore(){}
    static void save(Context context,String token)throws Exception{if(token==null||token.trim().isEmpty())throw new IllegalArgumentException("Empty session token");Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,key());byte[] encrypted=cipher.doFinal(token.getBytes(StandardCharsets.UTF_8));context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putString("iv",Base64.encodeToString(cipher.getIV(),Base64.NO_WRAP)).putString("value",Base64.encodeToString(encrypted,Base64.NO_WRAP)).apply();}
    static String load(Context context){try{String ivText=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).getString("iv",null),value=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).getString("value",null);if(ivText==null||value==null)return"";Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,Base64.decode(ivText,Base64.NO_WRAP)));return new String(cipher.doFinal(Base64.decode(value,Base64.NO_WRAP)),StandardCharsets.UTF_8);}catch(Exception ignored){clear(context);return"";}}
    static void clear(Context context){context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().clear().apply();}
    private static SecretKey key()throws Exception{KeyStore store=KeyStore.getInstance("AndroidKeyStore");store.load(null);java.security.Key existing=store.getKey(ALIAS,null);if(existing instanceof SecretKey)return(SecretKey)existing;KeyGenerator generator=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");generator.init(new KeyGenParameterSpec.Builder(ALIAS,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setRandomizedEncryptionRequired(true).build());return generator.generateKey();}
}
