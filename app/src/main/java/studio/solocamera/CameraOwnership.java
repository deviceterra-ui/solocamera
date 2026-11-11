package studio.solocamera;

import android.os.*;

/** Foreground camera/scanner takes priority over the optional screen presenter. Main looper only. */
final class CameraOwnership {
    private static final CameraUsePolicy owners=new CameraUsePolicy();
    static void claim(Object token,String reason){requireMain();owners.claim(token,reason);ScreenRecordingService.refreshPresenterOwnership();}
    static void release(Object token){if(Looper.myLooper()!=Looper.getMainLooper()){new Handler(Looper.getMainLooper()).post(()->release(token));return;}owners.release(token);ScreenRecordingService.refreshPresenterOwnership();}
    static boolean occupied(){requireMain();return owners.occupied();}
    static String reason(){requireMain();return owners.reason();}
    private static void requireMain(){if(Looper.myLooper()!=Looper.getMainLooper())throw new IllegalStateException("Camera ownership must run on the main looper");}
}
