package studio.solocamera;

/** Screen-safe placement, retaining the circle position while fullscreen is toggled. */
final class PresenterGeometry {
    static int[] bounds(int width,int height,int top,int bottom,int diameter,float x,float y,boolean fullscreen){
        if(width<=0||height<=0)throw new IllegalArgumentException("Invalid screen dimensions");
        top=Math.max(0,Math.min(height-1,top));bottom=Math.max(0,Math.min(height-top-1,bottom));
        int available=height-top-bottom;
        if(fullscreen)return new int[]{0,top,width,available};
        int size=Math.max(1,Math.min(Math.min(width,available),diameter));
        x=Float.isFinite(x)?Math.max(0,Math.min(1,x)):.9f;y=Float.isFinite(y)?Math.max(0,Math.min(1,y)):.85f;
        return new int[]{Math.round((width-size)*x),top+Math.round((available-size)*y),size,size};
    }
}
