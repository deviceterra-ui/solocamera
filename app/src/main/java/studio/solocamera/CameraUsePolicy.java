package studio.solocamera;

import java.util.IdentityHashMap;

/** Identity-based leases prevent an old activity's close callback releasing a newer camera. */
final class CameraUsePolicy {
    private final IdentityHashMap<Object,String> owners=new IdentityHashMap<>();
    void claim(Object token,String reason){if(token==null)throw new IllegalArgumentException("Camera owner required");owners.put(token,reason);}
    void release(Object token){owners.remove(token);}
    boolean occupied(){return !owners.isEmpty();}
    String reason(){return owners.isEmpty()?"":owners.values().iterator().next();}
}
