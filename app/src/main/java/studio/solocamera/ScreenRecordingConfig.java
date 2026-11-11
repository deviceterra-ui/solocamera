package studio.solocamera;
/** Bounded, even-sized encoder output that preserves the display's aspect ratio. */
final class ScreenRecordingConfig {
 static int[] size(int width,int height,int shortEdge){if(width<=0||height<=0)throw new IllegalArgumentException("Invalid screen size");double scale=Math.min(1,Math.min(shortEdge/(double)Math.min(width,height),1920.0/Math.max(width,height)));return new int[]{Math.max(16,((int)(width*scale)/16)*16),Math.max(16,((int)(height*scale)/16)*16)};}
 static int bitrate(int width,int height){return Math.max(2_000_000,Math.min(10_000_000,width*height*4));}
}
