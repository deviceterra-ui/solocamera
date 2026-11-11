package studio.solocamera;
final class EditorMask {
 static double alpha(double x,double y,EditorProject.Clip c){double angle=Math.toRadians(c.maskRotation),dx=x-c.maskX,dy=y-c.maskY;double nx=(Math.cos(angle)*dx+Math.sin(angle)*dy)/c.maskWidth,ny=(-Math.sin(angle)*dx+Math.cos(angle)*dy)/c.maskHeight;double d=c.mask==1?Math.sqrt(nx*nx+ny*ny)-.85:c.mask==2?Math.max(Math.abs(nx)-.82,Math.abs(ny)-.82):ny,f=Math.max(.001,c.feather),t=Math.max(0,Math.min(1,(d+f)/(2*f)));return 1-t*t*(3-2*t);}
}
