package studio.solocamera;

/** Preview-sized monitoring analysis, never alters the recorded image. */
final class CreatorSignal {
    static void overlay(int[] pixels,int width,int height,boolean zebra,boolean peaking,int threshold){
        for(int y=0;y<height;y++)for(int x=0;x<width;x++){
            int i=y*width+x,p=pixels[i],r=(p>>16)&255,g=(p>>8)&255,b=p&255,l=(r*54+g*183+b*19)>>8;
            boolean sharp=false;
            if(peaking&&x+1<width&&y+1<height){int q=pixels[i+1],v=pixels[i+width];int ql=(((q>>16)&255)*54+((q>>8)&255)*183+(q&255)*19)>>8;int vl=(((v>>16)&255)*54+((v>>8)&255)*183+(v&255)*19)>>8;sharp=Math.abs(l-ql)+Math.abs(l-vl)>70;}
            pixels[i]=sharp?0xff65ff83:zebra&&l>=threshold&&((x+y)/4)%2==0?0xb0ffcf57:0;
        }
    }
}
