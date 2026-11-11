package studio.solocamera;

final class PresenterOrientation {
    static int degrees(int sensor,int display,boolean front){return front?(360-(sensor+display)%360)%360:(sensor-display+360)%360;}
}
