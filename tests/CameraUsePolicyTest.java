package studio.solocamera;

public final class CameraUsePolicyTest {
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        CameraUsePolicy p=new CameraUsePolicy();Object first=new Object(),scanner=new Object(),replacement=new Object();
        check(!p.occupied(),"initial ownership");p.claim(first,"camera");p.claim(first,"camera");p.claim(scanner,"scanner");p.release(first);check(p.occupied()&&p.reason().equals("scanner"),"scanner loses camera after old owner closes");p.claim(replacement,"camera");p.release(first);check(p.occupied(),"stale callback resumes presenter");p.release(scanner);check(p.occupied()&&p.reason().equals("camera"),"replacement not protected");p.release(replacement);check(!p.occupied()&&p.reason().isEmpty(),"presenter cannot resume");p.release(replacement);check(!p.occupied(),"duplicate release");
        Object equalA=new String("same"),equalB=new String("same");p.claim(equalA,"A");p.claim(equalB,"B");p.release(equalA);check(p.occupied(),"leases used equality instead of identity");p.release(equalB);
        for(int sensor:new int[]{0,90,180,270})for(int display:new int[]{0,90,180,270})for(boolean front:new boolean[]{false,true}){int angle=PresenterOrientation.degrees(sensor,display,front);check(angle>=0&&angle<360&&angle%90==0,"invalid orientation");check(front?(angle+sensor+display)%360==0:(angle-sensor+display+360)%360==0,"wrong front/back rotation");}
        check(PresenterOrientation.degrees(90,0,false)==90,"back portrait");check(PresenterOrientation.degrees(270,0,true)==90,"front portrait");
        System.out.println("PASS camera/scanner overlapping leases, idempotent close, stale close, identity semantics and 32 front/back orientation cases");
    }
}
